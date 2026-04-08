package io.github.oleksiybondar.api.domain.permission

final case class PermissionId(value: Long) extends AnyVal
final case class RoleId(value: Long)       extends AnyVal

enum PermissionArea(val value: String) {
  case Board   extends PermissionArea("dashboard")
  case Ticket  extends PermissionArea("ticket")
  case Comment extends PermissionArea("comment")
}

object PermissionArea {
  def fromString(value: String): Option[PermissionArea] =
    PermissionArea.values.find(_.value == value)
}

final case class Permission(
    id: PermissionId,
    roleId: RoleId,
    area: PermissionArea,
    canRead: Boolean,
    canCreate: Boolean,
    canModify: Boolean,
    canDelete: Boolean,
    canReassign: Boolean
)
