package io.github.oleksiybondar.api.http.routes.graphql

import caliban.{CalibanError, Http4sAdapter}
import caliban.interop.cats.implicits.*
import caliban.interop.tapir.HttpInterpreter
import cats.effect.IO
import cats.effect.std.Dispatcher
import io.github.oleksiybondar.api.http.routes.graphql.user.UserApi
import io.github.oleksiybondar.api.infrastructure.db.user.UserRepo
import org.http4s.HttpRoutes
import zio.Runtime

object GraphQLRoutes {

  def routes(
              userRepo: UserRepo[IO]
            )(using dispatcher: Dispatcher[IO], runtime: Runtime[Any]): IO[HttpRoutes[IO]] = {
    val api = UserApi.api(userRepo)

    api.interpreterF[IO].map { interpreter =>
      Http4sAdapter.makeHttpServiceF[IO, Any, CalibanError](
        HttpInterpreter(interpreter)
      )
    }
  }
}