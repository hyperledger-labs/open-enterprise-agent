let DbServiceArgs = {
  version
    | String,
  databases
    | String,
  volumeName
    | String
}
in
{
  makeDbService | DbServiceArgs -> _ = fun args =>
      {
        image = "postgres:%{args.version}",
        restart = "always",
        environment = {
          POSTGRES_MULTIPLE_DATABASES = args.databases,
          POSTGRES_USER = "postgres",
          POSTGRES_PASSWORD = "postgres",
        },
        volumes = [
          "%{args.volumeName}:/var/lib/postgresql/data",
          "../.shared/postgres/init-script.sh:/docker-entrypoint-initdb.d/init-script.sh",
          "../.shared/postgres/max_conns.sql:/docker-entrypoint-initdb.d/max_conns.sql",
        ],
        healthcheck = {
          test = ["CMD", "pg_isready", "-U", "postgres", "-d", "postgres"],
          interval = "10s",
          timeout = "5s",
          retries = 5
        }
      }
}
