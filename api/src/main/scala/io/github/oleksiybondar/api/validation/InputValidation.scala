package io.github.oleksiybondar.api.validation

object InputValidation {

  private val EmailPattern =
    "^[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+@[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?(?:\\.[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?)+$".r

  def normalizeRequired(value: String): Option[String] =
    Option(value).map(_.trim).filter(_.nonEmpty)

  def normalizeEmail(value: String): Option[String] =
    normalizeRequired(value).map(_.toLowerCase)

  def isValidEmail(email: String): Boolean =
    EmailPattern.matches(email) && {
      val parts = email.split("@", 2)

      parts.length == 2 &&
      !parts.exists(_.isEmpty) &&
      !parts.exists(_.startsWith(".")) &&
      !parts.exists(_.endsWith(".")) &&
      !parts.exists(_.contains(".."))
    }
}
