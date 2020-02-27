package com.dowjones

import cats.effect.{ ConcurrentEffect, ContextShift, ExitCode, IO, Resource, Timer }
import cats.implicits._
import com.dowjones.config.Config
import com.dowjones.config.Config.AppConfig
import com.dowjones.db.Database
import com.dowjones.repository.{ DictionaryRepository, IndexRepository }
import com.dowjones.routes.WordResolverRoutes
import com.dowjones.service.WordResolverService
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import fs2.Stream
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.Logger

object Server {

  def create(
      configFile: String = "application.conf"
    )(implicit
      contextShift: ContextShift[IO],
      concurrentEffect: ConcurrentEffect[IO],
      timer: Timer[IO]
    ): IO[ExitCode] = {
    resources(configFile).use { res =>
      stream[IO](res).compile.drain.as(ExitCode.Success)
    }
  }

  private def stream[F[_]: ConcurrentEffect: Timer: ContextShift](resources: Resources): Stream[F, Nothing] = {
    implicit val dictionaryRepository = DictionaryRepository[F]
    implicit val indexRepository = IndexRepository[F]
    val wordResolverService = WordResolverService[F]

    val httpApp = (
      WordResolverRoutes.wordResolverRoutes[F](wordResolverService)
    ).orNotFound

    val loggedHttpApp = Logger.httpApp(true, true)(httpApp)

    BlazeServerBuilder[F]
      .bindHttp(resources.config.server.port, resources.config.server.host)
      .withHttpApp(loggedHttpApp)
      .serve
  }.drain

  private def resources(configFile: String)(implicit contextShift: ContextShift[IO]): Resource[IO, Resources] = {
    for {
      config <- Resource.liftF(Config.load(configFile))
      ec <- ExecutionContexts.fixedThreadPool[IO](config.database.threadPoolSize)
      transactor <- Database.transactor(config.database, ec)
    } yield Resources(transactor, config)
  }

  final case class Resources(transactor: HikariTransactor[IO], config: AppConfig)
}
