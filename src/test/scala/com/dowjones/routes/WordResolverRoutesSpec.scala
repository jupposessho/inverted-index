package com.dowjones.routes

import cats.MonadError
import cats.effect.IO
import com.dowjones.repository.IndexRepository
import com.dowjones.service.WordResolverService
import com.dowjones.service.WordResolverService.{ ApplicationError, WordNotFoundInDictionary, WordNotFoundInDocuments }
import org.http4s.{ Method, Request, Response, Status }
import org.http4s._
import org.http4s.implicits._
import org.http4s.headers._
import org.specs2.mutable.Specification

class WordResolverRoutesSpec extends Specification {

  "GET /v1/resolver/words/:word" >> {
    "when service return list of records" >> {

      "return 200" >> {
        listResponse().status must beEqualTo(Status.Ok)
      }

      "return Json" >> {
        listResponse().contentType.get must beEqualTo(`Content-Type`(MediaType.application.json))
      }

      "return proper document ids" >> {
        listResponse().as[String].unsafeRunSync() must beEqualTo("""["0","2","12"]""")
      }
    }

    "when service return WordNotFoundInDictionary" >> {
      "return 404" >> {
        errorResponse(WordNotFoundInDictionary).status must beEqualTo(Status.NotFound)
      }
    }

    "when service return WordNotFoundInDocuments" >> {
      "return 500" >> {
        errorResponse(WordNotFoundInDocuments).status must beEqualTo(Status.InternalServerError)
      }
    }
  }

  private def listResponse(result: List[String] = IndexRepository.db.table(1)): Response[IO] = {
    val listRequest = Request[IO](Method.GET, uri"/v1/resolver/words/the")
    val service = new WordResolverService[IO] {
      def find(word: String): IO[List[String]] =
        if (word == "the") IO.pure(result)
        else MonadError[IO, Throwable].raiseError(new Exception("Wrong parameter passed to WordResolverService.find"))
    }

    WordResolverRoutes.wordResolverRoutes(service).orNotFound(listRequest).unsafeRunSync()
  }

  private def errorResponse(error: ApplicationError): Response[IO] = {
    val listRequest = Request[IO](Method.GET, uri"/v1/resolver/words/???")
    val service = new WordResolverService[IO] {
      def find(word: String): IO[List[String]] = MonadError[IO, Throwable].raiseError(error)
    }

    WordResolverRoutes.wordResolverRoutes(service).orNotFound(listRequest).unsafeRunSync()
  }
}
