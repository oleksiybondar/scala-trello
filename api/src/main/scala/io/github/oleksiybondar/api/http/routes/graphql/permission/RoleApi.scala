package io.github.oleksiybondar.api.http.routes.graphql.permission

import cats.effect.unsafe.implicits.global
import io.github.oleksiybondar.api.domain.permission.{Permission, RoleId, RoleWithPermissions}
import io.github.oleksiybondar.api.http.routes.graphql.GraphQLContext
import sangria.schema.{
  Argument,
  BooleanType,
  Field,
  ListType,
  LongType,
  ObjectType,
  OptionType,
  StringType,
  fields
}

object RoleApi {

  private val IdArg = Argument("id", LongType)

  val PermissionType: ObjectType[GraphQLContext, Permission] =
    ObjectType(
      name = "Permission",
      fields[GraphQLContext, Permission](
        Field("id", StringType, resolve = _.value.id.value.toString),
        Field("area", StringType, resolve = _.value.area.value),
        Field("canRead", BooleanType, resolve = _.value.canRead),
        Field("canCreate", BooleanType, resolve = _.value.canCreate),
        Field("canModify", BooleanType, resolve = _.value.canModify),
        Field("canDelete", BooleanType, resolve = _.value.canDelete),
        Field("canReassign", BooleanType, resolve = _.value.canReassign)
      )
    )

  val RoleType: ObjectType[GraphQLContext, RoleWithPermissions] =
    ObjectType(
      name = "Role",
      fields[GraphQLContext, RoleWithPermissions](
        Field("id", StringType, resolve = _.value.role.id.value.toString),
        Field("name", StringType, resolve = _.value.role.name.value),
        Field("description", OptionType(StringType), resolve = _.value.role.description),
        Field(
          "permissions",
          ListType(PermissionType),
          resolve = _.value.permissions
        )
      )
    )

  val queryFields: List[Field[GraphQLContext, Unit]] =
    List(
      Field(
        name = "role",
        fieldType = OptionType(RoleType),
        arguments = IdArg :: Nil,
        resolve = ctx => ctx.ctx.roleQueryRepo.findById(RoleId(ctx.arg(IdArg))).unsafeToFuture()
      ),
      Field(
        name = "roles",
        fieldType = ListType(RoleType),
        resolve = ctx => ctx.ctx.roleQueryRepo.list.unsafeToFuture()
      )
    )
}
