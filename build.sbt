import sbtbuildinfo.BuildInfoPlugin.autoImport.*

// externalResolvers += "ScalaLibrary packages" at "https://maven.pkg.github.com/input-output-hk/anoncreds-rs" // use plugin"sbt-github-packages"

inThisBuild(
  Seq(
    organization := "org.hyperledger",
    scalaVersion := "3.3.5",
    licenses := Seq("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
    fork := true,
    run / connectInput := true,
    releaseUseGlobalVersion := false,
    versionScheme := Some("semver-spec"),
    // scalapb 1.0.0-alpha.6 (required for sbt 2.x) is selected over the 0.11.20 that
    // `app.fmgp:did-method-prism` transitively depends on. Mark the scheme as "always" so this
    // early-semver binary-incompat conflict is downgraded to an eviction instead of a hard error.
    libraryDependencySchemes += "com.thesamet.scalapb" %% "scalapb-runtime" % "always",
    libraryDependencySchemes += "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % "always",
  )
)

// Fixes a bug with concurrent packages download from GitHub registry
Global / concurrentRestrictions += Tags.limit(Tags.Network, 1)

coverageDataDir := target.value / "coverage"
coverageExcludedPackages := "(?i).*proto.*;.*grpc.*;.*scalapb.*;.*protobuf.*;.*generated.*"

inThisBuild(
  Seq(
    // NOTE: use a single `:=` assignment here. Under sbt 2.x the previous `scalacOptions ++=` / `+=`
    // chain inside `inThisBuild` was resolved multiple times, tripling the options and making
    // scalac fail with "Flag X set repeatedly" (promoted to error by -Wconf:any:error).
    scalacOptions := Seq(
      "-encoding", "UTF-8",
      "-feature",
      "-deprecation",
      "-unchecked",
      "-Wunused:all",
      "-Wconf:any:error,cat=deprecation:warning", // "-Wconf:help",
      // "-Yexplicit-nulls",
      // "-Ysafe-init",
      // "-Werror", // <=> "-Xfatal-warnings"
      "-Dquill.macro.log=false", // disable quill macro logs // TODO https://github.com/zio/zio-protoquill/issues/470
      "-Xmax-inlines", "50", // increase above 32 (https://github.com/circe/circe/issues/2162)
    ),
    Test / javaOptions ++= Seq("-Dlog4j2.disable.jmx=true", "-Ddocker.api.version=1.44"),
    Test / envVars ++= Map(
      "DOCKER_API_VERSION" -> "1.44",
      "DOCKER_HOST" -> "unix:///var/run/docker.sock"
    )
  )
)


publish / skip := true

val commonSetttings = Seq(
  testFrameworks ++= Seq(new TestFramework("zio.test.sbt.ZTestFramework")),
  libraryDependencies ++= Seq(D.zioTest, D.zioTestSbt, D.zioTestMagnolia),
  resolvers += "Local Maven Repository" at "file://" + Path.userHome.absolutePath + "/.m2/repository",
  // Needed for Kotlin coroutines that support new memory management mode
  resolvers += "JetBrains Space Maven Repository" at "https://maven.pkg.jetbrains.space/public/p/kotlinx-coroutines/maven",
  // Needed for com.github.multiformats:java-multibase
  resolvers += "jitpack" at "https://jitpack.io",
)

lazy val commonConfigure: Project => Project = _.settings(
  Compile / scalacOptions += "-Yimports:java.lang,scala,scala.Predef,org.hyperledger.identus.Predef",
  Test / scalacOptions -= "-Yimports:java.lang,scala,scala.Predef,org.hyperledger.identus.Predef",
  dependencyOverrides += D.javaMultibase,
  // sbt 2.x defaults `persistJarClasspath`/`exportJars` to true, packaging each project's classes into
  // a jar on downstream/test classpaths. Set at project scope (not inThisBuild) to override the
  // sbt default. Some tests do `Paths.get(getClass.getResource("/").toURI)`, which assumes a
  // directory classpath (sbt 1.x behavior) and throws FileSystemNotFoundException when the classpath
  // root is inside a jar.
  persistJarClasspath := false,
  exportJars := false,
).dependsOn(predef)

// #####################
// #####  shared  ######
// #####################

lazy val predef = (project in file("shared/predef"))

lazy val shared = (project in file("shared/core"))
  .configure(commonConfigure)
  .settings(commonSetttings)
  .settings(
    name := "shared",
    crossPaths := false,
    libraryDependencies ++= D_Shared.dependencies
  )

lazy val sharedJson = (project in file("shared/json"))
  .settings(commonSetttings)
  .settings(
    name := "shared-json",
    crossPaths := false,
    libraryDependencies ++= D_SharedJson.dependencies
  )
  .dependsOn(shared)

lazy val sharedCrypto = (project in file("shared/crypto"))
  .configure(commonConfigure)
  .settings(commonSetttings)
  .settings(
    name := "shared-crypto",
    crossPaths := false,
    libraryDependencies ++= D_SharedCrypto.dependencies
  )
  .dependsOn(shared)

lazy val sharedTest = (project in file("shared/test"))
  .configure(commonConfigure)
  .settings(commonSetttings)
  .settings(
    name := "shared-test",
    crossPaths := false,
    libraryDependencies ++= D_SharedTest.dependencies
  )
  .dependsOn(shared)

// #########################
// ### Models & Services ###
// #########################

/** Just data models and interfaces of service.
  *
  * This module must not depend on external libraries!
  */
lazy val models = project
  .in(file("mercury/models"))
  .configure(commonConfigure)
  .settings(name := "mercury-data-models")
  .settings(
    libraryDependencies ++= Seq(D.zio)
  )
  .settings(libraryDependencies += D.nimbusJwt) // FIXME just for the DidAgent
  .dependsOn(shared)

/* TODO move code from agentDidcommx to here
models implementation for didcommx () */
// lazy val modelsDidcommx = project
//   .in(file("models-didcommx"))
//   .settings(name := "mercury-models-didcommx")
//   .settings(libraryDependencies += D.didcommx)
//   .dependsOn(models)

// #################
// ### Protocols ###
// #################

lazy val protocolConnection = project
  .in(file("mercury/protocol-connection"))
  .configure(commonConfigure)
  .settings(name := "mercury-protocol-connection")
  .settings(libraryDependencies += D.zio)
  .settings(libraryDependencies += D.munitZio)
  .dependsOn(models, protocolInvitation)

lazy val protocolCoordinateMediation = project
  .in(file("mercury/protocol-coordinate-mediation"))
  .configure(commonConfigure)
  .settings(name := "mercury-protocol-coordinate-mediation")
  .settings(libraryDependencies += D.zio)
  .settings(libraryDependencies += D.munitZio)
  .dependsOn(models)

lazy val protocolDidExchange = project
  .in(file("mercury/protocol-did-exchange"))
  .configure(commonConfigure)
  .settings(name := "mercury-protocol-did-exchange")
  .settings(libraryDependencies += D.zio)
  .dependsOn(models, protocolInvitation)

lazy val protocolInvitation = project
  .in(file("mercury/protocol-invitation"))
  .configure(commonConfigure)
  .settings(name := "mercury-protocol-invitation")
  .settings(libraryDependencies += D.zio)
  .settings(
    libraryDependencies ++= Seq(
      D.munit,
      D.munitZio
    )
  )
  .dependsOn(models)

// lazy val protocolMercuryMailbox = project
//   .in(file("mercury/protocol-mercury-mailbox"))
//   .settings(predefSetttings)
//   .settings(name := "mercury-protocol-mailbox")
//   .settings(libraryDependencies += D.zio)
//   .dependsOn(models, protocolInvitation, protocolRouting)

lazy val protocolLogin = project
  .in(file("mercury/protocol-outofband-login"))
  .configure(commonConfigure)
  .settings(name := "mercury-protocol-outofband-login")
  .settings(libraryDependencies += D.zio)
  .settings(libraryDependencies += D.zio)
  .settings(libraryDependencies += D.munitZio)
  .dependsOn(models)

lazy val protocolReportProblem = project
  .in(file("mercury/protocol-report-problem"))
  .configure(commonConfigure)
  .settings(name := "mercury-protocol-report-problem")
  .settings(libraryDependencies += D.munitZio)
  .dependsOn(models)

lazy val protocolRouting = project
  .in(file("mercury/protocol-routing"))
  .configure(commonConfigure)
  .settings(name := "mercury-protocol-routing-2-0")
  .settings(libraryDependencies += D.zio)
  .dependsOn(models)

lazy val protocolIssueCredential = project
  .in(file("mercury/protocol-issue-credential"))
  .configure(commonConfigure)
  .settings(name := "mercury-protocol-issue-credential")
  .settings(libraryDependencies += D.zio)
  .settings(libraryDependencies += D.munitZio)
  .dependsOn(models, protocolInvitation)

lazy val protocolRevocationNotification = project
  .in(file("mercury/protocol-revocation-notification"))
  .configure(commonConfigure)
  .settings(name := "mercury-protocol-revocation-notification")
  .settings(libraryDependencies += D.zio)
  .settings(libraryDependencies += D.munitZio)
  .dependsOn(models)

lazy val protocolPresentProof = project
  .in(file("mercury/protocol-present-proof"))
  .configure(commonConfigure)
  .settings(name := "mercury-protocol-present-proof")
  .settings(libraryDependencies += D.zio)
  .settings(libraryDependencies += D.munitZio)
  .dependsOn(models, protocolInvitation)

lazy val vc = project
  .in(file("mercury/vc"))
  .configure(commonConfigure)
  .settings(name := "mercury-verifiable-credentials")
  .dependsOn(protocolIssueCredential, protocolPresentProof) //TODO merge those two modules into this one

lazy val protocolTrustPing = project
  .in(file("mercury/protocol-trust-ping"))
  .configure(commonConfigure)
  .settings(name := "mercury-protocol-trust-ping")
  .settings(libraryDependencies += D.zio)
  .settings(libraryDependencies += D.munitZio)
  .dependsOn(models)

// ################
// ### Resolver ###
// ################

// TODO move stuff to the models module
lazy val resolver = project // maybe merge into models
  .in(file("mercury/resolver"))
  .configure(commonConfigure)
  .settings(name := "mercury-resolver")
  .settings(
    libraryDependencies ++= Seq(
      D.didcommx,
      D.peerDidcommx,
      D.munit,
      D.munitZio,
      D.nimbusJwt,
    ),
    testFrameworks += new TestFramework("munit.Framework")
  )
  .dependsOn(models)

// ##############
// ### Agents ###
// ##############

lazy val agent = project // maybe merge into models
  .in(file("mercury/agent"))
  .configure(commonConfigure)
  .settings(name := "mercury-agent-core")
  .settings(libraryDependencies ++= Seq(D.zioLog, D.zioSLF4J))
  .dependsOn(
    models,
    resolver,
    protocolCoordinateMediation,
    protocolInvitation,
    protocolRouting,
    // protocolMercuryMailbox,
    protocolLogin,
    protocolIssueCredential,
    protocolRevocationNotification,
    protocolPresentProof,
    vc,
    protocolConnection,
    protocolReportProblem,
    protocolTrustPing,
  )

/** agents implementation with didcommx */
lazy val agentDidcommx = project
  .in(file("mercury/agent-didcommx"))
  .configure(commonConfigure)
  .settings(name := "mercury-agent-didcommx")
  .settings(libraryDependencies += D.didcommx)
  .settings(libraryDependencies += D.munitZio)
  .dependsOn(agent) //modelsDidcommx

// ///** TODO Demos agents and services implementation with did-scala */
// lazy val agentDidScala =
//   project
//     .in(file("mercury/agent-did-scala"))
//     .settings(name := "mercury-agent-didscala")
//     .settings(skip / publish := true)
//     .dependsOn(agent)

// ####################
// ###  Prism Node ####
// ####################
val prismNodeClient = project
  .in(file("prism-node/client/scala-client"))
  .configure(commonConfigure)
  .settings(
    name := "prism-node-client",
    libraryDependencies ++= Seq(D.scalaPbGrpc, D.scalaPbRuntime, D.grpcOkHttp),
    coverageEnabled := false,
    // gRPC settings
    Compile / PB.targets := Seq(scalapb.gen() -> (Compile / sourceManaged).value / "scalapb"),
    Compile / PB.protoSources := Seq(
      baseDirectory.value / "api" / "grpc",
      baseDirectory.value / "src" / "main" / "protobuf" // scalapb package-wide codegen config (package.proto)
    )
  )

// #####################
// #####  castor  ######
// #####################

lazy val castorCore = project
  .in(file("castor"))
  .configure(commonConfigure)
  .settings(commonSetttings)
  .settings(
    name := "castor-core",
    libraryDependencies ++= D_Castor.coreDependencies
  )
  .dependsOn(shared, prismNodeClient)
  .dependsOn(sharedCrypto % "compile->compile;test->test")

// #####################
// #####  pollux  ######
// #####################

lazy val polluxVcJWT = project
  .in(file("pollux/vc-jwt"))
  .configure(commonConfigure)
  .settings(commonSetttings)
  .settings(
    name := "pollux-vc-jwt",
    libraryDependencies ++= D_Pollux_VC_JWT.polluxVcJwtDependencies
  )
  .dependsOn(castorCore, sharedJson)

lazy val polluxCore = project
  .in(file("pollux/core"))
  .configure(commonConfigure)
  .settings(commonSetttings)
  .settings(
    name := "pollux-core",
    libraryDependencies ++= D_Pollux.coreDependencies
  )
  .dependsOn(
    shared,
    castorCore % "compile->compile;test->test", // Test is for MockDIDService
    cloudAgentWalletAPI % "compile->compile;test->test", // Test is for MockManagedDIDService
    vc,
    resolver,
    agentDidcommx,
    eventNotification,
    polluxAnoncreds,
    polluxVcJWT,
    polluxSDJWT,
    polluxPreX % "compile->compile;test->test", // Test is for example resources
  )

lazy val polluxDoobie = project
  .in(file("pollux/sql-doobie"))
  .configure(commonConfigure)
  .settings(commonSetttings)
  .settings(
    name := "pollux-sql-doobie",
    libraryDependencies ++= D_Pollux.sqlDoobieDependencies
  )
  .dependsOn(polluxCore % "compile->compile;test->test")
  .dependsOn(shared)
  .dependsOn(sharedTest % "test->test")

lazy val polluxPreX = project
  .in(file("pollux/prex"))
  .settings(commonSetttings)
  .settings(name := "pollux-prex")
  .dependsOn(shared, sharedJson, polluxVcJWT)

// ########################
// ### Pollux Anoncreds ###
// ########################

lazy val polluxAnoncreds = project
  .in(file("pollux/anoncreds"))
  .configure(commonConfigure)
  .settings(
    name := "pollux-anoncreds",
    Compile / unmanagedJars += Attributed.blank(
      (fileConverter.value.toVirtualFile((baseDirectory.value / "anoncreds-jvm-1.0-SNAPSHOT.jar").toPath): xsbti.HashedVirtualFileRef)
    ),
    Compile / unmanagedResourceDirectories ++= Seq(
      baseDirectory.value / "native-lib" / "NATIVE"
    ),
    libraryDependencies ++= D_Pollux_AnonCreds.baseDependencies
  )

lazy val polluxAnoncredsTest = project
  .in(file("pollux/anoncredsTest"))
  .configure(commonConfigure)
  .settings(libraryDependencies += D.scalaTest)
  .dependsOn(polluxAnoncreds % "compile->test")

lazy val polluxSDJWT = project
  .in(file("pollux/sd-jwt"))
  .configure(commonConfigure)
  .settings(commonSetttings)
  .settings(
    name := "pollux-sd-jwt",
    libraryDependencies += "io.iohk.atala" % "sd-jwt-kmp-jvm" % "0.1.2"
  )
  .dependsOn(sharedCrypto)

// #####################
// #####  connect  #####
// #####################

lazy val connectCore = project
  .in(file("connect/core"))
  .configure(commonConfigure)
  .settings(commonSetttings)
  .settings(
    name := "connect-core",
    libraryDependencies ++= D_Connect.coreDependencies,
    Test / publishArtifact := true
  )
  .dependsOn(shared)
  .dependsOn(protocolConnection, protocolReportProblem, eventNotification)

lazy val connectDoobie = project
  .in(file("connect/sql-doobie"))
  .configure(commonConfigure)
  .settings(commonSetttings)
  .settings(
    name := "connect-sql-doobie",
    libraryDependencies ++= D_Connect.sqlDoobieDependencies
  )
  .dependsOn(shared)
  .dependsOn(sharedTest % "test->test")
  .dependsOn(connectCore % "compile->compile;test->test")

// ############################
// #### Event Notification ####
// ############################

lazy val eventNotification = project
  .in(file("event-notification"))
  .configure(commonConfigure)
  .settings(
    name := "event-notification",
    libraryDependencies ++= D_EventNotification.baseDependencies
  )
  .dependsOn(shared)

// #####################
// #### Cloud Agent ####
// #####################

lazy val cloudAgentWalletAPI = project
  .in(file("cloud-agent/service/wallet-api"))
  .configure(commonConfigure)
  .settings(commonSetttings)
  .settings(
    name := "cloud-agent-wallet-api",
    libraryDependencies ++=
      D_CloudAgent.keyManagementDependencies ++
        D_CloudAgent.iamDependencies ++
        D_CloudAgent.postgresDependencies ++
        Seq(D.zioMock)
  )
  .dependsOn(
    agentDidcommx,
    castorCore,
    eventNotification
  )
  .dependsOn(sharedTest % "test->test")
  .dependsOn(sharedCrypto % "compile->compile;test->test")

lazy val cloudAgentVdr = project
  .in(file("cloud-agent/service/vdr"))
  .configure(commonConfigure)
  .settings(commonSetttings)
  .settings(
    name := "cloud-agent-vdr",
    libraryDependencies ++= D_CloudAgent.baseDependencies ++ D_CloudAgent.vdrDependencies,
  )
  .dependsOn(shared, prismNodeClient, vdrCore, vdrPrismNode, vdrNeoprism, vdrDatabase, vdrMemory, vdrProxy)

lazy val vdrCore = project
  .in(file("vdr/core"))
  .configure(commonConfigure)
  .settings(commonSetttings)
  .settings(
    name := "vdr-core",
    libraryDependencies ++= D_CloudAgent.vdrDependencies,
  )
  .dependsOn(shared, prismNodeClient)

lazy val vdrMemory = project
  .in(file("vdr/memory"))
  .configure(commonConfigure)
  .settings(commonSetttings)
  .settings(
    name := "vdr-memory",
    libraryDependencies ++= D_CloudAgent.vdrDependencies,
  )
  .dependsOn(vdrCore)

lazy val vdrPrismNode = project
  .in(file("vdr/prism-node"))
  .configure(commonConfigure)
  .settings(commonSetttings)
  .settings(
    name := "vdr-prism-node",
    libraryDependencies ++= D_CloudAgent.vdrDependencies,
  )
  .dependsOn(vdrCore, prismNodeClient, shared % "compile->compile;test->test")

lazy val vdrNeoprism = project
  .in(file("vdr/neoprism"))
  .configure(commonConfigure)
  .settings(commonSetttings)
  .settings(
    name := "vdr-neoprism",
    libraryDependencies ++= D_CloudAgent.vdrDependencies,
  )
  .dependsOn(vdrCore, castorCore, shared % "compile->compile;test->test")

lazy val vdrDatabase = project
  .in(file("vdr/database"))
  .configure(commonConfigure)
  .settings(commonSetttings)
  .settings(
    name := "vdr-database",
    libraryDependencies ++= D_CloudAgent.vdrDependencies ++ D_CloudAgent.postgresDependencies,
    Test / libraryDependencies ++= Seq(
      "com.dimafeng" %% "testcontainers-scala-postgresql" % V.testContainersScala % Test
    ),
  )
  .dependsOn(vdrCore, shared)

lazy val vdrBlockfrost = project
  .in(file("vdr/blockfrost"))
  .configure(commonConfigure)
  .settings(commonSetttings)
  .settings(
    name := "vdr-blockfrost",
    libraryDependencies ++= D_CloudAgent.vdrDependencies,
  )
  .dependsOn(vdrCore, shared)

lazy val vdrProxy = project
  .in(file("vdr/proxy"))
  .configure(commonConfigure)
  .settings(commonSetttings)
  .settings(
    name := "vdr-proxy",
    libraryDependencies ++= D_CloudAgent.vdrDependencies ++ Seq(
      "com.h2database" % "h2" % "2.2.224"
    ),
    Test / libraryDependencies += "com.h2database" % "h2" % "2.2.224" % Test
  )
  .dependsOn(vdrCore, vdrPrismNode, vdrNeoprism, vdrMemory, vdrDatabase, vdrBlockfrost, shared % "compile->compile;test->test")

lazy val cloudAgentServer = project
  .in(file("cloud-agent/service/server"))
  .configure(commonConfigure)
  .settings(commonSetttings)
  .settings(
    name := "identus-cloud-agent",
    fork := true,
    libraryDependencies ++= D_CloudAgent.serverDependencies,
    excludeDependencies ++= Seq(
      // Exclude `protobuf-javalite` from all dependencies since we're using scalapbRuntime which already include `protobuf-java`
      // Having both may introduce conflict on some api https://github.com/protocolbuffers/protobuf/issues/8104
      ExclusionRule("com.google.protobuf", "protobuf-javalite")
    ),
    Compile / mainClass := Some("org.hyperledger.identus.agent.server.MainApp"),
    Docker / maintainer := "atala-coredid@iohk.io", // TODO: clarify the contact emale of the project
    Docker / dockerUsername := Some("hyperledgeridentus"), // https://hub.docker.com/u/hyperledgeridentus
    Docker / dockerRepository := Some("docker.io"),
    dockerExposedPorts := Seq(8085, 8090),
    dockerBaseImage := "eclipse-temurin:22-jdk-ubi9-minimal",
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoPackage := "org.hyperledger.identus.agent.server.buildinfo",
    Compile / packageDoc / publishArtifact := false
  )
  .enablePlugins(JavaAppPackaging, DockerPlugin)
  .enablePlugins(BuildInfoPlugin)
  .dependsOn(cloudAgentWalletAPI % "compile->compile;test->test")
  .dependsOn(
    sharedTest % "test->test",
    agent,
    polluxCore % "compile->compile;test->test",
    polluxDoobie,
    polluxAnoncreds,
    connectCore % "compile->compile;test->test", // Test is for MockConnectionService
    connectDoobie,
    castorCore,
    eventNotification,
    cloudAgentVdr,
  )

// ############################
// ####  Release process  #####
// ############################
import sbtrelease.ReleasePlugin.autoImport.ReleaseTransformations.*
releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  ReleaseStep(releaseStepTask(cloudAgentServer / Docker / stage)),
  setNextVersion
)

lazy val aggregatedProjects: Seq[ProjectReference] = Seq(
  shared,
  sharedJson,
  sharedCrypto,
  sharedTest,
  models,
  protocolConnection,
  protocolCoordinateMediation,
  protocolDidExchange,
  protocolInvitation,
  // protocolMercuryMailbox,
  protocolLogin,
  protocolReportProblem,
  protocolRouting,
  protocolIssueCredential,
  protocolRevocationNotification,
  protocolPresentProof,
  vc,
  protocolTrustPing,
  resolver,
  agent,
  agentDidcommx,
  castorCore,
  polluxVcJWT,
  polluxCore,
  polluxDoobie,
  polluxAnoncreds,
  polluxAnoncredsTest,
  polluxSDJWT,
  polluxPreX,
  connectCore,
  connectDoobie,
  vdrCore,
  vdrBlockfrost,
  vdrMemory,
  vdrPrismNode,
  vdrNeoprism,
  vdrDatabase,
  vdrProxy,
  cloudAgentVdr,
  cloudAgentWalletAPI,
  cloudAgentServer,
  eventNotification,
)

lazy val root = project
  .in(file("."))
  .aggregate(aggregatedProjects: _*)

Global / excludeLintKeys ++= Set(
  vdrDatabase / Test / libraryDependencies,
  vdrProxy / Test / libraryDependencies
)
