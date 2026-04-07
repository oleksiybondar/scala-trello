package io.github.oleksiybondar.api.domain.permission

final case class RoleName(value: String) extends AnyVal

final case class Role(
    id: RoleId,
    name: RoleName,
    description: Option[String]
)
