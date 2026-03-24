package io.github.oleksiybondar.api.infrastructure.db

import io.github.oleksiybondar.api.config.DatabaseConfig
import org.flywaydb.core.Flyway

object MigrationRunner {
  def migrate(config: DatabaseConfig): Int =
    Flyway
      .configure()
      .dataSource(
        config.db.url,
        config.db.user,
        config.db.password
      )
      .load()
      .migrate()
      .migrationsExecuted
}