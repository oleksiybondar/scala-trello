package io.github.oleksiybondar.api.domain.auth.password

import cats.data.ValidatedNec

sealed trait PasswordStrengthError

object PasswordStrengthError {
  final case class PasswordTooShort(minLength: Int) extends PasswordStrengthError
  case object PasswordDigitRequired                 extends PasswordStrengthError
  case object PasswordSpecialCharRequired           extends PasswordStrengthError
}

trait PasswordStrengthValidator {
  def validate(password: String): ValidatedNec[PasswordStrengthError, Unit]
}
