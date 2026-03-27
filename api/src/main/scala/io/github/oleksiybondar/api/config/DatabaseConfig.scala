package io.github.oleksiybondar.api.config

import pureconfig.ConfigReader

final case class DatabaseDbConfig(
    url: String,
    user: String,
    password: String,
    driver: String,
    keepAliveConnection: Boolean
) derives ConfigReader

final case class DatabaseConfig(
    profile: String,
    db: DatabaseDbConfig
) derives ConfigReader
