import sbt.internal.librarymanagement.{IvyRetrieve, IvySbt}
import sbt.librarymanagement._
import sbt.librarymanagement.ivy._
import sbt.Logger

// Since ivy fails to resolve project dependencies, customized version is used to ignore any failure.
// This is OK as we only grab license information from the resolution metadata,
// and the failing dependencies are only used in 'Test' configuration.
// This should be used until 'sbt-license-report' plugin use coursier to populate licenses.
//
// https://github.com/sbt/sbt-license-report/issues/47
// https://github.com/sbt/sbt-license-report/issues/87
class LicenseReportCustomDependencyResolution(ivyConfiguration: IvyConfiguration, ivyModule: IvySbt#Module)
    extends DependencyResolutionInterface {

  private val ivyResolution = IvyDependencyResolution(ivyConfiguration)
  private val dummyFile = java.io.File.createTempFile("sbt-license-report", "")

  override def moduleDescriptor(moduleSetting: ModuleDescriptorConfiguration): ModuleDescriptor =
    ivyResolution.moduleDescriptor(moduleSetting)

  // Resolve using low-level ivy directly to skip sbt-wrapped ivy failing the resolution and discard the UpdateReport.
  // https://github.com/sbt/sbt-license-report/blob/5a8cb0b6567789bd8867e709b0cad8bb93aca50f/src/main/scala/sbtlicensereport/license/LicenseReport.scala#L221
  override def update(
      module: ModuleDescriptor,
      configuration: UpdateConfiguration,
      uwconfig: UnresolvedWarningConfiguration,
      log: Logger
  ): Either[UnresolvedWarning, UpdateReport] = {
    val (resolveReport, err) = ivyModule.withModule(Logger.Null) { (ivy, desc, default) =>
      import org.apache.ivy.core.resolve.ResolveOptions
      val resolveOptions = new ResolveOptions
      val resolveId = ResolveOptions.getDefaultResolveId(desc)
      resolveOptions.setResolveId(resolveId)
      import org.apache.ivy.core.LogOptions.LOG_QUIET
      resolveOptions.setLog(LOG_QUIET)
      val resolveReport = ivy.resolve(desc, resolveOptions)
      val err =
        if (resolveReport.hasError) {
          val messages = resolveReport.getAllProblemMessages.toArray.map(_.toString).distinct
          val failed = resolveReport.getUnresolvedDependencies.map(node => IvyRetrieve.toModuleID(node.getId))
          Some(new ResolveException(messages, failed))
        } else None
      (resolveReport, err)
    }

    err.foreach { resolveException =>
      log.warn(":::::::::::::::::::::::::::::::::::::::::::::::::::")
      log.warn("::     LicenseReport Unresolved Dependencies     ::")
      log.warn(":::::::::::::::::::::::::::::::::::::::::::::::::::")
      resolveException.failed
        .map(_.toString())
        .distinct
        .sorted
        .foreach { module => log.warn(s":: $module") }
      log.warn(":::::::::::::::::::::::::::::::::::::::::::::::::::")
    }

    val updateReport = IvyRetrieve.updateReport(resolveReport, dummyFile)
    Right(updateReport)
  }
}
