# How to run examples

## Prerequisites

- docker-compose version >= `2.23.1`

## Running examples

Most of the examples should follow the same pattern.
Simply go to each example directory and spin up the docker-compose of each example.

```bash
cd <EXAMPLE_DIR>
docker-compose up
```

If some example requires a different command, it should be provided in its own local README.

## Examples

| example             | description                                                               |
|---------------------|---------------------------------------------------------------------------|
| `st`                | single-tenant configuration without external services (except database)   |
| `st-multi`          | 3 instances of single-tenant configuration                                |
| `st-vault`          | single-tenant with Vault for secret storage                               |
| `st-oid4vci`        | single-tenant agent with Keycloak as external Issuer Authorization Server |
| `mt`                | multi-tenant configuration using built-in IAM                             |
| `mt-keycloak`       | multi-tenant configuration using Keycloak for IAM                         |
| `mt-keycloak-vault` | multi-tenant configuration using Keycloak and Vault                       |

## Testing examples

Some example directories may contain a sub-directory called `hurl`.
Hurl is a CLI tool for testing HTTP requests and can be installed according to [this documentation](https://hurl.dev/docs/installation.html).
If an example contains a sub-directory named `hurl`, the example can be tested against HTTP calls with the following commands.

```bash
cd ./hurl
hurl --variables-file ./local *.hurl
```

# Contributing

All of the docker-compose files in examples are generated using [Nickel](https://nickel-lang.org/).
They are defined in a shared `.nickel` directory and generated using the `build.sh` script.

## Prerequisites

- [Nickel](https://nickel-lang.org/) version >= `1.5` installed

## Generate example compose files

To generate the docker-compose config for all examples, run

```bash
cd .nickel
./build.sh
```

## Updating example compose files

To update the configuration, simply edit the `*.ncl` config in the `.nickel` directory and regenerate the docker-compose files.

## Adding new examples

To add a new example with docker-compose file, simply create a new configuration key in the `root.ncl` and add a new entry in the `build.sh` script.
You may need to create the target example directory if it does not already exist.

## Example with bootstrapping script

If any example requires initialize steps, it should be made part of the docker-compose `depends_on` construct.
Ideally, infrastructure bootstrapping should be automatic (database, IAM), but not necessarily application bootstrapping (tenant onboarding).
