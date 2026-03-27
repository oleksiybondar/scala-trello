package io.github.oleksiybondar.api

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import io.github.oleksiybondar.api.config.HttpConfig
import munit.FunSuite

import java.net.URL

class AppSmokeSpec extends FunSuite {

  test("the application starts and serves GET /health") {
    val response = (for {
      config <- Main.loadConfig
      testConfig = config.copy(http = HttpConfig(host = "127.0.0.1", port = 0))
      httpApp <- Main.buildApp(testConfig)
      response <- Main.buildServer(testConfig, httpApp).use { server =>
        IO.blocking {
          val connection =
            URL(s"http://127.0.0.1:${server.address.getPort}/health").openConnection()

          connection.setRequestProperty("Connection", "close")

          try {
            val statusCode =
              connection.asInstanceOf[java.net.HttpURLConnection].getResponseCode
            val source = scala.io.Source.fromInputStream(connection.getInputStream)
            val body =
              try source.mkString
              finally source.close()

            (statusCode, body)
          } finally {
            connection.asInstanceOf[java.net.HttpURLConnection].disconnect()
          }
        }
      }
    } yield response).unsafeRunSync()

    assertEquals(response._1, 200)
    assertEquals(response._2, "ok")
  }
}
