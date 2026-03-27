package io.github.oleksiybondar.api.http.docs.graphql

import cats.effect.Async
import org.http4s.HttpRoutes
import sttp.tapir._
import sttp.tapir.server.http4s.Http4sServerInterpreter

object GraphiQLRoutes {

  val graphiqlEndpoint: PublicEndpoint[Unit, Unit, String, Any] =
    endpoint.get
      .in("docs" / "graphql")
      .out(htmlBodyUtf8)
      .name("graphiql")
      .description("GraphiQL IDE page")
      .tag("graphql")

  val all =
    List(graphiqlEndpoint)

  def routes[F[_]: Async]: HttpRoutes[F] =
    Http4sServerInterpreter[F]().toRoutes(
      graphiqlServerEndpoint[F]
    )

  private def graphiqlServerEndpoint[F[_]: Async] =
    graphiqlEndpoint.serverLogicSuccess[F] { _ =>
      loadResource[F]("static/graphiql.html")
    }

  private def loadResource[F[_]: Async](path: String): F[String] =
    Async[F].blocking {
      val stream =
        Option(getClass.getClassLoader.getResourceAsStream(path))
          .getOrElse(throw new RuntimeException(s"Resource not found: $path"))

      try scala.io.Source.fromInputStream(stream, "UTF-8").mkString
      finally stream.close()
    }
}
