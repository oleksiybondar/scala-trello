package io.github.oleksiybondar.api.domain.comment

import cats.effect.IO
import io.github.oleksiybondar.api.domain.user.UserId
import io.github.oleksiybondar.api.testkit.fixtures.{
  BoardFixtures,
  BoardMemberFixtures,
  CommentFixtures,
  CommentServiceFixtures,
  PermissionFixtures,
  RoleFixtures,
  TicketFixtures
}
import munit.FunSuite

import java.util.UUID

class CommentServiceLiveSpec extends FunSuite {

  test("createComment persists a comment when the actor can create comments on an active board") {
    val command = CreateCommentCommand(
      ticketId = TicketFixtures.sampleTicket.id,
      message = CommentMessage("New discussion note"),
      relatedCommentId = None
    )

    val result = CommentServiceFixtures.withCommentService(
      tickets = List(TicketFixtures.sampleTicket),
      boards = List(BoardFixtures.sampleDashboard),
      members =
        List(BoardMemberFixtures.sampleMember.copy(roleId = RoleFixtures.contributorRole.id)),
      roles = List(RoleFixtures.contributorRole),
      permissions = List(PermissionFixtures.contributorCommentPermission)
    ) { ctx =>
      for {
        created <-
          ctx.commentService.createComment(command, BoardMemberFixtures.sampleMember.userId)
        stored  <- created match {
                     case Some(comment) => ctx.commentRepo.findById(comment.id)
                     case None          => IO.pure(None)
                   }
      } yield (created.nonEmpty, stored.map(_.authorUserId))
    }

    assertEquals(result, (true, Some(BoardMemberFixtures.sampleMember.userId)))
  }

  test("createComment returns none when the board is inactive") {
    val command = CreateCommentCommand(
      ticketId = TicketFixtures.sampleTicket.id,
      message = CommentMessage("Blocked"),
      relatedCommentId = None
    )

    val result = CommentServiceFixtures.withCommentService(
      tickets = List(TicketFixtures.sampleTicket),
      boards = List(BoardFixtures.dashboard(active = false)),
      members =
        List(BoardMemberFixtures.sampleMember.copy(roleId = RoleFixtures.contributorRole.id)),
      roles = List(RoleFixtures.contributorRole),
      permissions = List(PermissionFixtures.contributorCommentPermission)
    )(_.commentService.createComment(command, BoardMemberFixtures.sampleMember.userId))

    assertEquals(result, None)
  }

  test("createComment returns none when comments are disabled on the ticket") {
    val command = CreateCommentCommand(
      ticketId = TicketFixtures.sampleTicket.id,
      message = CommentMessage("Blocked"),
      relatedCommentId = None
    )

    val result = CommentServiceFixtures.withCommentService(
      tickets = List(TicketFixtures.ticket(commentsEnabled = false)),
      boards = List(BoardFixtures.sampleDashboard),
      members =
        List(BoardMemberFixtures.sampleMember.copy(roleId = RoleFixtures.contributorRole.id)),
      roles = List(RoleFixtures.contributorRole),
      permissions = List(PermissionFixtures.contributorCommentPermission)
    )(_.commentService.createComment(command, BoardMemberFixtures.sampleMember.userId))

    assertEquals(result, None)
  }

  test("changeMessage updates the comment only for its author") {
    val existing = CommentFixtures.sampleComment

    val result = CommentServiceFixtures.withCommentService(
      comments = List(existing),
      tickets = List(TicketFixtures.sampleTicket),
      boards = List(BoardFixtures.sampleDashboard),
      members =
        List(BoardMemberFixtures.sampleMember.copy(roleId = RoleFixtures.contributorRole.id)),
      roles = List(RoleFixtures.contributorRole),
      permissions = List(PermissionFixtures.contributorCommentPermission)
    ) { ctx =>
      for {
        updated <- ctx.commentService.changeMessage(
                     existing.id,
                     existing.authorUserId,
                     CommentMessage("Updated message")
                   )
        stored  <- ctx.commentRepo.findById(existing.id)
      } yield (updated, stored.map(_.message))
    }

    assertEquals(result, (true, Some(CommentMessage("Updated message"))))
  }

  test("changeMessage returns false when attempted by a different user") {
    val existing  = CommentFixtures.sampleComment
    val otherUser = UserId(UUID.fromString("22222222-2222-2222-2222-222222222222"))

    val result = CommentServiceFixtures.withCommentService(
      comments = List(existing),
      tickets = List(TicketFixtures.sampleTicket),
      boards = List(BoardFixtures.sampleDashboard),
      members = List(
        BoardMemberFixtures.sampleMember.copy(roleId = RoleFixtures.contributorRole.id),
        BoardMemberFixtures.member(userId = otherUser, roleId = RoleFixtures.contributorRole.id)
      ),
      roles = List(RoleFixtures.contributorRole),
      permissions = List(PermissionFixtures.contributorCommentPermission)
    ) { ctx =>
      for {
        updated <- ctx.commentService.changeMessage(
                     existing.id,
                     otherUser,
                     CommentMessage("Blocked update")
                   )
        stored  <- ctx.commentRepo.findById(existing.id)
      } yield (updated, stored.map(_.message))
    }

    assertEquals(result, (false, Some(existing.message)))
  }

  test("deleteComment removes the comment only for its author") {
    val existing = CommentFixtures.sampleComment

    val result = CommentServiceFixtures.withCommentService(
      comments = List(existing),
      tickets = List(TicketFixtures.sampleTicket),
      boards = List(BoardFixtures.sampleDashboard),
      members = List(BoardMemberFixtures.sampleMember.copy(roleId = RoleFixtures.adminRole.id)),
      roles = List(RoleFixtures.adminRole),
      permissions = List(PermissionFixtures.adminCommentPermission)
    ) { ctx =>
      for {
        deleted <- ctx.commentService.deleteComment(existing.id, existing.authorUserId)
        stored  <- ctx.commentRepo.findById(existing.id)
      } yield (deleted, stored)
    }

    assertEquals(result, (true, None))
  }

  test("deleteComment returns false when attempted by a different user") {
    val existing  = CommentFixtures.sampleComment
    val otherUser = UserId(UUID.fromString("22222222-2222-2222-2222-222222222222"))

    val result = CommentServiceFixtures.withCommentService(
      comments = List(existing),
      tickets = List(TicketFixtures.sampleTicket),
      boards = List(BoardFixtures.sampleDashboard),
      members = List(
        BoardMemberFixtures.sampleMember.copy(roleId = RoleFixtures.adminRole.id),
        BoardMemberFixtures.member(userId = otherUser, roleId = RoleFixtures.adminRole.id)
      ),
      roles = List(RoleFixtures.adminRole),
      permissions = List(PermissionFixtures.adminCommentPermission)
    ) { ctx =>
      for {
        deleted <- ctx.commentService.deleteComment(existing.id, otherUser)
        stored  <- ctx.commentRepo.findById(existing.id)
      } yield (deleted, stored)
    }

    assertEquals(result, (false, Some(existing)))
  }

  test("listComments returns comments when the actor can read comments") {
    val existing = CommentFixtures.sampleComment

    val result = CommentServiceFixtures.withCommentService(
      comments = List(existing),
      tickets = List(TicketFixtures.sampleTicket),
      boards = List(BoardFixtures.sampleDashboard),
      members =
        List(BoardMemberFixtures.sampleMember.copy(roleId = RoleFixtures.contributorRole.id)),
      roles = List(RoleFixtures.contributorRole),
      permissions = List(PermissionFixtures.contributorCommentPermission)
    )(_.commentService.listComments(existing.ticketId, existing.authorUserId))

    assertEquals(result, List(existing))
  }

  test("listComments returns empty when the actor lacks read permission") {
    val existing = CommentFixtures.sampleComment

    val result = CommentServiceFixtures.withCommentService(
      comments = List(existing),
      tickets = List(TicketFixtures.sampleTicket),
      boards = List(BoardFixtures.sampleDashboard),
      members = List(BoardMemberFixtures.sampleMember.copy(roleId = RoleFixtures.viewerRole.id)),
      roles = List(RoleFixtures.viewerRole),
      permissions = List(PermissionFixtures.viewerTicketPermission)
    )(_.commentService.listComments(existing.ticketId, existing.authorUserId))

    assertEquals(result, Nil)
  }

  test("listCommentsByUser returns own comments ordered by newest first") {
    val firstComment  = CommentFixtures.sampleComment
    val secondComment =
      CommentFixtures.comment(
        id = CommentId(2),
        createdAt = java.time.Instant.parse("2026-04-06T13:00:00Z"),
        modifiedAt = java.time.Instant.parse("2026-04-06T13:00:00Z"),
        message = CommentMessage("Later comment")
      )

    val result = CommentServiceFixtures.withCommentService(
      comments = List(firstComment, secondComment)
    )(_.commentService.listCommentsByUser(firstComment.authorUserId, firstComment.authorUserId))

    assertEquals(result, List(secondComment, firstComment))
  }

  test("listCommentsByUser returns empty for another user") {
    val existing  = CommentFixtures.sampleComment
    val otherUser = UserId(UUID.fromString("22222222-2222-2222-2222-222222222222"))

    val result = CommentServiceFixtures.withCommentService(
      comments = List(existing)
    )(_.commentService.listCommentsByUser(existing.authorUserId, otherUser))

    assertEquals(result, Nil)
  }
}
