ThisBuild / scalaVersion := "3.3.3"
ThisBuild / organization := "io.github.oleksiybondar"
ThisBuild / version := "0.1.0-SNAPSHOT"

lazy val root = (project in file("."))
  .settings(
    name := "api",

    libraryDependencies ++= Seq(
      // FP core
      "org.typelevel" %% "cats-core" % "2.13.0",
      "org.typelevel" %% "cats-effect" % "3.6.3",
      "co.fs2" %% "fs2-core" % "3.12.2",

      // HTTP
      "org.http4s" %% "http4s-dsl" % "0.23.30",
      "org.http4s" %% "http4s-ember-server" % "0.23.30",
      "org.http4s" %% "http4s-circe" % "0.23.30",

      // JSON
      "io.circe" %% "circe-core" % "0.14.15",
      "io.circe" %% "circe-generic" % "0.14.15",
      "io.circe" %% "circe-parser" % "0.14.15",

      // Config
      "com.github.pureconfig" %% "pureconfig-core" % "0.17.9",
      "com.github.pureconfig" %% "pureconfig-generic-scala3" % "0.17.9",

      // DB
      "com.typesafe.slick" %% "slick" % "3.6.0",
      "com.typesafe.slick" %% "slick-hikaricp" % "3.6.0",
      "org.postgresql" % "postgresql" % "42.7.5",

      // Migrations
      "org.flywaydb" % "flyway-core" % "12.1.1",
      "org.flywaydb" % "flyway-database-postgresql" % "12.1.1",

      // Logging
      "org.typelevel" %% "log4cats-slf4j" % "2.7.1",
      "ch.qos.logback" % "logback-classic" % "1.5.18",

      // REST docs: Tapir + Swagger UI
      "com.softwaremill.sttp.tapir" %% "tapir-core" % "1.11.50",
      "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % "1.11.50",
      "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % "1.11.50",
      "com.softwaremill.sttp.tapir" %% "tapir-openapi-docs" % "1.11.50",
      "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % "1.11.50",

      // GraphQL: Caliban + http4s + cats-effect interop
      "com.github.ghostdogpr" %% "caliban" % "3.0.0",
      "com.github.ghostdogpr" %% "caliban-http4s" % "3.0.0",
      "com.github.ghostdogpr" %% "caliban-cats" % "3.0.0",

      // Tests
      "org.scalameta" %% "munit" % "1.1.1" % Test
    ),

    addCommandAlias("migrate", "runMain io.github.oleksiybondar.api.MigrateMain"),
    addCommandAlias("app", "runMain io.github.oleksiybondar.api.Main"),
    addCommandAlias("migrateAndRun", "migrate; app")
  )