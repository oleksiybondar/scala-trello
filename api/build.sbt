import com.typesafe.sbt.packager.archetypes.JavaAppPackaging

ThisBuild / scalaVersion := "3.3.3"
ThisBuild / organization := "io.github.oleksiybondar"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision

lazy val root = (project in file("."))
  .enablePlugins(JavaAppPackaging)
  .settings(
    name := "api",
    Test / parallelExecution := false,
    Compile / mainClass := Some("io.github.oleksiybondar.api.Main"),
    Universal / javaOptions ++= Seq(
      "-Dconfig.resource=application.conf"
    ),

    scalacOptions ++= Seq(
      "-deprecation",
      "-feature",
      "-unchecked",
      "-Werror",
      "-Wunused:all",
      "-Wvalue-discard"
    ),

    coverageMinimumStmtTotal := 80,
    coverageMinimumBranchTotal := 70,
    coverageFailOnMinimum := true,
    coverageHighlighting := true,

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
      "com.github.jwt-scala" %% "jwt-circe" % "11.0.3",
      "com.password4j" % "password4j" % "1.8.4",

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

      // GraphQL: Sangria + Circe
      "org.sangria-graphql" %% "sangria" % "4.2.15",
      "org.sangria-graphql" %% "sangria-circe" % "1.3.2",

      // Tests
      "org.scalameta" %% "munit" % "1.1.1" % Test
    )
  )

addCommandAlias("fmt", "scalafmtAll")
addCommandAlias("fmtCheck", "scalafmtCheckAll")
addCommandAlias(
  "lint",
  "; compile; Test / compile; scalafixAll --check; Test / scalafixAll --check; scapegoat; Test / scapegoat"
)
addCommandAlias("packageApp", "; clean; stage")
addCommandAlias("distApp", "; clean; dist")
addCommandAlias(
  "coverageCheck",
  "; clean; coverage; test; coverageReport; coverageOff"
)
addCommandAlias("quality", "; fmtCheck; lint; coverageCheck")
addCommandAlias("migrate", "runMain io.github.oleksiybondar.api.MigrateMain")
addCommandAlias("app", "runMain io.github.oleksiybondar.api.Main")
addCommandAlias("cleanupSessions", "runMain io.github.oleksiybondar.api.CleanupSessionDataMain")
addCommandAlias(
  "dev",
  """; set Compile / run / fork := true; set Compile / run / envVars += ("debug" -> "true"); runMain io.github.oleksiybondar.api.Main"""
)
addCommandAlias("migrateAndRun", "migrate; app")
