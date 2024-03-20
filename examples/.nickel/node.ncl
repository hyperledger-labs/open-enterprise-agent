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
let NodeServiceArgs = {
  version
    | String,
  db
    | DbConfig,
}
in
{
  makeNodeService | NodeServiceArgs -> _ = fun args =>
      {
        image = "ghcr.io/input-output-hk/prism-node:%{args.version}",
        restart = "always",
        environment = {
          NODE_PSQL_HOST = "%{args.db.host}:%{std.to_string args.db.port}",
          NODE_PSQL_DATABASE = args.db.dbName,
          NODE_PSQL_USERNAME = args.db.user,
          NODE_PSQL_PASSWORD = args.db.password,
        },
        depends_on = {
          "%{args.db.host}" = {
            condition = "service_healthy"
          }
        }
      }
}
