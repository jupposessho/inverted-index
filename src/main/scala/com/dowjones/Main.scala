package com.dowjones

import cats.effect.IOApp

object Main extends IOApp {
  def run(args: List[String]) =
    Server.create()
}
