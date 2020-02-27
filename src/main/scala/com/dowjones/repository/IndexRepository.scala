package com.dowjones.repository

import cats.Applicative
import cats.implicits._

trait IndexRepository[F[_]] {
  def find(wordId: Int): F[List[String]]
}

object IndexRepository {

  object db {

    val table = Map(
      1 -> List("0", "2", "12"),
      2 -> List("2", "3"),
    )
  }

  def apply[F[_]: Applicative] = new IndexRepository[F] {

    def find(wordId: Int): F[List[String]] = db
      .table
      .get(wordId)
      .fold(List.empty[String])(identity)
      .pure[F]
  }
}
