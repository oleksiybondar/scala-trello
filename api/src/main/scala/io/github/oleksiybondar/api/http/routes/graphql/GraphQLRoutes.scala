package io.github.oleksiybondar.api.http.routes.graphql

import cats.effect.IO
import io.circe.{Decoder, Json}
import io.github.oleksiybondar.api.http.middleware.AuthMiddleware
import org.http4s.circe.CirceEntityCodec._
import org.http4s.circe.jsonOf
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, HttpRoutes, Request, Response}
import sangria.execution.{ErrorWithResolver, Executor, QueryAnalysisError}
import sangria.marshalling.circe._
import sangria.parser.QueryParser

import scala.concurrent.ExecutionContext

object GraphQLRoutes extends Http4sDsl[IO] {

  given ExecutionContext = ExecutionContext.global

  final case class GraphQLRequest(
      query: String,
      operationName: Option[String],
      variables: Option[Json]
  )

  given Decoder[GraphQLRequest] =
    Decoder.forProduct3("query", "operationName", "variables")(GraphQLRequest.apply)

  private given EntityDecoder[IO, GraphQLRequest] = jsonOf[IO, GraphQLRequest]

  def routes(context: GraphQLContext): IO[HttpRoutes[IO]] =
    IO.pure(
      HttpRoutes.of[IO] { case request @ POST -> Root =>
        request
          .as[GraphQLRequest]
          .flatMap(executeQuery(context, request, _))
      }
    )

  private def executeQuery(
      context: GraphQLContext,
      httpRequest: Request[IO],
      request: GraphQLRequest
  ): IO[Response[IO]] =
    parseQuery(request.query).flatMap { queryAst =>
      val requestContext = context.copy(
        currentUserId = AuthMiddleware.extractAuthenticatedUserId(httpRequest)
      )

      val result =
        Executor
          .execute(
            schema = GraphQLSchema.schema,
            queryAst = queryAst,
            userContext = requestContext,
            variables = request.variables.getOrElse(Json.obj()),
            operationName = request.operationName
          )
          .recover {
            case error: QueryAnalysisError => error.resolveError
            case error: ErrorWithResolver  => error.resolveError
          }

      IO.fromFuture(IO(result)).flatMap(Ok(_))
    }

  private def parseQuery(query: String) =
    IO.fromEither(
      QueryParser
        .parse(query)
        .toEither
        .left
        .map(error => new IllegalArgumentException(error.getMessage))
    )
}
