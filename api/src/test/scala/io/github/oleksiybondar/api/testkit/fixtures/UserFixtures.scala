package io.github.oleksiybondar.api.testkit.fixtures

import io.github.oleksiybondar.api.domain.user._

import java.time.Instant
import java.util.UUID

object UserFixtures {

  val sampleUser: User =
    User(
      id = UserId(UUID.fromString("11111111-1111-1111-1111-111111111111")),
      username = Some(Username("alice")),
      email = Some(Email("alice@example.com")),
      passwordHash = PasswordHash("hash:secret"),
      firstName = FirstName("Alice"),
      lastName = LastName("Example"),
      avatarUrl = None,
      createdAt = Instant.parse("2026-03-25T10:15:30Z")
    )

  def user(
      id: UserId = sampleUser.id,
      username: Option[Username] = sampleUser.username,
      email: Option[Email] = sampleUser.email,
      passwordHash: PasswordHash = sampleUser.passwordHash,
      firstName: FirstName = sampleUser.firstName,
      lastName: LastName = sampleUser.lastName,
      avatarUrl: Option[AvatarUrl] = sampleUser.avatarUrl,
      createdAt: Instant = sampleUser.createdAt
  ): User =
    User(
      id = id,
      username = username,
      email = email,
      passwordHash = passwordHash,
      firstName = firstName,
      lastName = lastName,
      avatarUrl = avatarUrl,
      createdAt = createdAt
    )
}
