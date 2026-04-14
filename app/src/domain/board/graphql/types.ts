export interface BoardUserSummary {
  avatarUrl: string | null;
  firstName: string;
  lastName: string;
  userId: string;
}

export interface BoardPermission {
  area: string;
  canCreate: boolean;
  canDelete: boolean;
  canModify: boolean;
  canRead: boolean;
  canReassign: boolean;
}

export interface BoardRole {
  description: string | null;
  permissions: BoardPermission[];
  roleId: string;
  roleName: string;
}

export interface BoardTimeTrackingTicketSummary {
  description: string | null;
  ticketId: string;
  title: string;
}

export interface BoardTimeTrackingEntry {
  activityCode: string | null;
  activityId: string;
  activityName: string | null;
  description: string | null;
  durationMinutes: number;
  entryId: string;
  loggedAt: string;
  ticket: BoardTimeTrackingTicketSummary | null;
  ticketId: string;
  user: BoardUserSummary | null;
  userId: string;
}

export interface BoardTicket {
  acceptanceCriteria: string | null;
  assignedTo: BoardUserSummary | null;
  assignedToUserId: string | null;
  boardId: string;
  commentsCount: number;
  createdAt: string;
  createdBy: BoardUserSummary | null;
  createdByUserId: string;
  description: string | null;
  estimatedMinutes: number | null;
  lastModifiedBy: BoardUserSummary | null;
  lastModifiedByUserId: string;
  modifiedAt: string;
  name: string;
  priority: number | null;
  severityId: string | null;
  severityName: string | null;
  status: string | null;
  ticketId: string;
  timeEntries: BoardTimeTrackingEntry[];
  trackedMinutes: number;
}

export interface Board {
  active: boolean;
  boardId: string;
  createdAt: string;
  createdBy: BoardUserSummary | null;
  createdByUserId: string;
  currentUserRole: BoardRole | null;
  description: string | null;
  lastModifiedByUserId: string;
  membersCount: number;
  modifiedAt: string;
  name: string;
  owner: BoardUserSummary | null;
  ownerUserId: string;
  tickets: BoardTicket[];
}

export interface CreateBoardInput {
  description: string | null;
  name: string;
}

export interface BoardMember {
  boardId: string;
  createdAt: string;
  role: BoardRole;
  user: BoardUserSummary | null;
  userId: string;
}
