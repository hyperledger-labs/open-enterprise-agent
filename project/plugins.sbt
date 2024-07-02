addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.12.0")
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.5.2")
addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.12.1")
addSbtPlugin("com.github.sbt" % "sbt-release" % "1.4.0")
addSbtPlugin("com.codecommit" % "sbt-github-packages" % "0.5.3")
addSbtPlugin("com.github.sbt" % "sbt-native-packager" % "1.10.0")
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "2.0.12")
addSbtPlugin("org.scoverage" % "sbt-coveralls" % "1.3.11")
addSbtPlugin("com.thesamet" % "sbt-protoc" % "1.0.7")

// In order to import proper version of com.google.protobuf.ByteString we need to add this dependency
libraryDependencies ++= Seq("com.thesamet.scalapb" %% "compilerplugin" % "0.11.17")
