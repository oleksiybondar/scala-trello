import type {
  TicketCommentResponse,
  TicketResponse,
  TicketTimeTrackingEntryResponse,
  TicketUserSummaryResponse
} from "@models/ticket/dto";
import type {
  Ticket,
  TicketComment,
  TicketCommentTicketSummary,
  TicketTimeTrackingEntry,
  TicketTimeTrackingTicketSummary,
  TicketUserSummary
} from "@models/ticket/types";

const mapTicketUserSummary = (
  response: TicketUserSummaryResponse | null
): TicketUserSummary | null => {
  if (response === null) {
    return null;
  }

  return {
    avatarUrl: response.avatarUrl,
    firstName: response.firstName,
    lastName: response.lastName,
    userId: response.id
  };
};

const mapTicketCommentTicketSummary = (
  response: TicketCommentResponse["ticket"]
): TicketCommentTicketSummary | null => {
  if (response === null) {
    return null;
  }

  return {
    boardId: response.boardId,
    ticketId: response.id,
    title: response.title
  };
};

const mapTicketComment = (response: TicketCommentResponse): TicketComment => {
  return {
    authorUserId: response.authorUserId,
    commentId: response.id,
    createdAt: response.createdAt,
    message: response.message,
    modifiedAt: response.modifiedAt,
    relatedCommentId: response.relatedCommentId,
    ticket: mapTicketCommentTicketSummary(response.ticket),
    ticketId: response.ticketId,
    user: mapTicketUserSummary(response.user)
  };
};

const mapTicketTimeTrackingTicketSummary = (
  response: TicketTimeTrackingEntryResponse["ticket"]
): TicketTimeTrackingTicketSummary | null => {
  if (response === null) {
    return null;
  }

  return {
    description: response.description,
    ticketId: response.id,
    title: response.title
  };
};

const mapTicketTimeTrackingEntry = (
  response: TicketTimeTrackingEntryResponse
): TicketTimeTrackingEntry => {
  return {
    activityCode: response.activityCode,
    activityId: response.activityId,
    activityName: response.activityName,
    description: response.description,
    durationMinutes: response.durationMinutes,
    entryId: response.id,
    loggedAt: response.loggedAt,
    ticket: mapTicketTimeTrackingTicketSummary(response.ticket),
    ticketId: response.ticketId,
    user: mapTicketUserSummary(response.user),
    userId: response.userId
  };
};

export const mapTicketResponseToTicket = (response: TicketResponse): Ticket => {
  return {
    acceptanceCriteria: response.acceptanceCriteria,
    assignedTo: mapTicketUserSummary(response.assignedTo),
    assignedToUserId: response.assignedToUserId,
    board:
      response.board === null
        ? null
        : {
            active: response.board.active,
            boardId: response.board.id,
            name: response.board.name
          },
    boardId: response.boardId,
    comments: response.comments.map(mapTicketComment),
    commentsCount: response.commentsCount,
    createdAt: response.createdAt,
    createdBy: mapTicketUserSummary(response.createdBy),
    createdByUserId: response.createdByUserId,
    description: response.description,
    estimatedMinutes: response.estimatedMinutes,
    lastModifiedBy: mapTicketUserSummary(response.lastModifiedBy),
    lastModifiedByUserId: response.lastModifiedByUserId,
    modifiedAt: response.modifiedAt,
    name: response.name,
    status: response.status,
    ticketId: response.id,
    timeEntries: response.timeEntries.map(mapTicketTimeTrackingEntry),
    trackedMinutes: response.trackedMinutes
  };
};
