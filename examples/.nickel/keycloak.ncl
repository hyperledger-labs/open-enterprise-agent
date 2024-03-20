let KeycloakServiceArgs = {
  hostname
    | String
    | default
    = "localhost",
  hostnamePath
    | String
    | default
    = "/keycloak",
  hostnamePort
    | Number
}
in
{
  makeKeycloakService
    | KeycloakServiceArgs
    & { version | String } -> _
    = fun args =>
      {
        image = "quay.io/keycloak/keycloak:%{args.version}",
        restart = "always",
        environment = {
          KEYCLOAK_ADMIN = "admin",
          KEYCLOAK_ADMIN_PASSWORD = "admin",
        },
        command = [
          "start-dev",
          "--features=preview",
          "--health-enabled=true",
          "--hostname-url=http://%{args.hostname}:%{std.to_string args.hostnamePort}%{args.hostnamePath}",
          "--hostname-admin-url=http://%{args.hostname}:%{std.to_string args.hostnamePort}%{args.hostnamePath}",
        ]
      }
}
