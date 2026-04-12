export interface TicketUserSummary {
  avatarUrl: string | null;
  firstName: string;
  lastName: string;
  userId: string;
}

export interface TicketBoardSummary {
  active: boolean;
  boardId: string;
  name: string;
}

export interface TicketCommentTicketSummary {
  boardId: string;
  ticketId: string;
  title: string;
}

export interface TicketComment {
  authorUserId: string;
  commentId: string;
  createdAt: string;
  message: string;
  modifiedAt: string;
  relatedCommentId: string | null;
  ticket: TicketCommentTicketSummary | null;
  ticketId: string;
  user: TicketUserSummary | null;
}

export interface TicketTimeTrackingTicketSummary {
  description: string | null;
  ticketId: string;
  title: string;
}

export interface TicketTimeTrackingEntry {
  activityCode: string | null;
  activityId: string;
  activityName: string | null;
  description: string | null;
  durationMinutes: number;
  entryId: string;
  loggedAt: string;
  ticket: TicketTimeTrackingTicketSummary | null;
  ticketId: string;
  user: TicketUserSummary | null;
  userId: string;
}

export interface Ticket {
  acceptanceCriteria: string | null;
  assignedTo: TicketUserSummary | null;
  assignedToUserId: string | null;
  board: TicketBoardSummary | null;
  boardId: string;
  comments: TicketComment[];
  commentsCount: number;
  createdAt: string;
  createdBy: TicketUserSummary | null;
  createdByUserId: string;
  description: string | null;
  estimatedMinutes: number | null;
  lastModifiedBy: TicketUserSummary | null;
  lastModifiedByUserId: string;
  modifiedAt: string;
  name: string;
  status: string | null;
  ticketId: string;
  timeEntries: TicketTimeTrackingEntry[];
  trackedMinutes: number;
}
