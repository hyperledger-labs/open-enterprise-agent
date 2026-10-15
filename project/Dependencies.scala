import sbt.*

object V {
  val munit = "1.3.6" // "0.7.29"
  val munitZio = "0.4.0"

  // https://mvnrepository.com/artifact/dev.zio/zio
  val zio = "2.1.26"
  val zioConfig = "4.0.8"
  val zioLogging = "2.5.3"
  val zioJson = "0.7.45"
  val zioHttp = "3.7.4"
  val zioCatsInterop = "3.3.0" // TODO "23.1.0.2" // https://mvnrepository.com/artifact/dev.zio/zio-interop-cats
  val zioMetricsConnector = "2.5.8"
  val zioMock = "1.0.0-RC12"
  val zioKafka = "3.7.1"
  val mockito = "3.2.18.0"
  val monocle = "3.3.0"

  val tapir = "1.11.7" // scala-steward:off // TODO "1.10.5"
  val http4sBlaze = "0.23.15" // scala-steward:off  // TODO "0.23.16"

  val typesafeConfig = "1.4.4"
  val protobuf = "3.1.9"
  val grpcOkHttp = "1.82.1" // align with grpc pulled in by scalapb-runtime-grpc (sbt 2.x -> scalapb 1.0.0-alpha.6)

  // align with Docker client API used by GH runners
  val testContainersScala = "0.44.1"
  val testContainersJavaKeycloak = "3.2.0" // scala-steward:off

  val doobie = "1.0.0-RC5" // scala-steward:off
  val quill = "4.8.6"
  val flyway = "9.22.3" // scala-steward:off
  val postgresDriver = "42.7.14"
  val logback = "1.5.18"
  val slf4j = "2.0.17"

  val scalaUri = "4.2.0"

  val jwtZioVersion = "11.0.2"
  val zioPreludeVersion = "1.0.0-RC48"

  val apollo = "1.8.8"

  val jsonSchemaValidator = "1.3.2" // scala-steward:off //TODO 1.3.2 need to fix:
  // [error] 	org.hyperledger.identus.pollux.core.model.schema.AnoncredSchemaTypeSpec
  // [error] 	org.hyperledger.identus.pollux.core.model.schema.CredentialSchemaSpec

  val commonsLogging = "1.3.5"
  val vaultDriver = "6.2.0"
  val micrometer = "1.15.2"

  val nimbusJwt = "9.37.3" // scala-steward:off //TODO: >=9.38 breaking change
  val keycloak = "23.0.7" // scala-steward:off //TODO 24.0.3 // update all quay.io/keycloak/keycloak

  val vdr = "0.2.1"
  val prismVdr = "0.3.0"
}

/** Dependencies */
object D {
  val zio: ModuleID = "dev.zio" %% "zio" % V.zio
  val zioStreams: ModuleID = "dev.zio" %% "zio-streams" % V.zio
  val zioLog: ModuleID = "dev.zio" %% "zio-logging" % V.zioLogging
  val zioSLF4J: ModuleID = "dev.zio" %% "zio-logging-slf4j" % V.zioLogging
  val zioJson: ModuleID = "dev.zio" %% "zio-json" % V.zioJson
  val zioConcurrent: ModuleID = "dev.zio" %% "zio-concurrent" % V.zio
  val zioHttp: ModuleID = "dev.zio" %% "zio-http" % V.zioHttp
  val zioKafka: ModuleID = ("dev.zio" %% "zio-kafka" % V.zioKafka).excludeAll(
    ExclusionRule("dev.zio", "zio_3"),
    ExclusionRule("dev.zio", "zio-streams_3")
  )
  val zioCatsInterop: ModuleID = "dev.zio" %% "zio-interop-cats" % V.zioCatsInterop
  val zioMetricsConnectorMicrometer: ModuleID = "dev.zio" %% "zio-metrics-connectors-micrometer" % V.zioMetricsConnector
  val tapirPrometheusMetrics: ModuleID = "com.softwaremill.sttp.tapir" %% "tapir-prometheus-metrics" % V.tapir
  val micrometer: ModuleID = "io.micrometer" % "micrometer-registry-prometheus" % V.micrometer
  val micrometerPrometheusRegistry = "io.micrometer" % "micrometer-core" % V.micrometer
  val scalaUri = Seq(
    ("com.indoorvivants" %% "scala-uri" % V.scalaUri).exclude(
      "org.typelevel",
      "cats-parse_3"
    ), // Exclude cats-parse to avoid deps conflict
    "org.typelevel" % "cats-parse_3" % "1.1.0", // Replace with version 1.0.0
  )

  val zioConfig: ModuleID = "dev.zio" %% "zio-config" % V.zioConfig
  val zioConfigMagnolia: ModuleID = "dev.zio" %% "zio-config-magnolia" % V.zioConfig
  val zioConfigTypesafe: ModuleID = "dev.zio" %% "zio-config-typesafe" % V.zioConfig

  val commonsLogging = "commons-logging" % "commons-logging" % V.commonsLogging
  val networkntJsonSchemaValidator = "com.networknt" % "json-schema-validator" % V.jsonSchemaValidator
  val jwtZio = "com.github.jwt-scala" %% "jwt-zio-json" % V.jwtZioVersion
  val jsonCanonicalization: ModuleID = "io.github.erdtman" % "java-json-canonicalization" % "1.1"
  val titaniumJsonLd: ModuleID = "com.apicatalog" % "titanium-json-ld" % "1.6.0"
  val jakartaJson: ModuleID = "org.glassfish" % "jakarta.json" % "2.0.1" // used by titanium-json-ld
  val ironVC: ModuleID = "com.apicatalog" % "iron-verifiable-credentials" % "0.14.0"
  val scodecBits: ModuleID = "org.scodec" %% "scodec-bits" % "1.2.4"
  val jaywayJsonPath: ModuleID = "com.jayway.jsonpath" % "json-path" % "2.9.0"

  // https://mvnrepository.com/artifact/org.didcommx/didcomm/0.3.2
  val didcommx: ModuleID = "org.didcommx" % "didcomm" % "0.3.2"
  val peerDidcommx: ModuleID = "org.didcommx" % "peerdid" % "0.5.0"
  // peerdid depends on java-multibase (transitive, JitPack only). v1.1.0 has stale .sha1 metadata,
  // so we force v1.1.1 which currently has consistent JitPack checksums. Remove once peerdid upgrades.
  val javaMultibase: ModuleID = "com.github.multiformats" % "java-multibase" % "v1.1.1"
  val didScala: ModuleID = "app.fmgp" %% "did" % "0.0.0+113-61efa271-SNAPSHOT"

  val nimbusJwt: ModuleID = "com.nimbusds" % "nimbus-jose-jwt" % V.nimbusJwt

  val typesafeConfig: ModuleID = "com.typesafe" % "config" % V.typesafeConfig
  val scalaPbRuntime: ModuleID =
    "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf"
  val scalaPbGrpc: ModuleID = "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion
  val grpcOkHttp: ModuleID = "io.grpc" % "grpc-okhttp" % V.grpcOkHttp

  val testcontainersPostgres: ModuleID =
    "com.dimafeng" %% "testcontainers-scala-postgresql" % V.testContainersScala % Test
  val testcontainersVault: ModuleID = "com.dimafeng" %% "testcontainers-scala-vault" % V.testContainersScala % Test
  val testcontainersKeycloak: ModuleID =
    "com.github.dasniko" % "testcontainers-keycloak" % V.testContainersJavaKeycloak % Test

  val doobiePostgres: ModuleID = "org.tpolecat" %% "doobie-postgres" % V.doobie
  val doobieHikari: ModuleID = "org.tpolecat" %% "doobie-hikari" % V.doobie
  val flyway: ModuleID = "org.flywaydb" % "flyway-core" % V.flyway

  // For munit https://scalameta.org/munit/docs/getting-started.html#scalajs-setup
  val munit: ModuleID = "org.scalameta" %% "munit" % V.munit % Test
  // For munit zio https://github.com/poslegm/munit-zio
  val munitZio: ModuleID = "com.github.poslegm" %% "munit-zio" % V.munitZio % Test

  val zioTest: ModuleID = "dev.zio" %% "zio-test" % V.zio % Test
  val zioTestSbt: ModuleID = "dev.zio" %% "zio-test-sbt" % V.zio % Test
  val zioTestMagnolia: ModuleID = "dev.zio" %% "zio-test-magnolia" % V.zio % Test
  val zioMock: ModuleID = "dev.zio" %% "zio-mock" % V.zioMock
  val zioPrelude: ModuleID = "dev.zio" %% "zio-prelude" % V.zioPreludeVersion
  val mockito: ModuleID = "org.scalatestplus" %% "mockito-4-11" % V.mockito % Test
  val monocle: ModuleID = "dev.optics" %% "monocle-core" % V.monocle % Test
  val monocleMacro: ModuleID = "dev.optics" %% "monocle-macro" % V.monocle % Test
  val scalaTest = "org.scalatest" %% "scalatest" % "3.2.19" % Test

  private val apolloJvm: ModuleID =
    ("org.hyperledger.identus" % "apollo-jvm" % V.apollo).exclude(
      "net.jcip",
      "jcip-annotations"
    ) // bitcoinj-core still pulls net.jcip:jcip-annotations transitively

  private val jcipAnnotationsRuntime: ModuleID =
    "com.github.stephenc.jcip" % "jcip-annotations" % "1.0-1" % Runtime

  val apollo = Seq(
    apolloJvm,
    jcipAnnotationsRuntime // License-compatible replacement for net.jcip:jcip-annotations
  )

  // LIST of Dependencies
  val doobieDependencies: Seq[ModuleID] =
    Seq(doobiePostgres, doobieHikari, flyway)
}

object D_Shared {
  lazy val dependencies: Seq[ModuleID] =
    Seq(
      D.typesafeConfig,
      D.scalaPbGrpc,
      D.zio,
      D.zioConcurrent,
      D.zioHttp,
      D.zioKafka,
      D.zioPrelude,
      // FIXME: split shared DB stuff as subproject?
      D.doobieHikari,
      D.doobiePostgres,
      D.zioCatsInterop,
    ) ++ D.scalaUri
}

object D_SharedJson {
  lazy val dependencies: Seq[ModuleID] =
    Seq(
      D.zio,
      D.zioJson,
      D.jsonCanonicalization,
      D.titaniumJsonLd,
      D.jakartaJson,
      D.ironVC,
      D.scodecBits,
      D.networkntJsonSchemaValidator,
      D.jaywayJsonPath
    )
}

object D_SharedCrypto {
  lazy val dependencies: Seq[ModuleID] =
    Seq(
      D.zioJson,
      D.nimbusJwt,
      D.zioTest,
      D.zioTestSbt,
      D.zioTestMagnolia,
    ) ++ D.apollo
}

object D_SharedTest {
  lazy val dependencies: Seq[ModuleID] =
    D_Shared.dependencies ++ Seq(
      D.testcontainersPostgres,
      D.testcontainersVault,
      D.testcontainersKeycloak,
      D.zioCatsInterop,
      D.zioJson,
      D.zioHttp,
      D.zioTest,
      D.zioTestSbt,
      D.zioTestMagnolia,
      D.zioMock,
      // The Keycloak admin client (used by KeycloakTestContainerSupport) depends on Apache
      // HttpClient, which needs commons-logging. Under sbt 1.x this arrived transitively, but sbt
      // 2.x's dependency resolution no longer pulls it in, so add it explicitly.
      D.commonsLogging,
    )
}

object D_Connect {

  private lazy val logback = "ch.qos.logback" % "logback-classic" % V.logback % Test

  // Dependency Modules
  private lazy val baseDependencies: Seq[ModuleID] =
    Seq(D.zio, D.zioTest, D.zioTestSbt, D.zioTestMagnolia, D.zioMock, D.testcontainersPostgres, logback)

  // Project Dependencies
  lazy val coreDependencies: Seq[ModuleID] =
    baseDependencies
  lazy val sqlDoobieDependencies: Seq[ModuleID] =
    baseDependencies ++ D.doobieDependencies ++ Seq(D.zioCatsInterop)
}

object D_Castor {
  // Dependency Modules
  val baseDependencies: Seq[ModuleID] =
    Seq(
      D.zio,
      D.zioTest,
      D.zioMock,
      D.zioTestSbt,
      D.zioTestMagnolia,
      D.zioHttp,
    )

  // Project Dependencies
  val coreDependencies: Seq[ModuleID] = baseDependencies
}

object D_Pollux {
  val logback = "ch.qos.logback" % "logback-classic" % V.logback % Test
  val slf4jApi = "org.slf4j" % "slf4j-api" % V.slf4j % Test
  val slf4jSimple = "org.slf4j" % "slf4j-simple" % V.slf4j % Test

  val doobiePostgres = "org.tpolecat" %% "doobie-postgres" % V.doobie
  val doobieHikari = "org.tpolecat" %% "doobie-hikari" % V.doobie

  val flyway = "org.flywaydb" % "flyway-core" % V.flyway

  val quillJdbcZio = ("io.getquill" %% "quill-jdbc-zio" %
    V.quill).exclude("org.scala-lang.modules", "scala-java8-compat_3")

  val quillDoobie = ("io.getquill" %% "quill-doobie" %
    V.quill).exclude("org.scala-lang.modules", "scala-java8-compat_3")

  // Dependency Modules
  val baseDependencies: Seq[ModuleID] = Seq(
    D.zio,
    D.zioJson,
    D.zioHttp,
    D.zioTest,
    D.zioTestSbt,
    D.zioTestMagnolia,
    D.zioMock,
    D.munit,
    D.munitZio,
    // shared,
    logback,
    slf4jApi,
    slf4jSimple
  )

  val doobieDependencies: Seq[ModuleID] = Seq(
    D.zioCatsInterop,
    D.doobiePostgres,
    D.doobieHikari,
    D.testcontainersPostgres,
    flyway,
    quillDoobie,
    quillJdbcZio,
  )

  // Project Dependencies
  val coreDependencies: Seq[ModuleID] = baseDependencies
  val sqlDoobieDependencies: Seq[ModuleID] = baseDependencies ++ doobieDependencies
}

object D_Pollux_VC_JWT {

  val zio = "dev.zio" %% "zio" % V.zio
  val zioPrelude = "dev.zio" %% "zio-prelude" % V.zioPreludeVersion

  val zioTest = "dev.zio" %% "zio-test" % V.zio % Test
  val zioTestSbt = "dev.zio" %% "zio-test-sbt" % V.zio % Test
  val zioTestMagnolia = "dev.zio" %% "zio-test-magnolia" % V.zio % Test

  // Dependency Modules
  val zioDependencies: Seq[ModuleID] = Seq(zio, zioPrelude, zioTest, zioTestSbt, zioTestMagnolia)
  val baseDependencies: Seq[ModuleID] =
    zioDependencies :+ D.jwtZio :+ D.networkntJsonSchemaValidator :+ D.nimbusJwt :+ D.scalaTest

  // Project Dependencies
  lazy val polluxVcJwtDependencies: Seq[ModuleID] = baseDependencies
}

object D_EventNotification {
  val zio = "dev.zio" %% "zio" % V.zio
  val zioTest = "dev.zio" %% "zio-test" % V.zio % Test
  val zioTestSbt = "dev.zio" %% "zio-test-sbt" % V.zio % Test
  val zioTestMagnolia = "dev.zio" %% "zio-test-magnolia" % V.zio % Test

  val zioDependencies: Seq[ModuleID] = Seq(zio, zioTest, zioTestSbt, zioTestMagnolia)
  val baseDependencies: Seq[ModuleID] = zioDependencies
}

object D_Pollux_AnonCreds {
  val baseDependencies: Seq[ModuleID] = Seq(D.zio, D.zioJson)
}

object D_CloudAgent {
  val logback = "ch.qos.logback" % "logback-classic" % V.logback

  val tapirSwaggerUiBundle = "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % V.tapir
  val tapirJsonZio = "com.softwaremill.sttp.tapir" %% "tapir-json-zio" % V.tapir

  val tapirZioHttpServer = "com.softwaremill.sttp.tapir" %% "tapir-zio-http-server" % V.tapir
  val tapirHttp4sServerZio = "com.softwaremill.sttp.tapir" %% "tapir-http4s-server-zio" % V.tapir
  val http4sBlazeServer = "org.http4s" %% "http4s-blaze-server" % V.http4sBlaze

  val tapirRedocBundle = "com.softwaremill.sttp.tapir" %% "tapir-redoc-bundle" % V.tapir

  val tapirSttpStubServer =
    "com.softwaremill.sttp.tapir" %% "tapir-sttp-stub-server" % V.tapir % Test
  val sttpClient3ZioJson = "com.softwaremill.sttp.client3" %% "zio-json" % "3.11.0" % Test

  val quillDoobie =
    ("io.getquill" %% "quill-doobie" % V.quill).exclude("org.scala-lang.modules", "scala-java8-compat_3")
  val postgresql = "org.postgresql" % "postgresql" % V.postgresDriver
  val quillJdbcZio =
    ("io.getquill" %% "quill-jdbc-zio" % V.quill).exclude("org.scala-lang.modules", "scala-java8-compat_3")

  val flyway = "org.flywaydb" % "flyway-core" % V.flyway

  val vaultDriver = "io.github.jopenlibs" % "vault-java-driver" % V.vaultDriver
  val keycloakAuthz = "org.keycloak" % "keycloak-authz-client" % V.keycloak

  val vdr = "org.hyperledger.identus" % "vdr" % V.vdr
  val prismVdr = "org.hyperledger.identus" %% "prism-vdr-driver" % V.prismVdr

  // Dependency Modules
  val baseDependencies: Seq[ModuleID] = Seq(
    D.zio,
    D.zioTest,
    D.zioTestSbt,
    D.zioTestMagnolia,
    D.zioConfig,
    D.zioConfigMagnolia,
    D.zioConfigTypesafe,
    D.zioJson,
    logback,
    D.zioHttp,
    D.zioMetricsConnectorMicrometer,
    D.tapirPrometheusMetrics,
    D.micrometer,
    D.micrometerPrometheusRegistry
  )
  val tapirDependencies: Seq[ModuleID] =
    Seq(
      tapirSwaggerUiBundle,
      tapirJsonZio,
      tapirRedocBundle,
      tapirSttpStubServer,
      sttpClient3ZioJson,
      tapirZioHttpServer,
      tapirHttp4sServerZio,
      http4sBlazeServer
    )

  val postgresDependencies: Seq[ModuleID] =
    Seq(quillDoobie, quillJdbcZio, postgresql, flyway, D.testcontainersPostgres, D.zioCatsInterop)

  // Project Dependencies
  lazy val keyManagementDependencies: Seq[ModuleID] =
    baseDependencies ++ D.doobieDependencies ++ Seq(D.zioCatsInterop, D.zioMock, vaultDriver)

  lazy val iamDependencies: Seq[ModuleID] = Seq(keycloakAuthz, D.jwtZio, D.commonsLogging)

  lazy val vdrDependencies: Seq[ModuleID] = Seq(vdr, prismVdr)

  lazy val serverDependencies: Seq[ModuleID] =
    baseDependencies ++ tapirDependencies ++ postgresDependencies ++ Seq(
      D.zioMock,
      D.mockito,
      D.monocle,
      D.monocleMacro
    )
}
