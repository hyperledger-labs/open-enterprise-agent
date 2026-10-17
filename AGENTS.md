# AGENTS.md — Cloud Agent Guide

## Project Overview

**Hyperledger Identus Cloud Agent** — a W3C/Aries standards-based cloud agent providing self-sovereign identity (SSI) services via REST API + DIDComm V2. Written in **Scala 3.3.5**, built with **SBT**.

Architecture: a controller sends HTTP requests to the agent and receives webhook notifications. The agent handles DID operations, verifiable credential issuance/verification, DIDComm messaging, and ledger anchoring (Cardano).

## Module Structure

The project is a multi-module SBT build. All project definitions are in `build.sbt`.

| Module | Directory | Description |
| ------- | ----------- | ------------- |
| `predef` | `shared/predef/` | Common implicit imports used project-wide via `-Yimports` |
| `shared` | `shared/core/` | Shared ZIO, Doobie, config dependencies |
| `shared-json` | `shared/json/` | JSON processing, JSON-LD, VC schema validation |
| `shared-crypto` | `shared/crypto/` | Crypto primitives, JWT, Apollo bindings |
| `shared-test` | `shared/test/` | Test utilities and fixtures |
| `mercury-data-models` | `mercury/models/` | DIDComm data models and service interfaces |
| `mercury-protocol-connection` | `mercury/protocol-connection/` | DIDComm connection protocol |
| `mercury-protocol-coordinate-mediation` | `mercury/protocol-coordinate-mediation/` | Mediation coordination protocol |
| `mercury-protocol-did-exchange` | `mercury/protocol-did-exchange/` | DID exchange protocol |
| `mercury-protocol-invitation` | `mercury/protocol-invitation/` | Out-of-band invitation protocol |
| `mercury-protocol-outofband-login` | `mercury/protocol-outofband-login/` | OOB login protocol |
| `mercury-protocol-report-problem` | `mercury/protocol-report-problem/` | Problem report protocol |
| `mercury-protocol-routing-2-0` | `mercury/protocol-routing/` | DIDComm routing protocol |
| `mercury-protocol-issue-credential` | `mercury/protocol-issue-credential/` | Issue credential protocol |
| `mercury-protocol-present-proof` | `mercury/protocol-present-proof/` | Present proof protocol |
| `mercury-protocol-revocation-notification` | `mercury/protocol-revocation-notification/` | Revocation notification protocol |
| `mercury-protocol-trust-ping` | `mercury/protocol-trust-ping/` | Trust ping protocol |
| `mercury-agent-core` | `mercury/agent/` | Agent core logic |
| `agent-didcommx` | `mercury/agent-didcommx/` | DIDComm v2 via didcommx JVM library |
| `mercury-vc` | `mercury/vc/` | Verifiable credential base types |
| `mercury-resolver` | `mercury/resolver/` | DID resolver |
| `castor-core` | `castor/` | DID operations (`did:prism`, `did:peer`) |
| `pollux-vc-jwt` | `pollux/vc-jwt/` | JWT-VC credential operations |
| `pollux-core` | `pollux/core/` | Credential service core |
| `pollux-sql-doobie` | `pollux/sql-doobie/` | Pollux SQL persistence (Doobie) |
| `pollux-anoncreds` | `pollux/anoncreds/` | AnonCreds support (JNI) |
| `pollux-anoncredsTest` | `pollux/anoncredsTest/` | AnonCreds tests |
| `pollux-sd-jwt` | `pollux/sd-jwt/` | SD-JWT-VC support |
| `pollux-prex` | `pollux/prex/` | Presentation exchange |
| `connect-core` | `connect/core/` | Connection management service |
| `connect-sql-doobie` | `connect/sql-doobie/` | Connect SQL persistence (Doobie) |
| `event-notification` | `event-notification/` | Webhook/event notification system |
| `prism-node-client` | `prism-node/client/scala-client/` | gRPC client for PRISM Node |
| `vdr-core` | `vdr/core/` | VDR core interfaces |
| `vdr-memory` | `vdr/memory/` | In-memory VDR backend |
| `vdr-prism-node` | `vdr/prism-node/` | PRISM Node VDR backend |
| `vdr-neoprism` | `vdr/neoprism/` | NeoPRISM VDR backend |
| `vdr-database` | `vdr/database/` | PostgreSQL VDR backend |
| `vdr-blockfrost` | `vdr/blockfrost/` | Blockfrost VDR backend |
| `vdr-proxy` | `vdr/proxy/` | VDR proxy routing to backends |
| `cloud-agent-wallet-api` | `cloud-agent/service/wallet-api/` | Wallet API, key management, IAM, multi-tenancy |
| `cloud-agent-vdr` | `cloud-agent/service/vdr/` | VDR routing layer: wires VDR backends (prism-node, neoprism, database, memory, blockfrost, proxy) into the cloud agent |
| `identus-cloud-agent` | `cloud-agent/service/server/` | **Main server entrypoint** (`org.hyperledger.identus.agent.server.MainApp`) |
| `root` | `.` | Aggregate project — runs commands across all modules |

### Entrypoints

- **Server binary**: `identus-cloud-agent` module, main class `org.hyperledger.identus.agent.server.MainApp`
- **Infrastructure scripts**: `infrastructure/dev/` and `infrastructure/local/` for composing dependent services (PostgreSQL, Vault, APISIX, PRISM Node)
- **Docker Compose config**: `docs/docker-compose.yml` (Swagger UI + Structurizr), `infrastructure/` for full stack

## Build Commands

All commands run via **SBT**. Java JDK 21 (Temurin) is required.

```bash
# Compile all modules
sbt compile

# Download all dependencies (CI uses this before testing)
sbt +update

# Compile and run unit tests
sbt test

# Full local pipeline: clean → compile → test → build Docker image
sbt clean compile test docker:publishLocal

# Run tests for a specific module
sbt "castor-core / test"
sbt "pollux-core / test"
sbt "cloud-agent-wallet-api / test"

# Coverage (Scoverage)
sbt coverage test coverageReport coverageAggregate

# Required JVM opts (set this to avoid OOM)
export SBT_OPTS="-Xmx2G"
```

### Scalac Options (from build.sbt)

```scala
scalacOptions ++= Seq("-feature", "-deprecation", "-unchecked")
scalacOptions += "-Wunused:all"
scalacOptions += "-Wconf:any:error,cat=deprecation:warning"
scalacOptions ++= Seq("-Xmax-inlines", "50")
// Compile only: adds project-wide Predef import
Compile / scalacOptions += "-Yimports:java.lang,scala,scala.Predef,org.hyperledger.identus.Predef"
```

## Format and Lint Commands

### Scala Formatting

- **Formatter**: Scalafmt 3.11.1
- **Config**: `.scalafmt.conf` (maxColumn=120, trailingCommas=preserve, scala3 dialect)
- **Commands**:

  ```bash
  sbt scalafmtAll          # Format all Scala sources
  sbt scalafmtCheckAll     # Check formatting without modifying
  sbt scalafixAll          # Run Scalafix lint rules
  ```

### Non-Scala Linting

- **MegaLinter** runs in CI only (`.github/workflows/lint.yml`), covering YAML, Markdown, OpenAPI, and other file types
- **Prettier** is used for non-Scala formatting (`package.json` has prettier as devDependency)

## Test Commands

### Unit Tests

- Framework: **munit** + **munit-zio** (primary), **zio-test**, **scalatest** (partial)
- Run all: `sbt test`
- Single module: `sbt "moduleName / test"`
- Single test: `sbt "moduleName / testOnly org.hyperledger.identus.SomeSpec"`

### Integration Tests

Integration tests require Docker and external services. They use **Testcontainers for Scala** and run via CI (`.github/workflows/integration-tests.yml`).

**Prerequisites**:

- Docker with docker-compose
- PostgreSQL, Hashicorp Vault, Keycloak, APISIX containers
- PRISM Node or NeoPRISM node (configurable via `infrastructure/local/.env`)
- DOCKER_API_VERSION=1.44

**Required env vars for tests**:

```bash
export DOCKER_API_VERSION="1.44"
export DOCKER_HOST="unix:///var/run/docker.sock"
export TESTCONTAINERS_RYUK_DISABLED=true
```

Infrastructure scripts for local integration testing:

- `infrastructure/dev/` — development environment scripts (build.sh, run.sh, clean.sh)
- `infrastructure/local/` — local run scripts with env configuration

### Performance Tests

Run via `.github/workflows/performance-tests.yml` (push to main, PR, or manual dispatch).

## Docker Build and Release

### Docker Image

- **Image**: published to both Docker Hub (`hyperledgeridentus/identus-cloud-agent`) and GHCR (`ghcr.io/hyperledger-identus/cloud-agent`)
- **Base image**: `eclipse-temurin:22-jdk-ubi9-minimal`
- **Exposed ports**: 8085, 8090
- **Build locally**: `sbt docker:publishLocal`

### Release Process

Defined in `build.sbt`:

```
checkSnapshotDependencies → inquireVersions → runClean → runTest →
setReleaseVersion → Docker/stage → setNextVersion
```

- **Version file**: `version.sbt` (currently `2.1.1-SNAPSHOT`)
- **Release workflow**: `.github/workflows/release.yml` — manual dispatch, publishes to GHCR + Docker Hub
- **Build workflow**: `.github/workflows/build.yml` — scheduled weekly (Saturday 03:00 UTC), builds + publishes revision
- **Semantic release**: `package.json` configured with semantic-release, conventional commits

### Docker Compose

- **Full stack**: see [identus-docker](https://github.com/hyperledger/identus/blob/main/identus-docker/dockerize-identus.md) for Cloud Agent + Mediator compose
- **Dev configs**: `infrastructure/local/`, `infrastructure/dev/`
- **Configuration modes**: Dev (PostgreSQL + in-memory PRISM Node), Pre-production (PostgreSQL + testnet), Production (Hashicorp Vault + mainnet)

## REST API Documentation

- **OpenAPI spec**: `cloud-agent/service/api/http/cloud-agent-openapi-spec.yaml`
- **Breaking change detection**: `.github/workflows/oasdiff.yml` — runs `oasdiff` on PRs to detect API breaking changes
- **Client libraries**: auto-generated from the OpenAPI spec (kotlin, python clients in `client/`)
- **Health endpoint**: `GET /cloud-agent/_system/health`
- **Online docs**: [hyperledger-identus.github.io/docs/](https://hyperledger-identus.github.io/docs/)
- **API reference**: [hyperledger.github.io/identus-docs/agent-api/](https://hyperledger.github.io/identus-docs/agent-api/)

## Scala 3-Specific Patterns

### Explicit `using` Clauses

The project uses Scala 3 `using` clauses extensively for ZIO `ZIO` environment requirements and dependency injection. Example pattern:

```scala
def myEffect: ZIO[MyService & Repository, Error, Result] = ???
```

The `shared/predef/` module defines a common implicit predef that is auto-imported via `-Yimports` for the `Compile` configuration only:

```scala
Compile / scalacOptions += "-Yimports:java.lang,scala,scala.Predef,org.hyperledger.identus.Predef"
```

### Scala 3 Enums

Algebraic data types use Scala 3 `enum` syntax throughout:

```scala
enum CredentialFormat:
  case JWT
  case AnonCreds
  case SDJWT
```

### Inline Max Increase

The `-Xmax-inlines 50` flag is required because several libraries (circe, zio-config-magnolia) generate deep inline expansions that exceed the default limit of 32.

### Cross-Compilation

All modules use `crossPaths := false` for shared modules (shared, shared-json, shared-crypto, shared-test) because they are JVM-only.

### Wconf for Deprecations

```scala
scalacOptions += "-Wconf:any:error,cat=deprecation:warning"
```

Deprecation warnings are elevated to errors, except for deprecation category which remains a warning.

## Codegen, Generated Code, and Build Artifacts

### gRPC Codegen (protobuf → ScalaPB)

- **Plugin**: `sbt-protoc` with ScalaPB
- **Module**: `prism-node-client`
- **Sources**: `prism-node/client/scala-client/api/grpc/*.proto`
- **Output**: generated Scala sources in `prism-node/client/scala-client/target/scala-3.3.5/src_managed/`
- **Excluded from coverage**: `.*proto.*;.*grpc.*;.*scalapb.*;.*protobuf.*;.*generated.*`

### Build Info

- **Plugin**: `sbt-buildinfo`
- **Generated class**: `org.hyperledger.identus.agent.server.buildinfo.BuildInfo` (in `identus-cloud-agent` module)
- **Keys**: name, version, scalaVersion, sbtVersion

### Coverage Reports

- **Tool**: Scoverage (`sbt-scoverage`, `sbt-coveralls`)
- **Output dir**: `target/coverage/`
- **Cobertura XML**: `target/coverage/coverage-report/cobertura.xml`
- **Report**: `sbt coverage test coverageReport` → `target/scala-3.3.5/coverage-report/`
- **CI**: Coverage pushed to Coveralls (`.github/workflows/unit-tests.yml`)

## Testing Quirks

- **Integration tests require** Docker, PostgreSQL, Vault, Keycloak, and a PRISM Node / NeoPRISM node — they will fail without these
- **Testcontainers Ryuk disabled** (`TESTCONTAINERS_RYUK_DISABLED=true`) to avoid networking issues in CI
- **Docker API version pinned**: `DOCKER_API_VERSION=1.44` (align with GitHub runner Docker version)
- **SBT `fork := true`** is set globally — tests run in a forked JVM
- **JMX disabled** for tests: `-Dlog4j2.disable.jmx=true`
- **AnonCreds native lib**: `pollux/anoncreds/` includes a JNI JAR (`anoncreds-jvm-1.0-SNAPSHOT.jar`) and native libraries in `native-lib/NATIVE/`
- **Performance tests** — runs on pull_request, push to main, or manual dispatch (not part of standard CI)

### CI Workflow Structure

| Workflow | Trigger | What it does |
| ---------- | --------- | ------------- |
| `unit-tests.yml` | push to main + PR | Build + unit tests via SBT |
| `integration-tests.yml` | push to main + PR | Full integration suite with Docker Compose |
| `build.yml` | weekly Sat + dispatch | Build Docker image, publish revision |
| `release.yml` | manual dispatch | Tag, release, publish Docker + clients |
| `lint.yml` (MegaLinter) | manual dispatch | Lint all non-Scala files |
| `oasdiff.yml` | PR | Check OpenAPI spec for breaking changes |
| `performance-tests.yml` | push to main + PR + dispatch | Run performance benchmarks (k6) |
| `file-hygiene.yml` | PR | Lint text files (markdown, YAML, editorconfig) |

## Version Compatibility

Cloud Agent → PRISM Node compatibility matrix (from README):

| Cloud Agent | PRISM Node |
| ------------- | ----------- |
| >=1.9.2 | 2.2.1 |
| <1.9.2 | 2.1.1 |

## Key Files Reference

| Purpose | Path |
| ---------- | ------ |
| Build definition | `build.sbt` |
| Version | `version.sbt` |
| SBT plugins | `project/plugins.sbt` |
| SBT properties | `project/build.properties` |
| Scalafmt config | `.scalafmt.conf` |
| OpenAPI spec | `cloud-agent/service/api/http/cloud-agent-openapi-spec.yaml` |
| Docker compose (docs) | `docs/docker-compose.yml` |
| Infrastructure scripts | `infrastructure/dev/`, `infrastructure/local/` |
| CI workflows | `.github/workflows/*.yml` |
| Release process | `.github/workflows/release.yml` |

## Commit Conventions

- Format: Conventional Commits (enforced by commitlint + husky)
- DCO sign-off required (see `DCO.md`)
- Examples: `feat: add support for SD-JWT-VC`, `fix: resolve DID resolution for deactivated keys`
