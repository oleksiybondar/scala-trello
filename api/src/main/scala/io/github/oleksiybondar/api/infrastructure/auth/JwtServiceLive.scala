package io.github.oleksiybondar.api.infrastructure.auth

import cats.effect.kernel.Sync
import io.circe.syntax._
import io.circe.{Decoder, Encoder}
import io.github.oleksiybondar.api.domain.auth.{
  AccessToken,
  AccessTokenClaims,
  JwtService,
  SessionId
}
import io.github.oleksiybondar.api.domain.user.UserId
import pdi.jwt.{JwtAlgorithm, JwtCirce}

import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.{Base64, UUID}
import scala.util.Try

final class JwtServiceLive[F[_]: Sync](
    secret: String
) extends JwtService[F] {
  import JwtServiceLive.Payload

  private val algorithm = JwtAlgorithm.HS256

  override def encode(claims: AccessTokenClaims): F[AccessToken] =
    Sync[F].delay {
      val payload = Payload.fromClaims(claims)
      AccessToken(JwtCirce.encode(payload.asJson, secret, algorithm))
    }

  override def decode(token: AccessToken): F[Option[AccessTokenClaims]] =
    Sync[F].delay {
      token.value
        .split("\\.")
        .lift(1)
        .flatMap(decodeBase64Url)
        .flatMap(io.circe.parser.decode[Payload](_).toOption)
        .map(_.toClaims)
    }

  override def verify(token: AccessToken): F[Option[AccessTokenClaims]] =
    Sync[F].delay {
      JwtCirce
        .decodeJson(token.value, secret, Seq(algorithm))
        .toOption
        .flatMap(_.as[Payload].toOption)
        .map(_.toClaims)
    }

  private def decodeBase64Url(value: String): Option[String] =
    Try(Base64.getUrlDecoder.decode(value))
      .toOption
      .map(bytes => new String(bytes, StandardCharsets.UTF_8))
}

object JwtServiceLive {

  private final case class Payload(
      sub: UUID,
      sid: UUID,
      jti: UUID,
      iat: Long,
      exp: Long
  ) {
    def toClaims: AccessTokenClaims =
      AccessTokenClaims(
        userId = UserId(sub),
        sessionId = SessionId(sid),
        tokenId = jti,
        issuedAt = Instant.ofEpochSecond(iat),
        expiresAt = Instant.ofEpochSecond(exp)
      )
  }

  private object Payload {
    def fromClaims(claims: AccessTokenClaims): Payload =
      Payload(
        sub = claims.userId.value,
        sid = claims.sessionId.value,
        jti = claims.tokenId,
        iat = claims.issuedAt.getEpochSecond,
        exp = claims.expiresAt.getEpochSecond
      )

    given Encoder[Payload] = Encoder.forProduct5("sub", "sid", "jti", "iat", "exp")(payload =>
      (payload.sub, payload.sid, payload.jti, payload.iat, payload.exp)
    )

    given Decoder[Payload] = Decoder.forProduct5("sub", "sid", "jti", "iat", "exp")(Payload.apply)
  }
}
