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

# Create agent client
POST {{ keycloak_base_url }}/admin/realms/{{ keycloak_realm }}/clients
Authorization: Bearer {{ admin_access_token }}
{
  "id": "{{ keycloak_client_id }}",
  "authorizationServicesEnabled": true,
  "serviceAccountsEnabled": true,
  "secret": "{{ keycloak_client_secret }}"
}
HTTP 201

# Create frontend client
POST {{ keycloak_base_url }}/admin/realms/{{ keycloak_realm }}/clients
Authorization: Bearer {{ admin_access_token }}
{
  "id": "web-ui",
  "directAccessGrantsEnabled": true,
  "publicClient": true
}
HTTP 201

