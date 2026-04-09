package io.github.oleksiybondar.api.config

object DebugMode {

  def isEnabled(
      env: Map[String, String] = sys.env,
      properties: collection.Map[String, String] = sys.props
  ): Boolean =
    readFlag(env.get("debug"))
      .orElse(readFlag(env.get("DEBUG")))
      .orElse(readFlag(properties.get("debug")))
      .getOrElse(false)

  private def readFlag(value: Option[String]): Option[Boolean] =
    value.map(_.trim.toLowerCase).collect {
      case "true"  => true
      case "false" => false
    }
}
