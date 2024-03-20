let _agent = import "./agent.ncl" in
let _db = import "./db.ncl" in
let _node = import "./node.ncl" in
let _caddy = import "./caddy.ncl" in
let _keycloak = import "./keycloak.ncl" in
let _bootstrap = import "./bootstrap.ncl" in
let _vault = import "./vault.ncl" in
let V = import "./versions.ncl" in
let ExternalKeycloakStackConfig = {
  name | String,
  port | Number,
  realm | String,
  build | String,
  extraEnvs
    | { _ | String }
    | default
    = {}
}
in
let AgentStackConfig = {
  name | String,
  port | Number,
  apikeyEnabled | Bool | default = false,
  keycloakEnabled | Bool | default = false,
  vault | { hostPort | Number } | optional,
}
in
{
  makeMockServerStack | { port | Number } -> _ = fun args =>
      {
        services = {
          mockserver = {
            image = "mockserver/mockserver:%{V.mockServer}",
            ports = ["%{std.to_string args.port}:1080"]
          }
        }
      },
  makeIssuerKeycloakStack | ExternalKeycloakStackConfig -> _ = fun args =>
      {
        services = {
          "external-keycloak-%{args.name}" =
            (
              _keycloak.makeKeycloakService
                {
                  version = V.keycloak,
                  hostnamePath = "",
                  hostnamePort = args.port,
                }
              |> std.record.remove "image"
            )
            & {
              ports = ["%{std.to_string args.port}:8080"],
              environment = args.extraEnvs,
              build = args.build
            },
          "external-keycloak-init-%{args.name}" =
            _bootstrap.makeHurlBootstrapService
              {
                version = V.hurl,
                hurlDir = "./bootstrap",
                variables = {
                  HURL_keycloak_base_url = "http://external-keycloak-%{args.name}:8080",
                  HURL_keycloak_admin_user = "admin",
                  HURL_keycloak_admin_password = "admin",
                  HURL_keycloak_realm = args.realm,
                  HURL_alice_username = "alice",
                  HURL_alice_password = "1234",
                  HURL_alice_wallet_client_id = "alice-wallet",
                }
              },
        }
      },
  makeAgentStack | AgentStackConfig -> _ = fun args =>
      let pgDockerVolumeName = "pg_data_%{args.name}" in
      let hosts = {
        caddy = "caddy-%{args.name}",
        agent = "agent-%{args.name}",
        keycloak = "keycloak-%{args.name}",
        keycloakInit = "keycloak-init-%{args.name}",
        vault = "vault-%{args.name}",
        db = "db-%{args.name}",
        # node services are static so they are the same when merged between stacks
        node = "node",
        nodeDb = "node-db",
      }
      in
      let caddyArgs = {
        version = V.caddy,
        hostPort = args.port,
        name = args.name,
        agent = {
          host = hosts.agent,
          restPort = 8085,
          didcommPort = 8090,
        },
        keycloak = {
          host = hosts.keycloak,
          port = 8080
        },
        vault = {
          host = hosts.vault,
          port = 8200
        }
      }
      in
      let makeSharedDbConfig = fun databaseName =>
        {
          host = hosts.db,
          dbName = databaseName,
          user = "postgres",
          password = "postgres",
        }
      in
      {
        version = "3",
        configs = _caddy.makeCaddyConfig caddyArgs,
        volumes = {
          pg_data_node = {},
          "%{pgDockerVolumeName}" = {},
        },
        services =
          {
            "%{hosts.caddy}" = _caddy.makeCaddyService caddyArgs,
            "%{hosts.nodeDb}" =
              _db.makeDbService
                {
                  version = V.postgres,
                  databases = "node_db",
                  volumeName = "pg_data_node"
                },
            "%{hosts.db}" =
              _db.makeDbService
                {
                  version = V.postgres,
                  databases = "pollux,connect,agent",
                  volumeName = pgDockerVolumeName
                },
            "%{hosts.node}" =
              _node.makeNodeService
                {
                  version = V.node,
                  db = {
                    host = hosts.nodeDb,
                    dbName = "node_db",
                    user = "postgres",
                    password = "postgres",
                  },
                },
            "%{hosts.agent}" =
              _agent.makeAgentService
                (
                  {
                    version = V.agent,
                    polluxDb = makeSharedDbConfig "pollux",
                    connectDb = makeSharedDbConfig "connect",
                    agentDb = makeSharedDbConfig "agent",
                    node = { host = "node" },
                    didcommServiceUrl = "http://%{hosts.caddy}:%{std.to_string args.port}/didcomm",
                    restServiceUrl = "http://%{hosts.caddy}:%{std.to_string args.port}/prism-agent",
                    apikeyEnabled = args.apikeyEnabled,
                  }
                  & (
                    if args.keycloakEnabled then
                      {
                        keycloak = {
                          url = "http://%{hosts.keycloak}:8080",
                          realmName = "identus",
                          clientId = "agent",
                          clientSecret = "agent-secret"
                        },
                        bootstrapContainer = "%{hosts.keycloakInit}",
                      }
                    else
                      {}
                  )
                  & (
                    if args |> std.record.has_field "vault" then
                      { vault = { url = "http://%{hosts.vault}:8200", token = "admin" } }
                    else
                      {}
                  )
                ),
          }
          # optional services
          & (
            if args.keycloakEnabled then
              {
                "%{hosts.keycloak}" =
                  _keycloak.makeKeycloakService
                    {
                      version = V.keycloak,
                      hostnamePort = args.port
                    },
                "%{hosts.keycloakInit}" =
                  _bootstrap.makeHurlBootstrapService
                    {
                      version = V.hurl,
                      hurlDir = "../.shared/hurl/simple_realm",
                      variables = {
                        HURL_keycloak_base_url = "http://%{hosts.keycloak}:8080",
                        HURL_keycloak_admin_user = "admin",
                        HURL_keycloak_admin_password = "admin",
                        HURL_keycloak_realm = "identus",
                        HURL_keycloak_client_id = "agent",
                        HURL_keycloak_client_secret = "agent-secret",
                      }
                    },
              }
            else
              {}
          )
          & (
            if args |> std.record.has_field "vault" then
              {
                "%{hosts.vault}" =
                  _vault.makeVaultService
                    {
                      version = V.vault,
                      hostPort = args.vault.hostPort,
                      rootToken = "admin"
                    }
              }
            else
              {}
          )
      }
}
