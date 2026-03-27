package io.github.oleksiybondar.api.http.routes.graphql

import cats.effect.IO
import cats.syntax.all.*
import io.circe.Decoder
import io.circe.Json
import org.http4s.EntityDecoder
import org.http4s.HttpRoutes
import org.http4s.Response
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.circe.jsonOf
import org.http4s.dsl.Http4sDsl
import sangria.execution.ErrorWithResolver
import sangria.execution.Executor
import sangria.execution.QueryAnalysisError
import sangria.marshalling.circe.*
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
          .flatMap(executeQuery(context, _))
      }
    )

  private def executeQuery(
    context: GraphQLContext,
    request: GraphQLRequest
  ): IO[Response[IO]] =
    parseQuery(request.query).flatMap { queryAst =>
      val result =
        Executor
          .execute(
            schema = GraphQLSchema.schema,
            queryAst = queryAst,
            userContext = context,
            variables = request.variables.getOrElse(Json.obj()),
            operationName = request.operationName
          )
          .recover {
            case error: QueryAnalysisError => error.resolveError
            case error: ErrorWithResolver => error.resolveError
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
