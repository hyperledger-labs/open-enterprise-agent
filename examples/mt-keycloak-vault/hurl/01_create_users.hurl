##############################
# Create users
##############################
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

# Create Issuer
POST {{ keycloak_base_url }}/admin/realms/{{ keycloak_realm }}/users
Authorization: Bearer {{ admin_access_token }}
{
  "username": "{{ issuer_username }}",
  "firstName": "Alice",
  "lastName": "Wonderland",
  "enabled": true,
  "email": "alice@atalaprism.io",
  "credentials": [{"value": "{{ issuer_password }}", "temporary": false}]
}
HTTP 201

# Create Holder
POST {{ keycloak_base_url }}/admin/realms/{{ keycloak_realm }}/users
Authorization: Bearer {{ admin_access_token }}
{
  "username": "{{ holder_username }}",
  "firstName": "SpongeBob",
  "lastName": "SquarePants",
  "enabled": true,
  "email": "bob@atalaprism.io",
  "credentials": [{"value": "{{ holder_password }}", "temporary": false}]
}
HTTP 201

# Create Verifier
POST {{ keycloak_base_url }}/admin/realms/{{ keycloak_realm }}/users
Authorization: Bearer {{ admin_access_token }}
{
  "username": "{{ verifier_username }}",
  "firstName": "John",
  "lastName": "Wick",
  "enabled": true,
  "email": "john@atalaprism.io",
  "credentials": [{"value": "{{ verifier_password }}", "temporary": false}]
}
HTTP 201

##############################
# Create Wallets
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

# Create Issuer wallet
POST {{ agent_url }}/prism-agent/wallets
Authorization: Bearer {{ issuer_access_token }}
{
  "name": "issuer-wallet"
}
HTTP 201

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

# Create Holder wallet
POST {{ agent_url }}/prism-agent/wallets
Authorization: Bearer {{ holder_access_token }}
{
  "name": "holder-wallet"
}
HTTP 201

# Verifier login
POST {{ keycloak_base_url }}/realms/{{ keycloak_realm }}/protocol/openid-connect/token
[FormParams]
grant_type: password
client_id: web-ui
username: {{ verifier_username }}
password: {{ verifier_password }}
HTTP 200
[Captures]
verifier_access_token: jsonpath "$.access_token"

# Create Verifier wallet
POST {{ agent_url }}/prism-agent/wallets
Authorization: Bearer {{ verifier_access_token }}
{
  "name": "verifier-wallet"
}
HTTP 201
