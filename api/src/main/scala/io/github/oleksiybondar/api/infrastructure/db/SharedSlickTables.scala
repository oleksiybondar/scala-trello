package io.github.oleksiybondar.api.infrastructure.db

import slick.jdbc.PostgresProfile.api._
import slick.lifted.ProvenShape

import java.time.Instant
import java.util.UUID

object SharedSlickTables {

  final case class UserRow(
      id: UUID,
      username: Option[String],
      email: Option[String],
      passwordHash: String,
      firstName: String,
      lastName: String,
      avatarUrl: Option[String],
      createdAt: Instant
  )

  final class UsersTable(tag: Tag) extends Table[UserRow](tag, "users") {
    def id: Rep[UUID]                  = column[UUID]("id", O.PrimaryKey)
    def username: Rep[Option[String]]  = column[Option[String]]("username")
    def email: Rep[Option[String]]     = column[Option[String]]("email")
    def passwordHash: Rep[String]      = column[String]("password_hash")
    def firstName: Rep[String]         = column[String]("first_name")
    def lastName: Rep[String]          = column[String]("last_name")
    def avatarUrl: Rep[Option[String]] = column[Option[String]]("avatar_url")
    def createdAt: Rep[Instant]        = column[Instant]("created_at")

    def * : ProvenShape[UserRow] =
      (id, username, email, passwordHash, firstName, lastName, avatarUrl, createdAt).mapTo[UserRow]
  }

  final case class TimeTrackingRow(
      id: Long,
      ticketId: Long,
      userId: UUID,
      activityId: Long,
      durationMinutes: Int,
      loggedAt: Instant,
      description: Option[String]
  )

  final class TimeTrackingTable(tag: Tag) extends Table[TimeTrackingRow](tag, "time_tracking") {
    def id: Rep[Long]                    = column[Long]("id", O.PrimaryKey)
    def ticketId: Rep[Long]              = column[Long]("ticket_id")
    def userId: Rep[UUID]                = column[UUID]("user_id")
    def activityId: Rep[Long]            = column[Long]("activity_id")
    def durationMinutes: Rep[Int]        = column[Int]("duration_minutes")
    def loggedAt: Rep[Instant]           = column[Instant]("logged_at")
    def description: Rep[Option[String]] = column[Option[String]]("description")

    def * : ProvenShape[TimeTrackingRow] =
      (id, ticketId, userId, activityId, durationMinutes, loggedAt, description)
        .mapTo[TimeTrackingRow]
  }

  final case class ActivityRow(
      id: Long,
      code: String,
      name: String,
      description: Option[String]
  )

  final class ActivitiesTable(tag: Tag) extends Table[ActivityRow](tag, "activities") {
    def id: Rep[Long]                    = column[Long]("id", O.PrimaryKey)
    def code: Rep[String]                = column[String]("code")
    def name: Rep[String]                = column[String]("name")
    def description: Rep[Option[String]] = column[Option[String]]("description")

    def * : ProvenShape[ActivityRow] = (id, code, name, description).mapTo[ActivityRow]
  }

  final case class RoleRow(
      id: Long,
      name: String,
      description: Option[String]
  )

  final class RolesTable(tag: Tag) extends Table[RoleRow](tag, "roles") {
    def id: Rep[Long]                    = column[Long]("id", O.PrimaryKey)
    def name: Rep[String]                = column[String]("name")
    def description: Rep[Option[String]] = column[Option[String]]("description")

    def * : ProvenShape[RoleRow] = (id, name, description).mapTo[RoleRow]
  }

  final case class PermissionRow(
      id: Long,
      roleId: Long,
      area: String,
      canRead: Boolean,
      canCreate: Boolean,
      canModify: Boolean,
      canDelete: Boolean,
      canReassign: Boolean
  )

  final class PermissionsTable(tag: Tag) extends Table[PermissionRow](tag, "permissions") {
    def id: Rep[Long]             = column[Long]("id", O.PrimaryKey)
    def roleId: Rep[Long]         = column[Long]("role_id")
    def area: Rep[String]         = column[String]("area")
    def canRead: Rep[Boolean]     = column[Boolean]("can_read")
    def canCreate: Rep[Boolean]   = column[Boolean]("can_create")
    def canModify: Rep[Boolean]   = column[Boolean]("can_modify")
    def canDelete: Rep[Boolean]   = column[Boolean]("can_delete")
    def canReassign: Rep[Boolean] = column[Boolean]("can_reassign")

    def * : ProvenShape[PermissionRow] =
      (id, roleId, area, canRead, canCreate, canModify, canDelete, canReassign).mapTo[PermissionRow]
  }

  final case class CommentRow(
      id: Long,
      ticketId: Long,
      authorUserId: UUID,
      createdAt: Instant,
      modifiedAt: Instant,
      message: String,
      relatedCommentId: Option[Long]
  )

  final class CommentsTable(tag: Tag) extends Table[CommentRow](tag, "comments") {
    def id: Rep[Long]                       = column[Long]("id", O.PrimaryKey)
    def ticketId: Rep[Long]                 = column[Long]("ticket_id")
    def authorUserId: Rep[UUID]             = column[UUID]("author_user_id")
    def createdAt: Rep[Instant]             = column[Instant]("created_at")
    def modifiedAt: Rep[Instant]            = column[Instant]("modified_at")
    def message: Rep[String]                = column[String]("message")
    def relatedCommentId: Rep[Option[Long]] = column[Option[Long]]("related_comment_id")

    def * : ProvenShape[CommentRow] =
      (id, ticketId, authorUserId, createdAt, modifiedAt, message, relatedCommentId)
        .mapTo[CommentRow]
  }

  final case class BoardRow(
      id: UUID,
      name: String,
      description: Option[String],
      active: Boolean,
      ownerUserId: UUID,
      createdByUserId: UUID,
      createdAt: Instant,
      modifiedAt: Instant,
      lastModifiedByUserId: UUID
  )

  final class BoardsTable(tag: Tag) extends Table[BoardRow](tag, "boards") {
    def id: Rep[UUID]                    = column[UUID]("id", O.PrimaryKey)
    def name: Rep[String]                = column[String]("name")
    def description: Rep[Option[String]] = column[Option[String]]("description")
    def active: Rep[Boolean]             = column[Boolean]("active")
    def ownerUserId: Rep[UUID]           = column[UUID]("owner_user_id")
    def createdByUserId: Rep[UUID]       = column[UUID]("created_by_user_id")
    def createdAt: Rep[Instant]          = column[Instant]("created_at")
    def modifiedAt: Rep[Instant]         = column[Instant]("modified_at")
    def lastModifiedByUserId: Rep[UUID]  = column[UUID]("last_modified_by_user_id")

    def * : ProvenShape[BoardRow] =
      (
        id,
        name,
        description,
        active,
        ownerUserId,
        createdByUserId,
        createdAt,
        modifiedAt,
        lastModifiedByUserId
      ).mapTo[BoardRow]
  }

  final case class TicketRow(
      id: Long,
      boardId: UUID,
      name: String,
      description: Option[String],
      acceptanceCriteria: Option[String],
      createdByUserId: UUID,
      assignedToUserId: Option[UUID],
      lastModifiedByUserId: UUID,
      createdAt: Instant,
      modifiedAt: Instant,
      originalEstimatedMinutes: Option[Int],
      priority: Option[String],
      severityId: Option[Long],
      stateId: Long
  )

  final class TicketsTable(tag: Tag) extends Table[TicketRow](tag, "tickets") {
    def id: Rep[Long]                              = column[Long]("id", O.PrimaryKey)
    def boardId: Rep[UUID]                         = column[UUID]("dashboard_id")
    def name: Rep[String]                          = column[String]("name")
    def description: Rep[Option[String]]           = column[Option[String]]("description")
    def acceptanceCriteria: Rep[Option[String]]    = column[Option[String]]("acceptance_criteria")
    def createdByUserId: Rep[UUID]                 = column[UUID]("created_by_user_id")
    def assignedToUserId: Rep[Option[UUID]]        = column[Option[UUID]]("assigned_to_user_id")
    def lastModifiedByUserId: Rep[UUID]            = column[UUID]("last_modified_by_user_id")
    def createdAt: Rep[Instant]                    = column[Instant]("created_at")
    def modifiedAt: Rep[Instant]                   = column[Instant]("modified_at")
    def originalEstimatedMinutes: Rep[Option[Int]] =
      column[Option[Int]]("original_estimated_minutes")
    def priority: Rep[Option[String]]              = column[Option[String]]("priority")
    def severityId: Rep[Option[Long]]              = column[Option[Long]]("severity_id")
    def stateId: Rep[Long]                         = column[Long]("state_id")

    def * : ProvenShape[TicketRow] =
      (
        id,
        boardId,
        name,
        description,
        acceptanceCriteria,
        createdByUserId,
        assignedToUserId,
        lastModifiedByUserId,
        createdAt,
        modifiedAt,
        originalEstimatedMinutes,
        priority,
        severityId,
        stateId
      ).mapTo[TicketRow]
  }
}
