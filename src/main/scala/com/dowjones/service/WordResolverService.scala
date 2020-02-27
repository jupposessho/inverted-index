package com.dowjones.service

import cats.implicits._
import com.dowjones.repository.{ DictionaryRepository, IndexRepository }
import cats.MonadError
import com.dowjones.model.DictionaryItem

import scala.util.control.NoStackTrace

trait WordResolverService[F[_]] {
  def find(word: String): F[List[String]]
}

object WordResolverService {

  sealed trait ApplicationError extends NoStackTrace
  final case object WordNotFoundInDictionary extends ApplicationError
  final case object WordNotFoundInDocuments extends ApplicationError

  def apply[F[_]: DictionaryRepository: IndexRepository](implicit M: MonadError[F, Throwable]) = new WordResolverService[F] {

    def find(word: String): F[List[String]] =
      for {
        maybeDictionaryItem <- DictionaryRepository[F].find(word)
        dictionaryItem <- validateItem(maybeDictionaryItem)
        documents <- M.ensure(IndexRepository[F].find(dictionaryItem.id))(WordNotFoundInDocuments)(_.nonEmpty)
      } yield documents

    private def validateItem(mItem: Option[DictionaryItem]): F[DictionaryItem] = mItem match {
      case None        => M.raiseError[DictionaryItem](WordNotFoundInDictionary)
      case Some(dItem) => dItem.pure[F]
    }
  }
}
