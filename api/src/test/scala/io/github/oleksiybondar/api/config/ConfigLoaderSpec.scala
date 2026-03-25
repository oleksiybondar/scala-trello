package io.github.oleksiybondar.api.config

import munit.FunSuite
import pureconfig.ConfigSource

class ConfigLoaderSpec extends FunSuite {

  test("ConfigLoader loads the default application configuration") {
    val result = ConfigLoader.load()

    assert(result.isRight)
  }

  test("ConfigLoader maps the http configuration") {
    val config = ConfigLoader.load().toOption.get

    assertEquals(config.http.host, "0.0.0.0")
    assertEquals(config.http.port, 8080)
  }

  test("ConfigLoader maps the database configuration") {
    val config = ConfigLoader.load().toOption.get

    assertEquals(config.database.profile, "slick.jdbc.PostgresProfile$")
  }

  test("ConfigLoader maps the nested database db configuration") {
    val config = ConfigLoader.load().toOption.get

    assertEquals(config.database.db.url, "jdbc:postgresql://localhost:5432/api")
    assertEquals(config.database.db.user, "api_user")
    assertEquals(config.database.db.password, "api_password")
    assertEquals(config.database.db.driver, "org.postgresql.Driver")
    assertEquals(config.database.db.keepAliveConnection, true)
  }

  test("AppConfig parsing fails when required fields are missing") {
    val result = ConfigSource
      .string(
        """http {
          |  host = "127.0.0.1"
          |}
          |
          |database {
          |  profile = "slick.jdbc.PostgresProfile$"
          |  db {
          |    url = "jdbc:postgresql://localhost:5432/api"
          |    user = "api_user"
          |    password = "api_password"
          |    driver = "org.postgresql.Driver"
          |    keep-alive-connection = true
          |  }
          |}
          |""".stripMargin
      )
      .load[AppConfig]

    assert(result.isLeft)
  }
}
