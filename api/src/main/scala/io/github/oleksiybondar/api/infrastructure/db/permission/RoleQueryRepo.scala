package io.github.oleksiybondar.api.infrastructure.db.permission

import io.github.oleksiybondar.api.domain.permission.{RoleId, RoleWithPermissions}

trait RoleQueryRepo[F[_]] {
  def findById(id: RoleId): F[Option[RoleWithPermissions]]
  def list: F[List[RoleWithPermissions]]
}
