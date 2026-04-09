package io.github.oleksiybondar.api.domain.timeTracking

import cats.effect.kernel.MonadCancelThrow
import cats.syntax.all._
import io.github.oleksiybondar.api.domain.board.BoardMembershipService
import io.github.oleksiybondar.api.domain.user.UserId
import io.github.oleksiybondar.api.infrastructure.db.board.BoardRepo
import io.github.oleksiybondar.api.infrastructure.db.ticket.TicketRepo
import io.github.oleksiybondar.api.infrastructure.db.timeTracking.{
  TimeTrackingActivityRepo,
  TimeTrackingRepo
}

final class TimeTrackingServiceLive[F[_]: MonadCancelThrow](
    timeTrackingRepo: TimeTrackingRepo[F],
    ticketRepo: TicketRepo[F],
    boardRepo: BoardRepo[F],
    boardMembershipService: BoardMembershipService[F],
    timeTrackingActivityRepo: TimeTrackingActivityRepo[F]
) extends TimeTrackingService[F] {

  override def createEntry(
      command: CreateTimeTrackingEntryCommand,
      actorUserId: UserId
  ): F[Option[TimeTrackingEntry]] =
    canMutateForTicket(command.ticketId, actorUserId).flatMap {
      case false => none[TimeTrackingEntry].pure[F]
      case true  =>
        timeTrackingActivityRepo.findById(command.activityId).flatMap {
          case None    => none[TimeTrackingEntry].pure[F]
          case Some(_) =>
            for {
              id   <- timeTrackingRepo.nextId
              entry = TimeTrackingEntry(
                        id = id,
                        ticketId = command.ticketId,
                        userId = actorUserId,
                        activityId = command.activityId,
                        durationMinutes = command.durationMinutes,
                        loggedAt = command.loggedAt,
                        description = command.description
                      )
              _    <- timeTrackingRepo.create(entry)
            } yield Some(entry)
        }
    }

  override def getOwnEntry(
      id: TimeTrackingEntryId,
      actorUserId: UserId
  ): F[Option[TimeTrackingEntry]] =
    timeTrackingRepo.findById(id).map(_.filter(_.userId == actorUserId))

  override def listOwnEntries(actorUserId: UserId): F[List[TimeTrackingEntry]] =
    timeTrackingRepo.listByUser(actorUserId)

  override def updateOwnEntry(
      id: TimeTrackingEntryId,
      command: UpdateTimeTrackingEntryCommand,
      actorUserId: UserId
  ): F[Boolean] =
    timeTrackingRepo.findById(id).flatMap {
      case Some(existing) if existing.userId == actorUserId =>
        canMutateForTicket(existing.ticketId, actorUserId).flatMap {
          case false => false.pure[F]
          case true  =>
            timeTrackingActivityRepo.findById(command.activityId).flatMap {
              case None    => false.pure[F]
              case Some(_) =>
                timeTrackingRepo.update(
                  existing.copy(
                    activityId = command.activityId,
                    durationMinutes = command.durationMinutes,
                    loggedAt = command.loggedAt,
                    description = command.description
                  )
                )
            }
        }
      case _                                                => false.pure[F]
    }

  override def deleteOwnEntry(id: TimeTrackingEntryId, actorUserId: UserId): F[Boolean] =
    timeTrackingRepo.findById(id).flatMap {
      case Some(existing) if existing.userId == actorUserId =>
        canMutateForTicket(existing.ticketId, actorUserId).flatMap {
          case false => false.pure[F]
          case true  => timeTrackingRepo.delete(id)
        }
      case _                                                => false.pure[F]
    }

  private def canMutateForTicket(
      ticketId: io.github.oleksiybondar.api.domain.ticket.TicketId,
      actorUserId: UserId
  ): F[Boolean] =
    ticketRepo.findById(ticketId).flatMap {
      case None         => false.pure[F]
      case Some(ticket) =>
        boardRepo.findById(ticket.boardId).flatMap {
          case Some(board) if board.active =>
            boardMembershipService.findMember(ticket.boardId, actorUserId).map(_.nonEmpty)
          case _                           => false.pure[F]
        }
    }
}
