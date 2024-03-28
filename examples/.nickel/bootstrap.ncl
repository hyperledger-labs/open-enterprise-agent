let HurlBootstrapServiceArgs = {
  version | String,
  hurlDir | String,
  variables
    | { _ : String }
    | default
    = {}
}
in
{
  makeHurlBootstrapService | HurlBootstrapServiceArgs -> _ = fun args =>
      {
        image = "ghcr.io/orange-opensource/hurl:%{args.version}",
        volumes = ["%{args.hurlDir}:/hurl"],
        command = ["--glob", "/hurl/*.hurl", "--test"],
        environment = args.variables,
      }
}
