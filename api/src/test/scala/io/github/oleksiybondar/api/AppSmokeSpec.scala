package io.github.oleksiybondar.api

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import io.github.oleksiybondar.api.config.HttpConfig
import munit.FunSuite

import java.net.URI

class AppSmokeSpec extends FunSuite {

  test("the application starts and serves GET /health") {
    val response = (for {
      config    <- Main.loadConfig
      testConfig = config.copy(http = HttpConfig(host = "127.0.0.1", port = 0))
      response  <- Main.buildApp(testConfig).flatMap(Main.buildServer(testConfig, _)).use { server =>
                     IO.blocking {
                       val connection =
                         URI
                           .create(s"http://127.0.0.1:${server.address.getPort}/health")
                           .toURL
                           .openConnection()

                       connection.setRequestProperty("Connection", "close")

                       try {
                         val statusCode =
                           connection.asInstanceOf[java.net.HttpURLConnection].getResponseCode
                         val source     = scala.io.Source.fromInputStream(connection.getInputStream)
                         val body       =
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
