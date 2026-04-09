package io.github.oleksiybondar.api.validation

import munit.FunSuite

class InputValidationSpec extends FunSuite {

  test("normalizeEmail trims and lowercases valid input") {
    assertEquals(
      InputValidation.normalizeEmail("  Alice.Example+test@Example.COM  "),
      Some("alice.example+test@example.com")
    )
  }

  test("isValidEmail accepts standard email formats") {
    val validEmails = List(
      "alice@example.com",
      "alice.example+test@example.com",
      "user_name@example.co.uk",
      "x@example.io"
    )

    assert(validEmails.forall(InputValidation.isValidEmail))
  }

  test("isValidEmail rejects malformed email formats") {
    val invalidEmails = List(
      "",
      "plainaddress",
      "@example.com",
      "alice@",
      "alice@example",
      "alice..dots@example.com",
      "alice@.example.com",
      "alice@example..com",
      "alice example@example.com"
    )

    assert(invalidEmails.forall(email => !InputValidation.isValidEmail(email)))
  }
}
