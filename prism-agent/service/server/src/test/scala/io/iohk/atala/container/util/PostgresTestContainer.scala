package io.iohk.atala.container.util

import cats.effect.std.Dispatcher
import cats.effect.Resource
import com.dimafeng.testcontainers.PostgreSQLContainer
import com.zaxxer.hikari.HikariConfig
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import doobie.util.transactor.Transactor
import org.testcontainers.containers.output.OutputFrame
import org.testcontainers.utility.DockerImageName
import zio.*
import zio.ZIO.*
import zio.interop.catz.*

import java.util.function.Consumer

object PostgresTestContainer {

  def postgresLayer(
      imageName: Option[String] = Some("postgres"),
      verbose: Boolean = false
  ): ZLayer[Any, Nothing, PostgreSQLContainer] =
    ZLayer.scoped {
      acquireRelease(ZIO.attemptBlockingIO {
        val container = new PostgreSQLContainer(
          dockerImageNameOverride = imageName.map(DockerImageName.parse)
        )

        if (verbose) {
          container.container
            .withLogConsumer(new Consumer[OutputFrame] {
              override def accept(t: OutputFrame): Unit = println(t.getUtf8String)
            })
          container.container
            .withCommand("postgres", "-c", "log_statement=all", "-c", "log_destination=stderr")
        }

        container.start()
        container
      }.orDie)(container => attemptBlockingIO(container.stop()).orDie)
    }

  private def hikariConfig(container: PostgreSQLContainer): HikariConfig = {
    val config = HikariConfig()
    config.setJdbcUrl(container.jdbcUrl)
    config.setUsername(container.username)
    config.setPassword(container.password)
    config
  }

  lazy val hikariConfigLayer: ZLayer[PostgreSQLContainer, Nothing, HikariConfig] =
    ZLayer.fromZIO {
      for {
        container <- ZIO.service[PostgreSQLContainer]
      } yield hikariConfig(container)
    }

  def transactor: ZLayer[HikariConfig, Throwable, Transactor[Task]] = ZLayer.fromZIO {
    val hikariTransactorLayerZIO = for {
      config <- ZIO.service[HikariConfig]
      htxResource: Resource[Task, HikariTransactor[Task]] = for {
        ec <- ExecutionContexts.cachedThreadPool[Task]
        xa <- HikariTransactor.fromHikariConfig[Task](config, ec)
      } yield xa
      layer <- Dispatcher.parallel[Task].allocated.map {
        case (dispatcher, _) => {
          given Dispatcher[Task] = dispatcher
          htxResource.toManaged.toLayer[Transactor[Task]]
        }
      }
    } yield layer
    hikariTransactorLayerZIO
  }.flatten

}
