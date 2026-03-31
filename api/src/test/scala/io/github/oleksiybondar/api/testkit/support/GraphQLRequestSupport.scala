package io.github.oleksiybondar.api.testkit.support

import cats.effect.IO
import io.circe.Json
import org.http4s.circe.CirceEntityCodec._
import org.http4s.headers.Authorization
import org.http4s.implicits._
import org.http4s.{AuthScheme, Credentials, Method, Request}

object GraphQLRequestSupport {

  def graphqlRequest(query: String, accessToken: Option[String] = None): Request[IO] = {
    val baseRequest =
      Request[IO](method = Method.POST, uri = uri"/graphql")
        .withEntity(
          Json.obj(
            "query" -> Json.fromString(query)
          )
        )

    accessToken match {
      case Some(token) =>
        baseRequest.putHeaders(
          Authorization(Credentials.Token(AuthScheme.Bearer, token))
        )
      case None        =>
        baseRequest
    }
  }
}
