package io.github.oleksiybondar.api.config

import pureconfig.ConfigReader

final case class PasswordStrengthConfig(
    minLength: Int,
    requireDigit: Boolean,
    requireSpecialChar: Boolean
) derives ConfigReader

final case class PasswordConfig(
    pepper: String,
    historySize: Int,
    strength: PasswordStrengthConfig
) derives ConfigReader
