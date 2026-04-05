package io.github.oleksiybondar.api.infrastructure.db.permission

import io.github.oleksiybondar.api.domain.permission.{Role, RoleId, RoleName}

trait RoleRepo[F[_]] {
  def findById(id: RoleId): F[Option[Role]]
  def findByName(name: RoleName): F[Option[Role]]
  def list: F[List[Role]]
}
