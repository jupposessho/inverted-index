package com.dowjones.service

import cats.effect.IO
import com.dowjones.repository.{ DictionaryRepository, IndexRepository }
import com.dowjones.service.WordResolverService.{ WordNotFoundInDictionary, WordNotFoundInDocuments }
import org.specs2.mutable.Specification

class WordResolverServiceSpec extends Specification {

  "WordResolverService.find should" >> {

    "return the resolved document ids" >> {
      implicit val indexRepository = IndexRepository[IO]
      implicit val dictionaryRepository = DictionaryRepository[IO]
      WordResolverService[IO].find("the").unsafeRunSync() must beEqualTo(IndexRepository.db.table(1))
    }

    "return error when word not found in dictionary" >> {
      implicit val indexRepository = IndexRepository[IO]
      implicit val dictionaryRepository = DictionaryRepository[IO]
      WordResolverService[IO].find("notFound").attempt.unsafeRunSync() must beLeft(WordNotFoundInDictionary)
    }

    "return error when word not found in documents" >> {
      implicit val indexRepository = IndexRepository[IO]
      implicit val dictionaryRepository = DictionaryRepository[IO]
      WordResolverService[IO].find("notInDocuments").attempt.unsafeRunSync() must beLeft(WordNotFoundInDocuments)
    }
  }
}
