package com.dowjones.repository

import cats.effect.IO
import org.specs2.mutable.Specification

class IndexRepositorySpec extends Specification {

  import IndexRepository.db

  "find should" >> {
    "return the found document ids" >> {
      IndexRepository[IO].find(1).unsafeRunSync must beEqualTo(db.table(1))
    }

    "return empty list if item does not exist" >> {
      IndexRepository[IO].find(99).unsafeRunSync must beEqualTo(Nil)
    }
  }
}
