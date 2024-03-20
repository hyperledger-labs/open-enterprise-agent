let stack = import "./stack.ncl"
in
{
  st =
    stack.makeAgentStack { name = "issuer", port = 8080 },

  st-vault =
    stack.makeAgentStack { name = "issuer", port = 8080, vault.hostPort = 8200 },

  st-multi =
    (stack.makeAgentStack { name = "issuer", port = 8080 })
    & (stack.makeAgentStack { name = "holder", port = 8081 })
    & (stack.makeAgentStack { name = "verifier", port = 8082 }),

  st-oidc4vc =
    (stack.makeAgentStack { name = "issuer", port = 8080 })
    & (stack.makeMockServerStack { port = 5000 })
    & (
      stack.makeIssuerKeycloakStack
        {
          name = "issuer",
          port = 9980,
          realm = "students",
          build = "../../extensions/keycloak-oidc4vc",
          extraEnvs = { IDENTUS_URL = "http://caddy-issuer:8080/prism-agent" }
        }
    ),

  mt =
    stack.makeAgentStack { name = "default", port = 8080, apikeyEnabled = true },

  mt-keycloak =
    stack.makeAgentStack { name = "default", port = 8080, keycloakEnabled = true },

  mt-keycloak-vault =
    stack.makeAgentStack
      {
        name = "default",
        port = 8080,
        keycloakEnabled = true,
        vault.hostPort = 8200,
      },
}
