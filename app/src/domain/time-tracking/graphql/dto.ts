export interface TimeTrackingUserSummaryResponse {
  avatarUrl: string | null;
  firstName: string;
  id: string;
  lastName: string;
}

export interface TimeTrackingTicketSummaryResponse {
  board?: TimeTrackingBoardSummaryResponse | null;
  description: string | null;
  id: string;
  title: string;
}

export interface TimeTrackingBoardSummaryResponse {
  active: boolean;
  id: string;
  title: string;
}

export interface TimeTrackingEntryResponse {
  activityCode: string | null;
  activityId: string;
  activityName: string | null;
  description: string | null;
  durationMinutes: number;
  id: string;
  loggedAt: string;
  ticket: TimeTrackingTicketSummaryResponse | null;
  ticketId: string;
  user: TimeTrackingUserSummaryResponse | null;
  userId: string;
}

export interface TimeTrackingEntryQueryResponse {
  timeTrackingEntry: TimeTrackingEntryResponse | null;
}

export interface TimeTrackingEntriesByUserQueryResponse {
  timeTrackingEntriesByUser: TimeTrackingEntryResponse[];
}

export interface TimeTrackingEntriesByTicketQueryResponse {
  timeTrackingEntriesByTicket: TimeTrackingEntryResponse[];
}

export interface CreateTimeTrackingEntryMutationResponse {
  createTimeTrackingEntry: TimeTrackingEntryResponse;
}

export interface UpdateTimeTrackingActivityMutationResponse {
  updateTimeTrackingActivity: TimeTrackingEntryResponse;
}

export interface UpdateTimeTrackingDescriptionMutationResponse {
  updateTimeTrackingDescription: TimeTrackingEntryResponse;
}

export interface UpdateTimeTrackingTimeMutationResponse {
  updateTimeTrackingTime: TimeTrackingEntryResponse;
}

export interface DeleteTimeTrackingEntryMutationResponse {
  deleteTimeTrackingEntry: boolean;
}
