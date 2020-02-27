package com.dowjones.routes

import cats.effect.Sync
import cats.implicits._
import com.dowjones.service.WordResolverService
import com.dowjones.service.WordResolverService.{ WordNotFoundInDictionary, WordNotFoundInDocuments }
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl.Http4sDsl
import org.http4s.HttpRoutes

object WordResolverRoutes {

  def wordResolverRoutes[F[_]: Sync](service: WordResolverService[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._

    HttpRoutes.of[F] {
      case GET -> Root / "v1" / "resolver" / "words" / word =>
        (for {
          result <- service.find(word)
          resp <- Ok(result)
        } yield resp).handleErrorWith {
          case WordNotFoundInDictionary => NotFound("WordNotFoundInDictionary")
          case WordNotFoundInDocuments  => InternalServerError("WordNotFoundInDocuments")
        }
    }
  }
}
