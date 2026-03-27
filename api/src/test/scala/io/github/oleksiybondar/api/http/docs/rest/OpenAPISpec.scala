package io.github.oleksiybondar.api.http.docs.rest

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import munit.FunSuite
import org.http4s.headers.Location
import org.http4s.implicits._
import org.http4s.{Method, Request, Status}

class OpenAPISpec extends FunSuite {

  test("GET /docs redirects to the Swagger UI page") {
    val response = OpenAPI
      .routes[IO]
      .orNotFound
      .run(Request[IO](method = Method.GET, uri = uri"/docs"))
      .unsafeRunSync()

    assertEquals(response.status, Status.PermanentRedirect)
    assertEquals(
      response.headers.get[Location].map(_.uri.renderString),
      Some("./docs/")
    )
  }

  test("GET /docs/docs.yaml returns the OpenAPI yaml document") {
    val response = OpenAPI
      .routes[IO]
      .orNotFound
      .run(Request[IO](method = Method.GET, uri = uri"/docs/docs.yaml"))
      .unsafeRunSync()

    val body = response.as[String].unsafeRunSync()

    assertEquals(response.status, Status.Ok)
    assert(body.contains("openapi:"))
    assert(body.contains("/auth/login"))
    assert(body.contains("/health"))
  }
}
