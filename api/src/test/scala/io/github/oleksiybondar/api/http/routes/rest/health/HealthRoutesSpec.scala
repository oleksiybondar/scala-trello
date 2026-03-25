package io.github.oleksiybondar.api.http.routes.rest.health

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import munit.FunSuite
import org.http4s.Method
import org.http4s.Request
import org.http4s.Status
import org.http4s.implicits.*

class HealthRoutesSpec extends FunSuite {

  test("GET /health returns ok") {
    val response = HealthRoutes
      .routes[IO]
      .orNotFound
      .run(Request[IO](method = Method.GET, uri = uri"/health"))
      .unsafeRunSync()

    val body = response.as[String].unsafeRunSync()

    assertEquals(response.status, Status.Ok)
    assertEquals(body, "ok")
  }
}
