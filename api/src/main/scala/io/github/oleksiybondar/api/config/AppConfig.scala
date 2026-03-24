package io.github.oleksiybondar.api.config

import pureconfig.ConfigReader

final case class AppConfig(
                            http: HttpConfig,
                            database: DatabaseConfig
                          ) derives ConfigReader