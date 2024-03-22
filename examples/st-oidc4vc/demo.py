import json
import jwt
import requests
import threading
import time
import urllib

from cryptography.hazmat.primitives.asymmetric import ec


MOCKSERVER_URL = "http://localhost:5000"
LOGIN_REDIRECT_URL = "http://localhost:5000/cb"

AGENT_URL = "http://localhost:8080/prism-agent"
CREDENTIAL_ISSUER = None
CREDENTIAL_CONFIGURATION_ID = "UniversityDegreeCredential"
AUTHORIZATION_SERVER = "http://localhost:9980/realms/students"

ALICE_CLIENT_ID = "alice-wallet"


def prepare_mock_server():
    # reset mock server
    requests.put(f"{MOCKSERVER_URL}/mockserver/reset")

    # mock wallet authorization callback endpoint
    requests.put(
        f"{MOCKSERVER_URL}/mockserver/expectation",
        json={
            "httpRequest": {"path": "/cb"},
            "httpResponse": {
                "statusCode": 200,
                "body": {"type": "string", "string": "Login Successful"},
            },
        },
    )


def prepare_issuer():
    dids = requests.get(f"{AGENT_URL}/did-registrar/dids").json()["contents"]
    if len(dids) == 0:
        requests.post(
            f"{AGENT_URL}/did-registrar/dids",
            json={
                "documentTemplate": {
                    "publicKeys": [{"id": "iss", "purpose": "assertionMethod"}],
                    "services": [],
                }
            },
        )
        dids = requests.get(f"{AGENT_URL}/did-registrar/dids").json()["contents"]

    issuer_did = dids[0]
    while issuer_did["status"] != "PUBLISHED":
        time.sleep(2)
        canonical_did = issuer_did["did"]
        issuer_did = requests.get(
            f"{AGENT_URL}/did-registrar/dids/{canonical_did}"
        ).json()

        # publish if not pending
        if issuer_did["status"] == "CREATED":
            requests.post(
                f"{AGENT_URL}/did-registrar/dids/{canonical_did}/publications"
            )

    global CREDENTIAL_ISSUER
    canonical_did = issuer_did["did"]
    CREDENTIAL_ISSUER = f"{AGENT_URL}/oidc4vc/{canonical_did}"
    print(f"CREDENTIAL_ISSUER: {CREDENTIAL_ISSUER}")


def issuer_create_credential_offer(claims):
    response = requests.post(
        f"{CREDENTIAL_ISSUER}/credential-offers",
        json={"schemaId": "TODO", "claims": claims},
    )
    return response.json()["credentialOffer"]


def holder_get_issuer_metadata(credential_issuer: str):
    metadata_url = f"{credential_issuer}/.well-known/openid-credential-issuer"
    # TODO: OEA should return these instead of hardcoded values
    return {
        "credential_issuer": CREDENTIAL_ISSUER,
        "authorization_servers": [AUTHORIZATION_SERVER],
        "credential_endpoint": f"{CREDENTIAL_ISSUER}/credentials",
        "credential_identifiers_supported": False,
        "credential_configurations_supported": {
            CREDENTIAL_CONFIGURATION_ID: {
                "format": "jwt_vc_json",
                "scope": CREDENTIAL_CONFIGURATION_ID,
                "credential_signing_alg_values_supported": ["ES256K"],
                "credential_definition": {
                    "type": ["VerifiableCredential", "UniversityDegreeCredential"],
                    "credentialSubject": {
                        "degree": {},
                        "gpa": {"display": [{"name": "GPA"}]},
                    },
                },
            }
        },
    }


def holder_get_issuer_as_metadata(authorization_server: str):
    metadata_url = f"{authorization_server}/.well-known/openid-configuration"
    response = requests.get(metadata_url)
    metadata = response.json()
    # print(json.dumps(metadata, indent=2))
    return metadata


def holder_start_login_flow(auth_endpoint: str, token_endpoint: str, issuer_state: str):
    def wait_redirect_authorization_code() -> str:
        print("wating for authorization redirect ...")
        while True:
            response = requests.put(
                f"{MOCKSERVER_URL}/mockserver/retrieve?type=REQUESTS",
                json={"path": "/cb", "method": "GET"},
            ).json()
            if len(response) > 0:
                break
            time.sleep(1)

        authorzation_code = response[0]["queryStringParameters"]["code"][0]
        print(f"code: {authorzation_code}")
        return authorzation_code

    def start_authorization_request(auth_endpoint: str, issuer_state: str):
        # Authorization Request
        queries = urllib.parse.urlencode(
            {
                "redirect_uri": LOGIN_REDIRECT_URL,
                "response_type": "code",
                "client_id": ALICE_CLIENT_ID,
                "scope": "openid " + CREDENTIAL_CONFIGURATION_ID,
                "issuer_state": issuer_state,
            }
        )
        login_url = f"{auth_endpoint}?{queries}"
        print("\n##############################\n")
        print("Open this link in the browser to login\n")
        print(login_url)
        print("\n##############################\n")

        # wait for authorization redirect
        authorzation_code = wait_redirect_authorization_code()
        return authorzation_code

    def start_token_request(token_endpoint: str, authorization_code: str):
        # Token Request
        response = requests.post(
            token_endpoint,
            data={
                "grant_type": "authorization_code",
                "code": authorization_code,
                "client_id": ALICE_CLIENT_ID,
                "redirect_uri": LOGIN_REDIRECT_URL,
            },
        )
        return response.json()

    authorization_code = start_authorization_request(auth_endpoint, issuer_state)
    token_response = start_token_request(token_endpoint, authorization_code)
    return token_response


def holder_extract_credential_offer(offer_uri: str):
    queries = urllib.parse.urlparse(credential_offer_uri).query
    credential_offer = urllib.parse.parse_qs(queries)["credential_offer"]
    return json.loads(credential_offer[0])


def holder_get_credential(credential_endpoint: str, token_response):
    access_token = token_response["access_token"]
    c_nonce = token_response["c_nonce"]
    c_nonce_expires_in = token_response["c_nonce_expires_in"]

    # generate proof
    private_key = ec.generate_private_key(ec.SECP256K1())
    jwt_proof = jwt.encode(
        headers={
            "typ": "openid4vci-proof+jwt",
            "kid": "did:prism:0000000000000000000000000000000000000000000000000000000000000000#key-1",  # TODO: use actual DID
        },
        payload={
            "iss": ALICE_CLIENT_ID,
            "aud": CREDENTIAL_ISSUER,
            "iat": int(time.time()),
            "nonce": c_nonce,
        },
        key=private_key,
        algorithm="ES256K",  # TODO: switch to EdDSA alg (Ed25519)
    )

    response = requests.post(
        credential_endpoint,
        headers={"Authorization": f"Bearer {access_token}"},
        json={
            "format": "jwt_vc_json",
            "credential_definition": {
                "type": ["VerifiableCredential", CREDENTIAL_CONFIGURATION_ID],
                "credentialSubject": {},
            },
            "proof": {"proof_type": "jwt", "jwt": jwt_proof},
        },
    )
    return response.json()


if __name__ == "__main__":
    prepare_mock_server()
    prepare_issuer()

    # step 1: Issuer create CredentialOffer
    credential_offer_uri = issuer_create_credential_offer(
        {"degree": "ChemicalEngineering", "gpa": "3.00"}
    )

    # step 2: Issuer present QR code container CredentialOffer URI
    credential_offer = holder_extract_credential_offer(credential_offer_uri)
    credential_offer_pretty = json.dumps(credential_offer, indent=2)
    issuer_state = credential_offer["grants"]["authorization_code"]["issuer_state"]
    print("\n##############################\n")
    print(f"QR code scanned, got credential-offer\n\n{credential_offer_uri}\n")
    print(f"\n{credential_offer_pretty}\n")
    print("\n##############################\n")
    input("\nEnter to continue ...")

    # step 3: Holdler retreive Issuer's metadata
    issuer_metadata = holder_get_issuer_metadata(CREDENTIAL_ISSUER)
    authorzation_server = issuer_metadata["authorization_servers"][0]
    print("\n::::: Issuer Metadata :::::")
    print(json.dumps(issuer_metadata, indent=2))
    input("\nEnter to continue ...")

    # step 3.1: Holder retreive Issuer's AS metadata
    issuer_as_metadata = holder_get_issuer_as_metadata(authorzation_server)
    issuer_as_token_endpoint = issuer_as_metadata["token_endpoint"]
    issuer_as_authorization_endpoint = issuer_as_metadata["authorization_endpoint"]
    print("\n::::: Issuer Authorization Server Metadata :::::")
    print(f"issuer_as_auth_endpoint: {issuer_as_authorization_endpoint}")
    print(f"issuer_as_token_endpoint: {issuer_as_token_endpoint}")
    input("\nEnter to continue ...")

    # step 4: Holder start authorization flow
    token_response = holder_start_login_flow(
        issuer_as_authorization_endpoint, issuer_as_token_endpoint, issuer_state
    )
    print("::::: Token Response :::::")
    print(json.dumps(token_response, indent=2))
    input("\nEnter to continue ...")

    # step 5: Holder use access_token to get credential
    credential_endpoint = issuer_metadata["credential_endpoint"]
    jwt_credential = holder_get_credential(credential_endpoint, token_response)
    print("\n::::: Credential Received :::::")
    print(json.dumps(jwt_credential, indent=2))
