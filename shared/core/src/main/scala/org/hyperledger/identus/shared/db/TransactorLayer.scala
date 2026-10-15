package org.hyperledger.identus.shared.db

import cats.effect.kernel.Resource
import cats.effect.std.Dispatcher
import cats.effect.Async
import com.zaxxer.hikari.HikariConfig
import doobie.hikari.HikariTransactor
import doobie.util.transactor.Transactor
import zio.*
import zio.interop.catz.*

object TransactorLayer {

  def task: RLayer[DbConfig, HikariTransactor[Task]] = {
    ZLayer.fromZIO {
      ZIO.service[DbConfig].flatMap { config =>
        // Here we use `Dispatcher.apply`
        // but at the agent level it is `Dispatcher.parallel` due to evicted version
        // Dispatcher.parallel[Task].allocated.map { case (dispatcher, _) =>
        Dispatcher.parallel[Task].allocated.map { case (dispatcher, _) =>
          given Dispatcher[Task] = dispatcher
          TransactorLayer.hikari[Task](config)
        }
      }
    }.flatten
  }

  def contextAwareTask: RLayer[DbConfig, HikariTransactor[ContextAwareTask]] = {
    ZLayer.fromZIO {
      ZIO.service[DbConfig].flatMap { config =>
        // Here we use `Dispatcher.apply`
        // but at the agent level it is `Dispatcher.parallel` due to evicted version
        // Dispatcher.parallel[ContextAwareTask].allocated.map { case (dispatcher, _) =>
        Dispatcher.parallel[ContextAwareTask].allocated.map { case (dispatcher, _) =>
          given Dispatcher[ContextAwareTask] = dispatcher
          TransactorLayer.hikari[ContextAwareTask](config)
        }
      }
    }.flatten
  }

  def hikari[A[_]: Async: Dispatcher](
      config: DbConfig
  )(using tag: Tag[HikariTransactor[A]]): TaskLayer[HikariTransactor[A]] = {
    val transactorLayerZio = ZIO
      .attempt {
        // https://github.com/brettwooldridge/HikariCP/wiki/About-Pool-Sizing
        val poolSize = (config.awaitConnectionThreads * 2) + 1
        val hikariConfig = makeHikariConfig(config)
        hikariConfig.setPoolName("DBPool")
        hikariConfig.setLeakDetectionThreshold(300000) // 5 mins
        hikariConfig.setMinimumIdle(poolSize)
        hikariConfig.setMaximumPoolSize(poolSize) // Both Pool size amd Minimum Idle should same and is recommended
        hikariConfig
      }
      .map { hikariConfig =>
        val pool: Resource[A, HikariTransactor[A]] = HikariTransactor.fromHikariConfig[A](hikariConfig)
        pool.toManaged.toLayer[HikariTransactor[A]]
      }

    ZLayer.fromZIO(transactorLayerZio).flatten
  }

  private def makeHikariConfig(config: DbConfig): HikariConfig = {
    val hikariConfig = HikariConfig()

    hikariConfig.setJdbcUrl(config.jdbcUrl)
    hikariConfig.setUsername(config.username)
    hikariConfig.setPassword(config.password)
    hikariConfig.setAutoCommit(false)

    hikariConfig.setDriverClassName("org.postgresql.Driver")
    hikariConfig.addDataSourceProperty("cachePrepStmts", "true")
    hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250")
    hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048")

    hikariConfig
  }

}
