let DbConfig = {
  host
    | String,
  port
    | Number
    | default
    = 5432,
  dbName
    | String,
  user
    | String,
  password
    | String,
}
in
let NodeConfig = {
  host
    | String,
  port
    | Number
    | default
    = 50053
}
in
let KeycloakConfig = {
  url
    | String,
  realmName
    | String,
  clientId
    | String,
  clientSecret
    | String
}
in
let VaultConfig = {
  url
    | String,
  token
    | String,
}
in
let AgentServiceArgs = {
  version
    | String,
  polluxDb
    | DbConfig,
  connectDb
    | DbConfig,
  agentDb
    | DbConfig,
  node
    | NodeConfig,
  didcommServiceUrl
    | String,
  restServiceUrl
    | String,
  apikeyEnabled
    | Bool,
  keycloak
    | KeycloakConfig
    | optional,
  vault
    | VaultConfig
    | optional,
  bootstrapContainer
    | String
    | doc "The container that agent must wait to complete before starting"
    | optional
}
in
{
  makeAgentService | AgentServiceArgs -> _ = fun args =>
      {
        image = "ghcr.io/input-output-hk/prism-agent:%{args.version}",
        restart = "always",
        environment =
          {
            POLLUX_DB_HOST = args.polluxDb.host,
            POLLUX_DB_PORT = std.to_string args.polluxDb.port,
            POLLUX_DB_NAME = args.polluxDb.dbName,
            POLLUX_DB_USER = args.polluxDb.user,
            POLLUX_DB_PASSWORD = args.polluxDb.password,
            CONNECT_DB_HOST = args.connectDb.host,
            CONNECT_DB_PORT = std.to_string args.connectDb.port,
            CONNECT_DB_NAME = args.connectDb.dbName,
            CONNECT_DB_USER = args.connectDb.user,
            CONNECT_DB_PASSWORD = args.connectDb.password,
            AGENT_DB_HOST = args.agentDb.host,
            AGENT_DB_PORT = std.to_string args.agentDb.port,
            AGENT_DB_NAME = args.agentDb.dbName,
            AGENT_DB_USER = args.agentDb.user,
            AGENT_DB_PASSWORD = args.agentDb.password,
            DIDCOMM_SERVICE_URL = args.didcommServiceUrl,
            REST_SERVICE_URL = args.restServiceUrl,
            PRISM_NODE_HOST = args.node.host,
            PRISM_NODE_PORT = std.to_string args.node.port,
            ADMIN_TOKEN = "admin",
            API_KEY_ENABLED = std.to_string args.apikeyEnabled,
          }
          & (
            if args |> std.record.has_field "keycloak" then
              {
                KEYCLOAK_ENABLED = "true",
                KEYCLOAK_URL = args.keycloak.url,
                KEYCLOAK_REALM = args.keycloak.realmName,
                KEYCLOAK_CLIENT_ID = args.keycloak.clientId,
                KEYCLOAK_CLIENT_SECRET = args.keycloak.clientSecret
              }
            else
              {}
          )
          & (
            if args |> std.record.has_field "vault" then
              {
                SECRET_STORAGE_BACKEND = "vault",
                VAULT_ADDR = args.vault.url,
                VAULT_TOKEN = args.vault.token
              }
            else
              { SECRET_STORAGE_BACKEND = "postgres" }
          ),
        depends_on =
          { "%{args.node.host}" = { condition = "service_started" } }
          & (
            if args |> std.record.has_field "bootstrapContainer" then
              { "%{args.bootstrapContainer}" = { condition = "service_completed_successfully" } }
            else
              {}
          )
      }
}
