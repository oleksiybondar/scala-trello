package io.github.oleksiybondar.api.domain.ticket

import cats.effect.kernel.Temporal
import cats.syntax.all._
import io.github.oleksiybondar.api.domain.board.BoardAccessService
import io.github.oleksiybondar.api.domain.user.UserId
import io.github.oleksiybondar.api.infrastructure.db.ticket.TicketRepo

final class TicketServiceLive[F[_]: Temporal](
    ticketRepo: TicketRepo[F],
    boardAccessService: BoardAccessService[F]
) extends TicketService[F] {

  override def createTicket(ticket: Ticket, actorUserId: UserId): F[Boolean] =
    boardAccessService.canCreateTicket(ticket.boardId, actorUserId).flatMap {
      case false => false.pure[F]
      case true  =>
        Temporal[F].realTimeInstant.flatMap { now =>
          ticketRepo
            .create(
              ticket.copy(
                createdByUserId = actorUserId,
                lastModifiedByUserId = actorUserId,
                createdAt = now,
                modifiedAt = now
              )
            )
            .as(true)
        }
    }

  override def getTicket(id: TicketId): F[Option[Ticket]] =
    ticketRepo.findById(id)

  override def modifyTicket(ticket: Ticket, actorUserId: UserId): F[Boolean] =
    ticketRepo.findById(ticket.id).flatMap {
      case None           => false.pure[F]
      case Some(existing) =>
        boardAccessService.canModifyTicket(existing.boardId, actorUserId).flatMap {
          case false => false.pure[F]
          case true  =>
            Temporal[F].realTimeInstant.flatMap { now =>
              ticketRepo.update(
                existing.copy(
                  name = ticket.name,
                  description = ticket.description,
                  component = ticket.component,
                  scope = ticket.scope,
                  acceptanceCriteria = ticket.acceptanceCriteria,
                  assignedToUserId = ticket.assignedToUserId,
                  originalEstimatedMinutes = ticket.originalEstimatedMinutes,
                  priority = ticket.priority,
                  severityId = ticket.severityId,
                  stateId = ticket.stateId,
                  commentsEnabled = ticket.commentsEnabled,
                  modifiedAt = now,
                  lastModifiedByUserId = actorUserId
                )
              )
            }
        }
    }

  override def reassignTicket(
      ticketId: TicketId,
      actorUserId: UserId,
      assignedToUserId: Option[UserId]
  ): F[Boolean] =
    ticketRepo.findById(ticketId).flatMap {
      case None           => false.pure[F]
      case Some(existing) =>
        boardAccessService.canReassignTicket(existing.boardId, actorUserId).flatMap {
          case false => false.pure[F]
          case true  =>
            Temporal[F].realTimeInstant.flatMap { now =>
              ticketRepo.update(
                existing.copy(
                  assignedToUserId = assignedToUserId,
                  modifiedAt = now,
                  lastModifiedByUserId = actorUserId
                )
              )
            }
        }
    }

  override def deleteTicket(ticketId: TicketId, actorUserId: UserId): F[Boolean] =
    ticketRepo.findById(ticketId).flatMap {
      case None           => false.pure[F]
      case Some(existing) =>
        boardAccessService.canDeleteTicket(existing.boardId, actorUserId).flatMap {
          case false => false.pure[F]
          case true  => ticketRepo.delete(ticketId)
        }
    }
}
