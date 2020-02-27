package com.dowjones.repository

import cats.Applicative
import cats.implicits._
import com.dowjones.model.DictionaryItem

trait DictionaryRepository[F[_]] {
  def list(): F[List[DictionaryItem]]
  def find(word: String): F[Option[DictionaryItem]]
}

object DictionaryRepository {

  object db {

    val table = List(
      DictionaryItem(1, "the"),
      DictionaryItem(2, "dictionary"),
      DictionaryItem(3, "notindocuments")
    )
  }

  def apply[F[_]: Applicative] = new DictionaryRepository[F] {

    def list(): F[List[DictionaryItem]] = db.table.pure[F]

    def find(word: String): F[Option[DictionaryItem]] = db.table.find(_.word == word.toLowerCase).pure[F]
  }
}
