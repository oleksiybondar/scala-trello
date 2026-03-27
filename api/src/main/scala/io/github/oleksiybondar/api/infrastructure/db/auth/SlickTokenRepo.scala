package io.github.oleksiybondar.api.infrastructure.db.auth

import cats.effect.Async
import cats.syntax.all._
import io.github.oleksiybondar.api.domain.auth.{AccessToken, RefreshToken, TokenRepo, TokenType}
import io.github.oleksiybondar.api.domain.user.UserId
import slick.jdbc.PostgresProfile.api._

import java.time.Instant
import java.util.UUID
import scala.concurrent.ExecutionContext

final class SlickTokenRepo[F[_]: Async](
    db: Database
)(implicit ec: ExecutionContext) extends TokenRepo[F] {

  private final case class TokenRow(
      token: String,
      userId: UUID,
      tokenType: String,
      createdAt: Instant
  )

  private final class AuthTokensTable(tag: Tag) extends Table[TokenRow](tag, "auth_tokens") {
    def token     = column[String]("token", O.PrimaryKey)
    def userId    = column[UUID]("user_id")
    def tokenType = column[String]("token_type")
    def createdAt = column[Instant]("created_at")

    def userFk =
      foreignKey("auth_tokens_user_id_fk", userId, users)(_.id, onDelete = ForeignKeyAction.Cascade)

    def * = (token, userId, tokenType, createdAt).mapTo[TokenRow]
  }

  private final class UsersTable(tag: Tag) extends Table[(UUID, String)](tag, "users") {
    def id           = column[UUID]("id", O.PrimaryKey)
    def passwordHash = column[String]("password_hash")

    def * = (id, passwordHash)
  }

  private val users      = TableQuery[UsersTable]
  private val authTokens = TableQuery[AuthTokensTable]

  private def run[A](action: DBIO[A]): F[A] =
    Async[F].fromFuture(Async[F].delay(db.run(action)))

  private def insertToken(token: String, userId: UserId, tokenType: TokenType): F[Unit] =
    run(
      authTokens += TokenRow(
        token = token,
        userId = userId.value,
        tokenType = tokenType.toString,
        createdAt = Instant.now()
      )
    ).void

  override def saveAccessToken(token: AccessToken, userId: UserId): F[Unit] =
    insertToken(token.value, userId, TokenType.Access)

  override def saveRefreshToken(token: RefreshToken, userId: UserId): F[Unit] =
    insertToken(token.value, userId, TokenType.Refresh)

  override def findUserIdByAccessToken(token: AccessToken): F[Option[UserId]] =
    findUserId(token.value, TokenType.Access)

  override def findUserIdByRefreshToken(token: RefreshToken): F[Option[UserId]] =
    findUserId(token.value, TokenType.Refresh)

  override def rotateRefreshToken(
      current: RefreshToken,
      next: RefreshToken,
      userId: UserId
  ): F[Unit] =
    run {
      for {
        _ <- authTokens
               .filter(row =>
                 row.token === current.value && row.tokenType === TokenType.Refresh.toString
               )
               .delete
        _ <- authTokens += TokenRow(
               token = next.value,
               userId = userId.value,
               tokenType = TokenType.Refresh.toString,
               createdAt = Instant.now()
             )
      } yield ()
    }

  override def deleteRefreshToken(token: RefreshToken): F[Unit] =
    run(
      authTokens
        .filter(row => row.token === token.value && row.tokenType === TokenType.Refresh.toString)
        .delete
    ).void

  private def findUserId(token: String, tokenType: TokenType): F[Option[UserId]] =
    run(
      authTokens
        .filter(row => row.token === token && row.tokenType === tokenType.toString)
        .map(_.userId)
        .result
        .headOption
    ).map(_.map(UserId(_)))
}
