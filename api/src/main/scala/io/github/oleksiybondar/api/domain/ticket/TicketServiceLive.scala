package io.github.oleksiybondar.api.domain.ticket

import cats.effect.kernel.Temporal
import cats.syntax.all._
import io.github.oleksiybondar.api.domain.board.{BoardAccessService, BoardMembershipService}
import io.github.oleksiybondar.api.domain.user.UserId
import io.github.oleksiybondar.api.infrastructure.db.board.BoardRepo
import io.github.oleksiybondar.api.infrastructure.db.ticket.{TicketRepo, TicketStateRepo}

final class TicketServiceLive[F[_]: Temporal](
    ticketRepo: TicketRepo[F],
    ticketStateRepo: TicketStateRepo[F],
    boardRepo: BoardRepo[F],
    boardAccessService: BoardAccessService[F],
    boardMembershipService: BoardMembershipService[F]
) extends TicketService[F] {

  override def createTicket(
      command: CreateTicketCommand,
      actorUserId: UserId
  ): F[Option[Ticket]] =
    boardAccessService.canCreateTicket(command.boardId, actorUserId).flatMap {
      case false => none[Ticket].pure[F]
      case true  =>
        isActiveBoard(command.boardId).flatMap {
          case false => none[Ticket].pure[F]
          case true  =>
            ensureAssignableMember(command.boardId, command.assignedToUserId).flatMap {
              case false => none[Ticket].pure[F]
              case true  =>
                for {
                  id    <- ticketRepo.nextId
                  now   <- Temporal[F].realTimeInstant
                  ticket = Ticket(
                             id = id,
                             boardId = command.boardId,
                             name = command.name,
                             description = command.description,
                             component = command.component,
                             scope = command.scope,
                             acceptanceCriteria = command.acceptanceCriteria,
                             createdByUserId = actorUserId,
                             assignedToUserId = command.assignedToUserId,
                             lastModifiedByUserId = actorUserId,
                             createdAt = now,
                             modifiedAt = now,
                             originalEstimatedMinutes = command.originalEstimatedMinutes,
                             priority = command.priority,
                             severityId = command.severityId,
                             stateId = command.stateId,
                             commentsEnabled = true
                           )
                  _     <- ticketRepo.create(ticket)
                } yield Some(ticket)
            }
        }
    }

  override def getTicket(id: TicketId): F[Option[Ticket]] =
    ticketRepo.findById(id)

  override def listTickets(
      boardId: io.github.oleksiybondar.api.domain.board.BoardId,
      actorUserId: UserId
  ): F[List[Ticket]] =
    boardAccessService.canReadTicket(boardId, actorUserId).flatMap {
      case false => List.empty[Ticket].pure[F]
      case true  => ticketRepo.listByBoard(boardId)
    }

  override def changeTitle(
      ticketId: TicketId,
      actorUserId: UserId,
      title: TicketName
  ): F[Boolean] =
    updateTicket(ticketId, actorUserId)(_.copy(name = title))

  override def changeDescription(
      ticketId: TicketId,
      actorUserId: UserId,
      description: Option[TicketDescription]
  ): F[Boolean] =
    updateTicket(ticketId, actorUserId)(_.copy(description = description))

  override def changeAcceptanceCriteria(
      ticketId: TicketId,
      actorUserId: UserId,
      acceptanceCriteria: Option[TicketAcceptanceCriteria]
  ): F[Boolean] =
    updateTicket(ticketId, actorUserId)(_.copy(acceptanceCriteria = acceptanceCriteria))

  override def changeEstimatedTime(
      ticketId: TicketId,
      actorUserId: UserId,
      estimatedMinutes: Option[Int]
  ): F[Boolean] =
    updateTicket(ticketId, actorUserId)(_.copy(originalEstimatedMinutes = estimatedMinutes))

  override def changePriority(
      ticketId: TicketId,
      actorUserId: UserId,
      priority: Option[TicketPriority]
  ): F[Boolean] =
    updateTicket(ticketId, actorUserId)(_.copy(priority = priority))

  override def changeSeverity(
      ticketId: TicketId,
      actorUserId: UserId,
      severityId: Option[TicketSeverityId]
  ): F[Boolean] =
    updateTicket(ticketId, actorUserId)(_.copy(severityId = severityId))

  override def changeState(
      ticketId: TicketId,
      actorUserId: UserId,
      stateId: TicketStateId
  ): F[Boolean] =
    ticketStateRepo.findById(stateId).flatMap {
      case None        => false.pure[F]
      case Some(state) => updateTicket(ticketId, actorUserId)(_.copy(stateId = state.id))
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
            isActiveBoard(existing.boardId).flatMap {
              case false => false.pure[F]
              case true  =>
                ensureAssignableMember(existing.boardId, assignedToUserId).flatMap {
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
        }
    }

  override def deleteTicket(ticketId: TicketId, actorUserId: UserId): F[Boolean] =
    ticketRepo.findById(ticketId).flatMap {
      case None           => false.pure[F]
      case Some(existing) =>
        boardAccessService.canDeleteTicket(existing.boardId, actorUserId).flatMap {
          case false => false.pure[F]
          case true  =>
            isActiveBoard(existing.boardId).ifM(ticketRepo.delete(ticketId), false.pure[F])
        }
    }

  private def updateTicket(
      ticketId: TicketId,
      actorUserId: UserId
  )(update: Ticket => Ticket): F[Boolean] =
    ticketRepo.findById(ticketId).flatMap {
      case None           => false.pure[F]
      case Some(existing) =>
        boardAccessService.canModifyTicket(existing.boardId, actorUserId).flatMap {
          case false => false.pure[F]
          case true  =>
            isActiveBoard(existing.boardId).flatMap {
              case false => false.pure[F]
              case true  =>
                Temporal[F].realTimeInstant.flatMap { now =>
                  ticketRepo.update(
                    update(existing).copy(
                      modifiedAt = now,
                      lastModifiedByUserId = actorUserId
                    )
                  )
                }
            }
        }
    }

  private def isActiveBoard(
      boardId: io.github.oleksiybondar.api.domain.board.BoardId
  ): F[Boolean] =
    boardRepo.findById(boardId).map(_.exists(_.active))

  private def ensureAssignableMember(
      boardId: io.github.oleksiybondar.api.domain.board.BoardId,
      assignedToUserId: Option[UserId]
  ): F[Boolean] =
    assignedToUserId match {
      case None         => true.pure[F]
      case Some(userId) =>
        boardMembershipService.findMember(boardId, userId).map(_.nonEmpty)
    }
}
