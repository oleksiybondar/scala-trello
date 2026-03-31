package io.github.oleksiybondar.api.config

import pureconfig.ConfigSource

object ConfigLoader {

  def load(): Either[Throwable, AppConfig] =
    ConfigSource.default.load[AppConfig].left.map(failures =>
      new RuntimeException(failures.prettyPrint())
    )
}
