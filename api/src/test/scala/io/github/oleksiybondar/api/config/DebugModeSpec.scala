package io.github.oleksiybondar.api.config

import munit.FunSuite

class DebugModeSpec extends FunSuite {

  test("debug mode is disabled by default") {
    assertEquals(DebugMode.isEnabled(Map.empty, Map.empty), false)
  }

  test("debug mode is enabled from lowercase environment variable") {
    assertEquals(DebugMode.isEnabled(Map("debug" -> "true"), Map.empty), true)
  }

  test("debug mode is enabled from uppercase environment variable") {
    assertEquals(DebugMode.isEnabled(Map("DEBUG" -> "true"), Map.empty), true)
  }

  test("debug mode falls back to system properties") {
    assertEquals(DebugMode.isEnabled(Map.empty, Map("debug" -> "true")), true)
  }

  test("invalid debug values keep debug mode disabled") {
    assertEquals(DebugMode.isEnabled(Map("debug" -> "yes"), Map.empty), false)
  }
}
