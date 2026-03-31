package io.github.oleksiybondar.api.config

import pureconfig.ConfigReader

final case class AuthConfig(
    jwtSecret: String,
    accessTokenTtlSeconds: Long,
    sessionTtlDays: Long
) derives ConfigReader
