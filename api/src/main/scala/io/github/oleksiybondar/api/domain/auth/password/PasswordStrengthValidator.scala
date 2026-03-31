package io.github.oleksiybondar.api.domain.auth.password

import cats.data.ValidatedNec

/** Validation error describing why a password does not satisfy the configured policy. */
sealed trait PasswordStrengthError

object PasswordStrengthError {

  /** Password length is below the configured minimum. */
  final case class PasswordTooShort(minLength: Int) extends PasswordStrengthError

  /** Password is required to contain at least one digit but does not. */
  case object PasswordDigitRequired extends PasswordStrengthError

  /** Password is required to contain at least one special character but does not. */
  case object PasswordSpecialCharRequired extends PasswordStrengthError
}

/** Pure validator that evaluates a raw password against the configured strength rules. */
trait PasswordStrengthValidator {

  /** Validates a raw password and accumulates every violated rule.
    *
    * @param password
    *   Raw password to validate.
    * @return
    *   Successful validation when all rules pass, otherwise the non-empty list of violations.
    */
  def validate(password: String): ValidatedNec[PasswordStrengthError, Unit]
}
