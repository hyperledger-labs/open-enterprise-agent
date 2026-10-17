addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.13.2")
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.6.2")
addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.14.7")
addSbtPlugin("com.github.sbt" % "sbt-release" % "1.5.0")
addSbtPlugin("com.github.sbt" % "sbt-native-packager" % "1.12.0")
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "2.4.4")
addSbtPlugin("com.thesamet" % "sbt-protoc" % "1.1.0-RC2")
addSbtPlugin("com.github.sbt" % "sbt-license-report" % "1.10.0")

// scalapb 1.0.0-alpha.x is required for sbt 2.x: the 0.11.x _3 POM hardcodes _2.13 transitive deps
// (protoc-gen, protoc-cache-coursier) which pull protoc-bridge_2.13 and conflict with sbt-protoc's
// protoc-bridge_3. See https://github.com/scalapb/ScalaPB/releases/tag/v1.0.0-alpha.5
// ("Fix compilerplugin artifact for sbt 2")
libraryDependencies ++= Seq("com.thesamet.scalapb" %% "compilerplugin" % "1.0.0-alpha.6")

// NOTE: `sbt-coveralls` (org.scoverage) was removed: it has no published artifact for sbt 2.x
// (`_sbt2_3`) and the project is unmaintained. Coverage is now uploaded via the
// `coverallsapp/github-action` (cobertura format) in .github/workflows/unit-tests.yml.
// NOTE: `sbt-github-packages` (com.codecommit) was removed: it has no sbt 2.x artifact and was only
// referenced by a commented-out resolver in build.sbt.

// See file in .github/workflows/sbt-dependency-submission.yml
if (sys.env.get("DEPEDABOT").isDefined) {
  println(s"Adding plugin sbt-github-dependency-submission since env DEPEDABOT is defined.")
  // The reason for this is that the plugin needs the variable to be defined. We don't want to have that requirement.
  libraryDependencies += {
    val dependency = "ch.epfl.scala" % "sbt-github-dependency-submission" % "3.2.3"
    val sbtV = (pluginCrossBuild / sbtBinaryVersion).value
    val scalaV = (update / scalaBinaryVersion).value
    Defaults.sbtPluginExtra(dependency, sbtV, scalaV)
  }
} else libraryDependencies ++= Seq[ModuleID]()
