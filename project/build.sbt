// Using unreleased plugin from 'main' which accepts DependencyResolution interface for building license report.
lazy val sbtLicenseReportPlugin = ProjectRef(
  uri("https://github.com/sbt/sbt-license-report.git#9675cedb19c794de1119cbcf46a255fc8dcd5d4e"),
  "sbt-license-report"
)

lazy val root = (project in file(".")).dependsOn(sbtLicenseReportPlugin)
