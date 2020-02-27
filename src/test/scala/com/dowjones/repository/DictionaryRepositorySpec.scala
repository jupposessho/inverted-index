package com.dowjones.repository

import cats.effect.IO
import com.dowjones.model.DictionaryItem
import org.specs2.mutable.Specification

class DictionaryRepositorySpec extends Specification {

  "list should" >> {
    "return list of records" >> {
      DictionaryRepository[IO].list().unsafeRunSync must beEqualTo(DictionaryRepository.db.table)
    }
  }

  "find should" >> {
    "return the found directory item" >> {
      DictionaryRepository[IO].find("the").unsafeRunSync must beSome(DictionaryItem(1, "the"))
    }

    "find words in different case" >> {
      DictionaryRepository[IO].find("ThE").unsafeRunSync must beSome(DictionaryItem(1, "the"))
    }

    "return None if item does not exist" >> {
      DictionaryRepository[IO].find("notFound").unsafeRunSync must beNone
    }
  }
}
