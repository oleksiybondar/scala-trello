package io.github.oleksiybondar.api.testkit.fixtures

import io.github.oleksiybondar.api.domain.permission.{
  Permission,
  PermissionArea,
  PermissionId,
  RoleId
}

object PermissionFixtures {

  val adminDashboardPermission: Permission =
    Permission(
      id = PermissionId(1),
      roleId = RoleId(1),
      area = PermissionArea.Dashboard,
      canRead = true,
      canCreate = true,
      canModify = true,
      canDelete = true,
      canReassign = true
    )

  val adminTicketPermission: Permission =
    Permission(
      id = PermissionId(2),
      roleId = RoleId(1),
      area = PermissionArea.Ticket,
      canRead = true,
      canCreate = true,
      canModify = true,
      canDelete = true,
      canReassign = true
    )

  val adminCommentPermission: Permission =
    Permission(
      id = PermissionId(3),
      roleId = RoleId(1),
      area = PermissionArea.Comment,
      canRead = true,
      canCreate = true,
      canModify = true,
      canDelete = true,
      canReassign = false
    )

  val contributorDashboardPermission: Permission =
    Permission(
      id = PermissionId(4),
      roleId = RoleId(2),
      area = PermissionArea.Dashboard,
      canRead = true,
      canCreate = false,
      canModify = false,
      canDelete = false,
      canReassign = false
    )

  val contributorTicketPermission: Permission =
    Permission(
      id = PermissionId(5),
      roleId = RoleId(2),
      area = PermissionArea.Ticket,
      canRead = true,
      canCreate = true,
      canModify = true,
      canDelete = false,
      canReassign = true
    )

  val contributorCommentPermission: Permission =
    Permission(
      id = PermissionId(6),
      roleId = RoleId(2),
      area = PermissionArea.Comment,
      canRead = true,
      canCreate = true,
      canModify = true,
      canDelete = false,
      canReassign = false
    )

  val viewerDashboardPermission: Permission =
    Permission(
      id = PermissionId(7),
      roleId = RoleId(3),
      area = PermissionArea.Dashboard,
      canRead = true,
      canCreate = false,
      canModify = false,
      canDelete = false,
      canReassign = false
    )

  val viewerTicketPermission: Permission =
    Permission(
      id = PermissionId(8),
      roleId = RoleId(3),
      area = PermissionArea.Ticket,
      canRead = true,
      canCreate = false,
      canModify = false,
      canDelete = false,
      canReassign = false
    )

  val viewerCommentPermission: Permission =
    Permission(
      id = PermissionId(9),
      roleId = RoleId(3),
      area = PermissionArea.Comment,
      canRead = true,
      canCreate = false,
      canModify = false,
      canDelete = false,
      canReassign = false
    )
}
