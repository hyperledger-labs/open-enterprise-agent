##############################
# Login
##############################
# Issuer login
POST {{ keycloak_base_url }}/realms/{{ keycloak_realm }}/protocol/openid-connect/token
[FormParams]
grant_type: password
client_id: web-ui
username: {{ issuer_username }}
password: {{ issuer_password }}
HTTP 200
[Captures]
issuer_access_token: jsonpath "$.access_token"

# Holder login
POST {{ keycloak_base_url }}/realms/{{ keycloak_realm }}/protocol/openid-connect/token
[FormParams]
grant_type: password
client_id: web-ui
username: {{ holder_username }}
password: {{ holder_password }}
HTTP 200
[Captures]
holder_access_token: jsonpath "$.access_token"

# Verfier login
POST {{ keycloak_base_url }}/realms/{{ keycloak_realm }}/protocol/openid-connect/token
[FormParams]
grant_type: password
client_id: web-ui
username: {{ verifier_username }}
password: {{ verifier_password }}
HTTP 200
[Captures]
verifier_access_token: jsonpath "$.access_token"


##############################
# Prerequisites
##############################
# Issuer create DID
POST {{ agent_url }}/prism-agent/did-registrar/dids
Authorization: Bearer {{ issuer_access_token }}
{
  "documentTemplate": {
    "publicKeys": [
      {
        "id": "iss-key",
        "purpose": "assertionMethod"
      }
    ],
    "services": []
  }
}
HTTP 201
[Captures]
issuer_did: jsonpath "$.longFormDid" regex "(did:prism:[a-z0-9]+):.+$"

# Issuer publish DID
POST {{ agent_url }}/prism-agent/did-registrar/dids/{{ issuer_did }}/publications
Authorization: Bearer {{ issuer_access_token }}
HTTP 202

# Issuer wait for DID to be published
GET {{ agent_url }}/prism-agent/did-registrar/dids/{{ issuer_did }}
Authorization: Bearer {{ issuer_access_token }}
[Options]
retry: -1
HTTP 200
[Asserts]
jsonpath "$.status" == "PUBLISHED"

# Holder create DID
POST {{ agent_url }}/prism-agent/did-registrar/dids
Authorization: Bearer {{ holder_access_token }}
{
  "documentTemplate": {
    "publicKeys": [
      {
        "id": "auth-key",
        "purpose": "authentication"
      }
    ],
    "services": []
  }
}
HTTP 201
[Captures]
holder_did: jsonpath "$.longFormDid"

##############################
# Issuance Connection
##############################
# Inviter create connection
POST {{ agent_url }}/prism-agent/connections
Authorization: Bearer {{ issuer_access_token }}
{
  "label": "My Connection"
}
HTTP 201
[Captures]
raw_invitation: jsonpath "$.invitation.invitationUrl" regex ".*_oob=(.*)$"
issuer_connection_id: jsonpath "$.connectionId"

# Invitee accept connection
POST {{ agent_url }}/prism-agent/connection-invitations
Authorization: Bearer {{ holder_access_token }}
{
  "invitation": "{{ raw_invitation }}"
}
HTTP 200
[Captures]
holder_connection_id: jsonpath "$.connectionId"

# Wait for inviter connection status
GET {{ agent_url }}/prism-agent/connections/{{ issuer_connection_id }}
Authorization: Bearer {{ issuer_access_token }}
[Options]
retry: -1
HTTP 200
[Asserts]
jsonpath "$.state" == "ConnectionResponseSent"

# Wait for invitee connection status
GET {{ agent_url }}/prism-agent/connections/{{ holder_connection_id }}
Authorization: Bearer {{ holder_access_token }}
[Options]
retry: -1
HTTP 200
[Asserts]
jsonpath "$.state" == "ConnectionResponseReceived"

##############################
# Issuance
##############################
# Issuer create credential offer
POST {{ agent_url }}/prism-agent/issue-credentials/credential-offers
Authorization: Bearer {{ issuer_access_token }}
{
  "claims": {
    "emailAddress": "alice@wonderland.com",
    "givenName": "Alice",
    "familyName": "Wonderland"
  },
  "credentialFormat": "JWT",
  "issuingDID": "{{ issuer_did }}",
  "connectionId": "{{ issuer_connection_id }}"
}
HTTP 201
[Captures]
issuer_cred_record_id: jsonpath "$.recordId"
didcomm_issuing_thid: jsonpath "$.thid"

# Holder wait for OfferReceived state
GET {{ agent_url }}/prism-agent/issue-credentials/records
Authorization: Bearer {{ holder_access_token }}
[QueryStringParams]
thid: {{ didcomm_issuing_thid }}
[Options]
retry: -1
HTTP 200
[Asserts]
jsonpath "$.contents[0].protocolState" == "OfferReceived"
[Captures]
holder_cred_record_id: jsonpath "$.contents[0].recordId"

# Holder accept a credential-offer
POST {{ agent_url }}/prism-agent/issue-credentials/records/{{ holder_cred_record_id }}/accept-offer
Authorization: Bearer {{ holder_access_token }}
{
  "subjectId": "{{ holder_did }}"
}
HTTP 200

# Holder wait for CredentialReceived state
GET {{ agent_url }}/prism-agent/issue-credentials/records/{{ holder_cred_record_id }}
Authorization: Bearer {{ holder_access_token }}
[Options]
retry: -1
HTTP 200
[Asserts]
jsonpath "$.protocolState" == "CredentialReceived"

# Issuer wait for CredentialSent state
GET {{ agent_url }}/prism-agent/issue-credentials/records/{{ issuer_cred_record_id }}
Authorization: Bearer {{ issuer_access_token }}
[Options]
retry: -1
HTTP 200
[Asserts]
jsonpath "$.protocolState" == "CredentialSent"

##############################
# Presentation Connection
##############################
# Inviter create connection
POST {{ agent_url }}/prism-agent/connections
Authorization: Bearer {{ verifier_access_token }}
{
  "label": "My Connection"
}
HTTP 201
[Captures]
raw_invitation: jsonpath "$.invitation.invitationUrl" regex ".*_oob=(.*)$"
verifier_connection_id: jsonpath "$.connectionId"

# Invitee accept connection
POST {{ agent_url }}/prism-agent/connection-invitations
Authorization: Bearer {{ holder_access_token }}
{
  "invitation": "{{ raw_invitation }}"
}
HTTP 200
[Captures]
holder_connection_id: jsonpath "$.connectionId"

# Wait for inviter connection status
GET {{ agent_url }}/prism-agent/connections/{{ verifier_connection_id }}
Authorization: Bearer {{ verifier_access_token }}
[Options]
retry: -1
HTTP 200
[Asserts]
jsonpath "$.state" == "ConnectionResponseSent"

# Wait for invitee connection status
GET {{ agent_url }}/prism-agent/connections/{{ holder_connection_id }}
Authorization: Bearer {{ holder_access_token }}
[Options]
retry: -1
HTTP 200
[Asserts]
jsonpath "$.state" == "ConnectionResponseReceived"

##############################
# Presentation
##############################
# Verifier create presentation request
POST {{ agent_url }}/prism-agent/present-proof/presentations
Authorization: Bearer {{ verifier_access_token }}
{
  "connectionId": "{{ verifier_connection_id }}",
  "proofs":[],
  "options": {
    "challenge": "11c91493-01b3-4c4d-ac36-b336bab5bddf",
    "domain": "https://prism-verifier.com"
  }
}
HTTP 201
[Captures]
verifier_presentation_id: jsonpath "$.presentationId"
didcomm_presentation_thid: jsonpath "$.thid"

# Holder wait for RequestReceived state
GET {{ agent_url }}/prism-agent/present-proof/presentations
Authorization: Bearer {{ holder_access_token }}
[QueryStringParams]
thid: {{ didcomm_presentation_thid }}
[Options]
retry: -1
HTTP 200
[Asserts]
jsonpath "$.contents[0].status" == "RequestReceived"
[Captures]
holder_presentation_id: jsonpath "$.contents[0].presentationId"

# Holder accept presentation request
PATCH {{ agent_url }}/prism-agent/present-proof/presentations/{{ holder_presentation_id }}
Authorization: Bearer {{ holder_access_token }}
{
  "action": "request-accept",
  "proofId": ["{{ holder_cred_record_id }}"]
}
HTTP 200

# Holder wait for PresentationSent state
GET {{ agent_url }}/prism-agent/present-proof/presentations
Authorization: Bearer {{ holder_access_token }}
[QueryStringParams]
thid: {{ didcomm_presentation_thid }}
[Options]
retry: -1
HTTP 200
[Asserts]
jsonpath "$.contents[0].status" == "PresentationSent"

# Verfiier wait for PresentationVerified state
GET {{ agent_url }}/prism-agent/present-proof/presentations
Authorization: Bearer {{ verifier_access_token }}
[QueryStringParams]
thid: {{ didcomm_presentation_thid }}
[Options]
retry: -1
HTTP 200
[Asserts]
jsonpath "$.contents[0].status" == "PresentationVerified"
