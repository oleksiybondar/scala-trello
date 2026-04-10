package io.github.oleksiybondar.api.http.docs.graphql

import cats.effect.Async
import io.github.oleksiybondar.api.http.TapirSupport
import org.http4s.HttpRoutes
import sttp.tapir._

object GraphiQLRoutes {

  val graphiqlEndpoint: PublicEndpoint[Unit, Unit, String, Any] =
    endpoint.get
      .in("docs" / "graphql")
      .out(htmlBodyUtf8)
      .name("graphiql")
      .description("GraphiQL IDE page")
      .tag("graphql")

  val all: List[PublicEndpoint[Unit, Unit, String, Any]] =
    List(graphiqlEndpoint)

  def routes[F[_]: Async]: HttpRoutes[F] =
    TapirSupport.interpreter[F].toRoutes(
      graphiqlServerEndpoint[F]
    )

  private def graphiqlServerEndpoint[F[_]: Async] =
    graphiqlEndpoint.serverLogicSuccess[F] { _ =>
      loadResource[F]("static/graphiql.html")
    }

  private def loadResource[F[_]: Async](path: String): F[String] =
    Async[F].flatMap(
      Async[F].fromOption(
        Option(getClass.getClassLoader.getResourceAsStream(path)),
        new RuntimeException(s"Resource not found: $path")
      )
    ) { stream =>
      Async[F].blocking {
        try scala.io.Source.fromInputStream(stream, "UTF-8").mkString
        finally stream.close()
      }
    }
}
