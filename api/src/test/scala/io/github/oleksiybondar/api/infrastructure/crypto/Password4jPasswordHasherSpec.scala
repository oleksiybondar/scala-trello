package io.github.oleksiybondar.api.infrastructure.crypto

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import io.github.oleksiybondar.api.config.{PasswordConfig, PasswordStrengthConfig}
import munit.FunSuite

class Password4jPasswordHasherSpec extends FunSuite {

  private val passwordConfig =
    PasswordConfig(
      pepper = "test-password-pepper",
      historySize = 5,
      strength = PasswordStrengthConfig(
        minLength = 8,
        requireDigit = false,
        requireSpecialChar = false
      )
    )

  test("hash returns a non-empty encoded password hash") {
    val hasher = new Password4jPasswordHasher[IO](passwordConfig)

    val result = hasher.hash("correct horse battery staple").unsafeRunSync()

    assertNotEquals(result.value, "")
    assertNotEquals(result.value, "correct horse battery staple")
  }

  test("verify returns true for the matching password") {
    val hasher = new Password4jPasswordHasher[IO](passwordConfig)

    val result =
      for {
        hash     <- hasher.hash("correct horse battery staple")
        verified <- hasher.verify("correct horse battery staple", hash)
      } yield verified

    assertEquals(result.unsafeRunSync(), true)
  }

  test("verify returns false for a non-matching password") {
    val hasher = new Password4jPasswordHasher[IO](passwordConfig)

    val result =
      for {
        hash     <- hasher.hash("correct horse battery staple")
        verified <- hasher.verify("wrong password", hash)
      } yield verified

    assertEquals(result.unsafeRunSync(), false)
  }
}
