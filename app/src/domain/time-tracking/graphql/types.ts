export interface TimeTrackingUserSummary {
  avatarUrl: string | null;
  firstName: string;
  lastName: string;
  userId: string;
}

export interface TimeTrackingTicketSummary {
  board: TimeTrackingBoardSummary | null;
  description: string | null;
  ticketId: string;
  title: string;
}

export interface TimeTrackingBoardSummary {
  active: boolean;
  boardId: string;
  title: string;
}

export interface TimeTrackingEntry {
  activityCode: string | null;
  activityId: string;
  activityName: string | null;
  description: string | null;
  durationMinutes: number;
  entryId: string;
  loggedAt: string;
  ticket: TimeTrackingTicketSummary | null;
  ticketId: string;
  user: TimeTrackingUserSummary | null;
  userId: string;
}
