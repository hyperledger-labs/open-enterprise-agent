addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.11.0")
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.5.0")
addSbtPlugin("com.github.sbt" % "sbt-release" % "1.1.0")
addSbtPlugin("com.codecommit" % "sbt-github-packages" % "0.5.3")
addSbtPlugin("com.github.sbt" % "sbt-native-packager" % "1.9.11")
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "2.0.6")
addSbtPlugin("org.scoverage" % "sbt-coveralls" % "1.3.9")
addSbtPlugin("com.thesamet" % "sbt-protoc" % "1.0.6")

// In order to import proper version of com.google.protobuf.ByteString we need to add this dependency
libraryDependencies ++= Seq("com.thesamet.scalapb" %% "compilerplugin" % "0.11.15")
