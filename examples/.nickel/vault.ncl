let VaultServiceArgs = {
  version
    | String,
  hostPort
    | Number,
  rootToken
    | String
    | default
    = "admin"
}
in
{
  makeVaultService | VaultServiceArgs -> _ = fun args =>
      {
        image = "hashicorp/vault:%{args.version}",
        ports = ["%{std.to_string args.hostPort}:8200"],
        environment = {
          VAULT_ADDR = "http://0.0.0.0:8200",
          VAULT_DEV_ROOT_TOKEN_ID = args.rootToken
        },
        cap_add = ["IPC_LOCK"],
        healthcheck = {
          test = ["CMD", "vault", "status"],
          interval = "10s",
          timeout = "5s",
          retries = "5",
        }
      }
}
