package io.github.oleksiybondar.api.http.docs.graphql

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import munit.FunSuite
import org.http4s.implicits._
import org.http4s.{Method, Request, Status}

class GraphiQLRoutesSpec extends FunSuite {

  test("GET /docs/graphql returns the GraphiQL page") {
    val response = GraphiQLRoutes
      .routes[IO]
      .orNotFound
      .run(Request[IO](method = Method.GET, uri = uri"/docs/graphql"))
      .unsafeRunSync()

    val body = response.as[String].unsafeRunSync()

    assertEquals(response.status, Status.Ok)
    assert(body.contains("<title>GraphiQL</title>"))
    assert(body.contains("const fetcher"))
  }
}
