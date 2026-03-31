package io.github.oleksiybondar.api.infrastructure.auth.password

import io.github.oleksiybondar.api.config.PasswordStrengthConfig
import io.github.oleksiybondar.api.domain.auth.password.PasswordStrengthError.{
  PasswordDigitRequired,
  PasswordSpecialCharRequired,
  PasswordTooShort
}
import munit.FunSuite

class PasswordStrengthValidatorLiveSpec extends FunSuite {

  test("validate accepts a password that matches the configured rules") {
    val validator =
      new PasswordStrengthValidatorLive(
        PasswordStrengthConfig(
          minLength = 8,
          requireDigit = true,
          requireSpecialChar = true
        )
      )

    val result = validator.validate("Secure1!")

    assert(result.isValid)
  }

  test("validate reports a minimum length violation") {
    val validator =
      new PasswordStrengthValidatorLive(
        PasswordStrengthConfig(
          minLength = 8,
          requireDigit = false,
          requireSpecialChar = false
        )
      )

    val result = validator.validate("short")

    assertEquals(result.swap.toOption.get.toChain.toList, List(PasswordTooShort(8)))
  }

  test("validate reports a missing digit when required") {
    val validator =
      new PasswordStrengthValidatorLive(
        PasswordStrengthConfig(
          minLength = 8,
          requireDigit = true,
          requireSpecialChar = false
        )
      )

    val result = validator.validate("Password")

    assertEquals(result.swap.toOption.get.toChain.toList, List(PasswordDigitRequired))
  }

  test("validate reports a missing special character when required") {
    val validator =
      new PasswordStrengthValidatorLive(
        PasswordStrengthConfig(
          minLength = 8,
          requireDigit = false,
          requireSpecialChar = true
        )
      )

    val result = validator.validate("Password")

    assertEquals(result.swap.toOption.get.toChain.toList, List(PasswordSpecialCharRequired))
  }

  test("validate accumulates all configured violations") {
    val validator =
      new PasswordStrengthValidatorLive(
        PasswordStrengthConfig(
          minLength = 10,
          requireDigit = true,
          requireSpecialChar = true
        )
      )

    val result = validator.validate("short")

    assertEquals(
      result.swap.toOption.get.toChain.toList,
      List(
        PasswordTooShort(10),
        PasswordDigitRequired,
        PasswordSpecialCharRequired
      )
    )
  }
}
