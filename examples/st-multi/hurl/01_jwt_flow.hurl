##############################
# Prerequisites
##############################
# Issuer create DID
POST {{ issuer_url }}/prism-agent/did-registrar/dids
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
POST {{ issuer_url }}/prism-agent/did-registrar/dids/{{ issuer_did }}/publications
HTTP 202

# Issuer wait for DID to be published
GET {{ issuer_url }}/prism-agent/did-registrar/dids/{{ issuer_did }}
[Options]
retry: -1
HTTP 200
[Asserts]
jsonpath "$.status" == "PUBLISHED"

# Holder create DID
POST {{ holder_url }}/prism-agent/did-registrar/dids
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
POST {{ issuer_url }}/prism-agent/connections
{
  "label": "My Connection"
}
HTTP 201
[Captures]
raw_invitation: jsonpath "$.invitation.invitationUrl" regex ".*_oob=(.*)$"
issuer_connection_id: jsonpath "$.connectionId"

# Invitee accept connection
POST {{ holder_url }}/prism-agent/connection-invitations
{
  "invitation": "{{ raw_invitation }}"
}
HTTP 200
[Captures]
holder_connection_id: jsonpath "$.connectionId"

# Wait for inviter connection status
GET {{ issuer_url }}/prism-agent/connections/{{ issuer_connection_id }}
[Options]
retry: -1
HTTP 200
[Asserts]
jsonpath "$.state" == "ConnectionResponseSent"

# Wait for invitee connection status
GET {{ holder_url }}/prism-agent/connections/{{ holder_connection_id }}
[Options]
retry: -1
HTTP 200
[Asserts]
jsonpath "$.state" == "ConnectionResponseReceived"

##############################
# Issuance
##############################
# Issuer create credential offer
POST {{ issuer_url }}/prism-agent/issue-credentials/credential-offers
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
GET {{ holder_url }}/prism-agent/issue-credentials/records
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
POST {{ holder_url }}/prism-agent/issue-credentials/records/{{ holder_cred_record_id }}/accept-offer
{
  "subjectId": "{{ holder_did }}"
}
HTTP 200

# Holder wait for CredentialReceived state
GET {{ holder_url }}/prism-agent/issue-credentials/records/{{ holder_cred_record_id }}
[Options]
retry: -1
HTTP 200
[Asserts]
jsonpath "$.protocolState" == "CredentialReceived"

# Issuer wait for CredentialSent state
GET {{ issuer_url }}/prism-agent/issue-credentials/records/{{ issuer_cred_record_id }}
[Options]
retry: -1
HTTP 200
[Asserts]
jsonpath "$.protocolState" == "CredentialSent"

##############################
# Presentation Connection
##############################
# Inviter create connection
POST {{ verifier_url }}/prism-agent/connections
{
  "label": "My Connection"
}
HTTP 201
[Captures]
raw_invitation: jsonpath "$.invitation.invitationUrl" regex ".*_oob=(.*)$"
verifier_connection_id: jsonpath "$.connectionId"

# Invitee accept connection
POST {{ holder_url }}/prism-agent/connection-invitations
{
  "invitation": "{{ raw_invitation }}"
}
HTTP 200
[Captures]
holder_connection_id: jsonpath "$.connectionId"

# Wait for inviter connection status
GET {{ verifier_url }}/prism-agent/connections/{{ verifier_connection_id }}
[Options]
retry: -1
HTTP 200
[Asserts]
jsonpath "$.state" == "ConnectionResponseSent"

# Wait for invitee connection status
GET {{ holder_url }}/prism-agent/connections/{{ holder_connection_id }}
[Options]
retry: -1
HTTP 200
[Asserts]
jsonpath "$.state" == "ConnectionResponseReceived"

##############################
# Presentation
##############################
# Verifier create presentation request
POST {{ verifier_url }}/prism-agent/present-proof/presentations
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
GET {{ holder_url }}/prism-agent/present-proof/presentations
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
PATCH {{ holder_url }}/prism-agent/present-proof/presentations/{{ holder_presentation_id }}
{
  "action": "request-accept",
  "proofId": ["{{ holder_cred_record_id }}"]
}
HTTP 200

# Holder wait for PresentationSent state
GET {{ holder_url }}/prism-agent/present-proof/presentations
[QueryStringParams]
thid: {{ didcomm_presentation_thid }}
[Options]
retry: -1
HTTP 200
[Asserts]
jsonpath "$.contents[0].status" == "PresentationSent"

# Verfiier wait for PresentationVerified state
GET {{ verifier_url }}/prism-agent/present-proof/presentations
[QueryStringParams]
thid: {{ didcomm_presentation_thid }}
[Options]
retry: -1
HTTP 200
[Asserts]
jsonpath "$.contents[0].status" == "PresentationVerified"
