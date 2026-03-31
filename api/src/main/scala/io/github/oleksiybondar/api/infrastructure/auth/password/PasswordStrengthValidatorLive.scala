package io.github.oleksiybondar.api.infrastructure.auth.password

import cats.data.{Validated, ValidatedNec}
import cats.syntax.all._
import io.github.oleksiybondar.api.config.PasswordStrengthConfig
import io.github.oleksiybondar.api.domain.auth.password.PasswordStrengthError.{
  PasswordDigitRequired,
  PasswordSpecialCharRequired,
  PasswordTooShort
}
import io.github.oleksiybondar.api.domain.auth.password.{
  PasswordStrengthError,
  PasswordStrengthValidator
}

final class PasswordStrengthValidatorLive(
    config: PasswordStrengthConfig
) extends PasswordStrengthValidator {

  override def validate(password: String): ValidatedNec[PasswordStrengthError, Unit] =
    (
      validateMinLength(password),
      validateDigit(password),
      validateSpecialChar(password)
    ).mapN((_, _, _) => ())

  private def validateMinLength(password: String): ValidatedNec[PasswordStrengthError, Unit] =
    Validated.condNec(
      password.length >= config.minLength,
      (),
      PasswordTooShort(config.minLength)
    )

  private def validateDigit(password: String): ValidatedNec[PasswordStrengthError, Unit] =
    Validated.condNec(
      !config.requireDigit || password.exists(_.isDigit),
      (),
      PasswordDigitRequired
    )

  private def validateSpecialChar(password: String): ValidatedNec[PasswordStrengthError, Unit] =
    Validated.condNec(
      !config.requireSpecialChar || password.exists(isSpecialChar),
      (),
      PasswordSpecialCharRequired
    )

  private def isSpecialChar(char: Char): Boolean =
    !char.isLetterOrDigit
}
