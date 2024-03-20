let CaddyServiceArgs = {
  version
    | String,
  hostPort
    | Number,
  name
    | String
    | default
    = "default",
  agent
    | { host | String, restPort | Number, didcommPort | Number },
  keycloak
    | { host | String, port | Number },
  vault
    | { host | String, port | Number },
}
in
{
  makeCaddyConfig | CaddyServiceArgs -> _ = fun args =>
      {
        "caddyfile_%{args.name}" = {
          content = m%"
              :%{std.to_string args.hostPort} {
                handle_path /didcomm* {
                  reverse_proxy %{args.agent.host}:%{std.to_string args.agent.didcommPort}
                }
                handle_path /prism-agent* {
                  reverse_proxy %{args.agent.host}:%{std.to_string args.agent.restPort}
                }
                handle_path /keycloak* {
                  reverse_proxy %{args.keycloak.host}:%{std.to_string args.keycloak.port}
                }
                handle_path /vault* {
                  reverse_proxy %{args.vault.host}:%{std.to_string args.vault.port}
                }
              }
            "%
        }
      },
  makeCaddyService | CaddyServiceArgs -> _ = fun args =>
      {
        image = "caddy:%{args.version}",
        restart = "always",
        configs = [
          {
            source = "caddyfile_%{args.name}",
            target = "/etc/caddy/Caddyfile"
          }
        ],
        ports = ["%{std.to_string args.hostPort}:%{std.to_string args.hostPort}"],
      }
}
