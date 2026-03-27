package io.github.oleksiybondar.api.domain.user

import java.time.Instant
import java.util.UUID

final case class UserId(value: UUID)         extends AnyVal
final case class Username(value: String)     extends AnyVal
final case class Email(value: String)        extends AnyVal
final case class PasswordHash(value: String) extends AnyVal
final case class FirstName(value: String)    extends AnyVal
final case class LastName(value: String)     extends AnyVal
final case class AvatarUrl(value: String)    extends AnyVal

final case class User(
    id: UserId,
    username: Option[Username],
    email: Option[Email],
    passwordHash: PasswordHash,
    firstName: FirstName,
    lastName: LastName,
    avatarUrl: Option[AvatarUrl],
    createdAt: Instant
)
