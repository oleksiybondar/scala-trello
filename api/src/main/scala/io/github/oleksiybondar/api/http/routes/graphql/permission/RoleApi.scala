package io.github.oleksiybondar.api.http.routes.graphql.permission

import cats.effect.unsafe.implicits.global
import io.github.oleksiybondar.api.domain.permission.RoleWithPermissions
import io.github.oleksiybondar.api.http.routes.graphql.GraphQLContext
import sangria.schema.{BooleanType, Field, ListType, ObjectType, StringType, fields}

object RoleApi {

  val PermissionType: ObjectType[Unit, PermissionView] =
    ObjectType(
      name = "PermissionView",
      fields[Unit, PermissionView](
        Field("id", StringType, resolve = _.value.id),
        Field("area", StringType, resolve = _.value.area),
        Field("canRead", BooleanType, resolve = _.value.canRead),
        Field("canCreate", BooleanType, resolve = _.value.canCreate),
        Field("canModify", BooleanType, resolve = _.value.canModify),
        Field("canDelete", BooleanType, resolve = _.value.canDelete),
        Field("canReassign", BooleanType, resolve = _.value.canReassign)
      )
    )

  val RoleType: ObjectType[Unit, RoleView] =
    ObjectType(
      name = "RoleView",
      fields[Unit, RoleView](
        Field("id", StringType, resolve = _.value.id),
        Field("name", StringType, resolve = _.value.name),
        Field("description", sangria.schema.OptionType(StringType), resolve = _.value.description),
        Field("permissions", ListType(PermissionType), resolve = _.value.permissions)
      )
    )

  val queryFields: List[Field[GraphQLContext, Unit]] =
    List(
      Field(
        name = "roles",
        fieldType = ListType(RoleType),
        resolve = ctx =>
          ctx.ctx.roleService
            .listRolesWithPermissions
            .map(_.map(toView))
            .unsafeToFuture()
      )
    )

  private def toView(role: RoleWithPermissions): RoleView =
    RoleView(
      id = role.role.id.value.toString,
      name = role.role.name.value,
      description = role.role.description,
      permissions = role.permissions.map { permission =>
        PermissionView(
          id = permission.id.value.toString,
          area = permission.area.value,
          canRead = permission.canRead,
          canCreate = permission.canCreate,
          canModify = permission.canModify,
          canDelete = permission.canDelete,
          canReassign = permission.canReassign
        )
      }
    )
}
