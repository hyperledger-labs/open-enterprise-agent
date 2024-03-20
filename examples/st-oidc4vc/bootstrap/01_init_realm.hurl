# Wait for keycloak ready
GET {{ keycloak_base_url }}/health/ready
[Options]
retry: 300
HTTP 200

# Admin login
POST {{ keycloak_base_url }}/realms/master/protocol/openid-connect/token
[FormParams]
grant_type: password
client_id: admin-cli
username: {{ keycloak_admin_user }}
password: {{ keycloak_admin_password }}
HTTP 200
[Captures]
admin_access_token: jsonpath "$.access_token"

# Create realm
POST {{ keycloak_base_url }}/admin/realms
authorization: Bearer {{ admin_access_token }}
{
  "realm": "{{ keycloak_realm }}",
  "enabled": true
}
HTTP 201

# Create Alice
POST {{ keycloak_base_url }}/admin/realms/{{ keycloak_realm }}/users
Authorization: Bearer {{ admin_access_token }}
{
  "username": "{{ alice_username }}",
  "firstName": "Alice",
  "lastName": "Wonderland",
  "enabled": true,
  "email": "alice@atalaprism.io",
  "credentials": [{"value": "{{ alice_password }}", "temporary": false}]
}
HTTP 201

##############################
# TODO: actions below to be performed by controller
##############################
# Pre-register holder wallet client // TODO: dynamic registration?
POST {{ keycloak_base_url }}/admin/realms/{{ keycloak_realm }}/clients
Authorization: Bearer {{ admin_access_token }}
{
  "id": "{{ alice_wallet_client_id }}",
  "publicClient": true,
  "consentRequired": true,
  "redirectUris": [ "http://localhost:5000/*" ]
}
HTTP 201

# Create a scope for issuable credential
POST {{ keycloak_base_url }}/admin/realms/{{ keycloak_realm }}/client-scopes
Authorization: Bearer {{ admin_access_token }}
{
  "name": "UniversityDegreeCredential",
  "description": "The University Degree Credential",
  "protocol": "openid-connect",
  "attributes": {
    "consent.screen.text": "University Degree",
    "display.on.consent.screen": "true",
    "include.in.token.scope": "true",
    "gui.order": ""
  }
}
HTTP 201
[Captures]
client_scope_id: header "Location" split "/" nth 7

# scope mapping
PUT {{ keycloak_base_url }}/admin/realms/{{ keycloak_realm }}/clients/{{ alice_wallet_client_id }}/optional-client-scopes/{{ client_scope_id }}
Authorization: Bearer {{ admin_access_token }}
{}
HTTP 204

