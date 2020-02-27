package com.dowjones.config

import cats.effect.IO
import com.typesafe.config.ConfigFactory
import pureconfig._
import pureconfig.generic.auto._
import pureconfig.module.catseffect.syntax._

object Config {

  final case class ServerConfig(host: String, port: Int)
  final case class DatabaseConfig(
      driver: String,
      url: String,
      user: String,
      password: String,
      threadPoolSize: Int)
  final case class AppConfig(server: ServerConfig, database: DatabaseConfig)

  def load(configFile: String = "application.conf"): IO[AppConfig] = {
    ConfigSource.fromConfig(ConfigFactory.load(configFile)).loadF[IO, AppConfig]
  }
}
