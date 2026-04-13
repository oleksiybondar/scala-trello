package io.github.oleksiybondar.api.testkit.support

import cats.effect.kernel.Sync
import cats.syntax.all._
import io.github.oleksiybondar.api.domain.ticket.{Ticket, TicketId}
import io.github.oleksiybondar.api.domain.timeTracking.TimeTrackingActivity
import io.github.oleksiybondar.api.infrastructure.db.ticket.{
  TicketQueryBoardRow,
  TicketQueryRepo,
  TicketQueryRow,
  TicketQueryTimeEntryRow,
  TicketQueryUserRow
}

final class InMemoryTicketQueryRepo[F[_]: Sync](
    tickets: List[Ticket],
    boards: List[io.github.oleksiybondar.api.domain.board.Board],
    comments: List[io.github.oleksiybondar.api.domain.comment.Comment],
    timeEntries: List[io.github.oleksiybondar.api.domain.timeTracking.TimeTrackingEntry],
    users: List[io.github.oleksiybondar.api.domain.user.User],
    activities: List[TimeTrackingActivity]
) extends TicketQueryRepo[F] {

  override def findById(ticketId: TicketId): F[Option[TicketQueryRow]] =
    tickets
      .find(_.id == ticketId)
      .flatMap { ticket =>
        for {
          board      <- boards.find(_.id == ticket.boardId)
          createdBy  <- users.find(_.id == ticket.createdByUserId)
          modifiedBy <- users.find(_.id == ticket.lastModifiedByUserId)
        } yield TicketQueryRow(
          id = ticket.id,
          boardId = ticket.boardId.value.toString,
          name = ticket.name.value,
          description = ticket.description.map(_.value),
          acceptanceCriteria = ticket.acceptanceCriteria.map(_.value),
          estimatedMinutes = ticket.originalEstimatedMinutes,
          priority = ticket.priority.flatMap(_.value.toIntOption),
          severityId = ticket.severityId.map(_.value),
          createdByUserId = ticket.createdByUserId.value.toString,
          assignedToUserId = ticket.assignedToUserId.map(_.value.toString),
          lastModifiedByUserId = ticket.lastModifiedByUserId.value.toString,
          createdAt = ticket.createdAt.toString,
          modifiedAt = ticket.modifiedAt.toString,
          stateId = ticket.stateId,
          commentsCount = comments.count(_.ticketId == ticket.id),
          board = TicketQueryBoardRow(
            id = board.id.value.toString,
            name = board.name.value,
            active = board.active
          ),
          createdBy = toUserRow(createdBy),
          assignedTo = ticket.assignedToUserId.flatMap(id => users.find(_.id == id).map(toUserRow)),
          lastModifiedBy = toUserRow(modifiedBy),
          timeEntries = timeEntries
            .filter(_.ticketId == ticket.id)
            .sortBy(_.loggedAt)(Ordering[java.time.Instant].reverse)
            .flatMap { entry =>
              for {
                user     <- users.find(_.id == entry.userId)
                activity <- activities.find(_.id == entry.activityId)
              } yield TicketQueryTimeEntryRow(
                id = entry.id,
                ticketId = entry.ticketId,
                userId = entry.userId.value.toString,
                activityId = entry.activityId,
                activityCode = activity.code.value,
                activityName = activity.name.value,
                durationMinutes = entry.durationMinutes.value,
                loggedAt = entry.loggedAt.toString,
                description = entry.description.map(_.value),
                user = toUserRow(user)
              )
            }
        )
      }
      .pure[F]

  private def toUserRow(user: io.github.oleksiybondar.api.domain.user.User): TicketQueryUserRow =
    TicketQueryUserRow(
      id = user.id.value.toString,
      username = user.username.map(_.value),
      email = user.email.map(_.value),
      firstName = user.firstName.value,
      lastName = user.lastName.value,
      avatarUrl = user.avatarUrl.map(_.value),
      createdAt = user.createdAt.toString
    )
}
