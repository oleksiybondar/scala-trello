package io.github.oleksiybondar.api.infrastructure.db.user

import cats.syntax.all.*
import cats.effect.Async
import io.github.oleksiybondar.api.domain.user.*
import slick.jdbc.PostgresProfile.api.*

import java.time.Instant
import java.util.UUID
import scala.concurrent.ExecutionContext

final class SlickUserRepo[F[_]: Async](
                                        db: Database
                                      )(implicit ec: ExecutionContext) extends UserRepo[F] {

  private final case class UserRow(
                                    id: UUID,
                                    username: Option[String],
                                    email: Option[String],
                                    passwordHash: String,
                                    firstName: String,
                                    lastName: String,
                                    avatarUrl: Option[String],
                                    createdAt: Instant
                                  )

  private final class UsersTable(tag: Tag) extends Table[UserRow](tag, "users") {
    def id = column[UUID]("id", O.PrimaryKey)
    def username = column[Option[String]]("username")
    def email = column[Option[String]]("email")
    def passwordHash = column[String]("password_hash")
    def firstName = column[String]("first_name")
    def lastName = column[String]("last_name")
    def avatarUrl = column[Option[String]]("avatar_url")
    def createdAt = column[Instant]("created_at")

    def * =
      (
        id,
        username,
        email,
        passwordHash,
        firstName,
        lastName,
        avatarUrl,
        createdAt
      ).mapTo[UserRow]
  }

  private val users = TableQuery[UsersTable]

  private def toRow(user: User): UserRow =
    UserRow(
      id = user.id.value,
      username = user.username.map(_.value),
      email = user.email.map(_.value),
      passwordHash = user.passwordHash.value,
      firstName = user.firstName.value,
      lastName = user.lastName.value,
      avatarUrl = user.avatarUrl.map(_.value),
      createdAt = user.createdAt
    )

  private def toDomain(row: UserRow): User =
    User(
      id = UserId(row.id),
      username = row.username.map(Username(_)),
      email = row.email.map(Email(_)),
      passwordHash = PasswordHash(row.passwordHash),
      firstName = FirstName(row.firstName),
      lastName = LastName(row.lastName),
      avatarUrl = row.avatarUrl.map(AvatarUrl(_)),
      createdAt = row.createdAt
    )

  private def run[A](action: DBIO[A]): F[A] =
    Async[F].fromFuture(Async[F].delay(db.run(action)))

  override def create(user: User): F[Unit] =
    run(users += toRow(user)).void

  override def findById(id: UserId): F[Option[User]] =
    run(
      users
        .filter(_.id === id.value)
        .result
        .headOption
    ).map(_.map(toDomain))

  override def list: F[List[User]] =
    run(users.result).map(_.toList.map(toDomain))

  override def update(user: User): F[Boolean] =
    run(
      users
        .filter(_.id === user.id.value)
        .update(toRow(user))
    ).map(_ > 0)

  override def delete(id: UserId): F[Boolean] =
    run(
      users
        .filter(_.id === id.value)
        .delete
    ).map(_ > 0)

  override def findByUsername(username: Username): F[Option[User]] =
    run(
      users
        .filter(_.username === username.value)
        .result
        .headOption
    ).map(_.map(toDomain))

  override def findByEmail(email: Email): F[Option[User]] =
    run(
      users
        .filter(_.email === email.value)
        .result
        .headOption
    ).map(_.map(toDomain))
}