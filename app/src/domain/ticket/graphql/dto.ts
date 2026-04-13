export interface TicketUserSummaryResponse {
  avatarUrl: string | null;
  firstName: string;
  id: string;
  lastName: string;
}

export interface TicketBoardSummaryResponse {
  active: boolean;
  id: string;
  name: string;
}

export interface TicketCommentTicketSummaryResponse {
  boardId: string;
  id: string;
  title: string;
}

export interface TicketCommentResponse {
  authorUserId: string;
  createdAt: string;
  id: string;
  message: string;
  modifiedAt: string;
  relatedCommentId: string | null;
  ticket: TicketCommentTicketSummaryResponse | null;
  ticketId: string;
  user: TicketUserSummaryResponse | null;
}

export interface TicketTimeTrackingTicketSummaryResponse {
  description: string | null;
  id: string;
  title: string;
}

export interface TicketTimeTrackingEntryResponse {
  activityCode: string | null;
  activityId: string;
  activityName: string | null;
  description: string | null;
  durationMinutes: number;
  id: string;
  loggedAt: string;
  ticket: TicketTimeTrackingTicketSummaryResponse | null;
  ticketId: string;
  user: TicketUserSummaryResponse | null;
  userId: string;
}

export interface TicketResponse {
  acceptanceCriteria: string | null;
  assignedTo: TicketUserSummaryResponse | null;
  assignedToUserId: string | null;
  board: TicketBoardSummaryResponse | null;
  boardId: string;
  comments: TicketCommentResponse[];
  commentsCount: number;
  createdAt: string;
  createdBy: TicketUserSummaryResponse | null;
  createdByUserId: string;
  description: string | null;
  estimatedMinutes: number | null;
  id: string;
  lastModifiedBy: TicketUserSummaryResponse | null;
  lastModifiedByUserId: string;
  modifiedAt: string;
  name: string;
  status: string | null;
  timeEntries: TicketTimeTrackingEntryResponse[];
  trackedMinutes: number;
}

export interface TicketQueryResponse {
  ticket: TicketResponse | null;
}

export interface TicketsQueryResponse {
  tickets: TicketResponse[];
}

export interface CreateTicketMutationResponse {
  createTicket: TicketResponse;
}

export interface ChangeTicketTitleMutationResponse {
  changeTicketTitle: TicketResponse;
}

export interface ChangeTicketDescriptionMutationResponse {
  changeTicketDescription: TicketResponse;
}

export interface ChangeTicketAcceptanceCriteriaMutationResponse {
  changeTicketAcceptanceCriteria: TicketResponse;
}

export interface ChangeTicketEstimatedTimeMutationResponse {
  changeTicketEstimatedTime: TicketResponse;
}

export interface ChangeTicketStatusMutationResponse {
  changeTicketStatus: TicketResponse;
}

export interface ReassignTicketMutationResponse {
  reassignTicket: TicketResponse;
}
