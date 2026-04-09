package io.github.oleksiybondar.api.http.middleware

import cats.data.Kleisli
import cats.effect.kernel.Sync
import cats.syntax.all._
import org.http4s.{HttpApp, Request, Response}
import org.typelevel.log4cats.slf4j.Slf4jLogger

object LoggingMiddleware {

  def apply[F[_]: Sync](debugEnabled: Boolean): HttpApp[F] => HttpApp[F] =
    httpApp =>
      Kleisli { request =>
        Slf4jLogger.create[F].flatMap { logger =>
          val requestPath = s"${request.method.name} ${request.uri.renderString}"

          val requestLog =
            if (debugEnabled) logger.info(s"Request: $requestPath")
            else Sync[F].unit

          requestLog *>
            httpApp.run(request).attempt.flatTap {
              case Right(response) =>
                logResponse(debugEnabled, logger, request, response)
              case Left(error)     =>
                logger.error(error)(s"Error: $requestPath")
            }.rethrow
        }
      }

  private def logResponse[F[_]: Sync](
      debugEnabled: Boolean,
      logger: org.typelevel.log4cats.Logger[F],
      request: Request[F],
      response: Response[F]
  ): F[Unit] = {
    val requestPath = s"${request.method.name} ${request.uri.renderString}"
    val responseLog =
      if (debugEnabled) logger.info(s"Response: $requestPath -> ${response.status.code}")
      else Sync[F].unit

    val errorLog =
      if (response.status.code >= 500) {
        logger.error(s"Error response: $requestPath -> ${response.status.code}")
      } else Sync[F].unit

    responseLog *> errorLog
  }
}
