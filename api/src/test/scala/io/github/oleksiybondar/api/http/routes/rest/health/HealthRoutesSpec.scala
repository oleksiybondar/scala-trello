package io.github.oleksiybondar.api.http.routes.rest.health

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import munit.FunSuite
import org.http4s.circe.CirceEntityCodec._
import org.http4s.implicits._
import org.http4s.{Method, Request, Status}

import java.time.Instant

class HealthRoutesSpec extends FunSuite {

  test("GET /health returns ok") {
    val response = HealthRoutes
      .routes[IO]
      .orNotFound
      .run(Request[IO](method = Method.GET, uri = uri"/health"))
      .unsafeRunSync()

    val body = response.as[HealthRoutes.HealthResponse].unsafeRunSync()

    assertEquals(response.status, Status.Ok)
    assertEquals(body.status, "ok")
    assertEquals(body.service, "api")
    assertEquals(body.version, "0.1.0-SNAPSHOT")
    assertEquals(Instant.parse(body.timestamp).toString, body.timestamp)
  }
}
