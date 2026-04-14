import type {
  BoardMemberResponse,
  BoardResponse,
  BoardUserSummaryResponse,
  CreateBoardRequest
} from "./dto";
import type {
  Board,
  BoardMember,
  BoardPermission,
  BoardRole,
  BoardTicket,
  BoardTimeTrackingEntry,
  BoardTimeTrackingTicketSummary,
  BoardUserSummary,
  CreateBoardInput
} from "./types";

const mapBoardUserSummary = (
  response: BoardUserSummaryResponse | null
): BoardUserSummary | null => {
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

const mapBoardPermission = (
  response: BoardMemberResponse["role"]["permissions"][number]
): BoardPermission => {
  return {
    area: response.area,
    canCreate: response.canCreate,
    canDelete: response.canDelete,
    canModify: response.canModify,
    canRead: response.canRead,
    canReassign: response.canReassign
  };
};

const mapBoardRole = (response: BoardResponse["currentUserRole"]): BoardRole | null => {
  if (response === null) {
    return null;
  }

  return {
    description: response.description,
    permissions: response.permissions.map(mapBoardPermission),
    roleId: response.id,
    roleName: response.name
  };
};

const mapBoardTimeTrackingTicketSummary = (
  response: BoardResponse["tickets"][number]["timeEntries"][number]["ticket"]
): BoardTimeTrackingTicketSummary | null => {
  if (response === null) {
    return null;
  }

  return {
    description: response.description,
    ticketId: response.id,
    title: response.title
  };
};

const mapBoardTimeTrackingEntry = (
  response: BoardResponse["tickets"][number]["timeEntries"][number]
): BoardTimeTrackingEntry => {
  return {
    activityCode: response.activityCode,
    activityId: response.activityId,
    activityName: response.activityName,
    description: response.description,
    durationMinutes: response.durationMinutes,
    entryId: response.id,
    loggedAt: response.loggedAt,
    ticket: mapBoardTimeTrackingTicketSummary(response.ticket),
    ticketId: response.ticketId,
    user: mapBoardUserSummary(response.user),
    userId: response.userId
  };
};

const mapBoardTicket = (
  response: BoardResponse["tickets"][number]
): BoardTicket => {
  return {
    acceptanceCriteria: response.acceptanceCriteria,
    assignedTo: mapBoardUserSummary(response.assignedTo),
    assignedToUserId: response.assignedToUserId,
    boardId: response.boardId,
    commentsCount: response.commentsCount,
    createdAt: response.createdAt,
    createdBy: mapBoardUserSummary(response.createdBy),
    createdByUserId: response.createdByUserId,
    description: response.description,
    estimatedMinutes: response.estimatedMinutes,
    lastModifiedBy: mapBoardUserSummary(response.lastModifiedBy),
    lastModifiedByUserId: response.lastModifiedByUserId,
    modifiedAt: response.modifiedAt,
    name: response.name,
    priority: response.priority,
    severityId: response.severityId,
    severityName: response.severityName,
    status: response.status,
    ticketId: response.id,
    timeEntries: response.timeEntries.map(mapBoardTimeTrackingEntry),
    trackedMinutes: response.trackedMinutes
  };
};

export const mapBoardResponseToBoard = (
  response: BoardResponse
): Board => {
  return {
    active: response.active,
    boardId: response.id,
    createdAt: response.createdAt,
    createdBy: mapBoardUserSummary(response.createdBy),
    createdByUserId: response.createdByUserId,
    currentUserRole: mapBoardRole(response.currentUserRole),
    description: response.description,
    lastModifiedByUserId: response.lastModifiedByUserId,
    membersCount: response.membersCount,
    modifiedAt: response.modifiedAt,
    name: response.name,
    owner: mapBoardUserSummary(response.owner),
    ownerUserId: response.ownerUserId,
    tickets: response.tickets.map(mapBoardTicket)
  };
};

export const mapBoardMemberResponseToBoardMember = (
  response: BoardMemberResponse
): BoardMember => {
  return {
    boardId: response.boardId,
    createdAt: response.createdAt,
    role: {
      description: response.role.description,
      permissions: response.role.permissions.map(mapBoardPermission),
      roleId: response.role.id,
      roleName: response.role.name
    },
    user: mapBoardUserSummary(response.user),
    userId: response.userId
  };
};

export const mapCreateBoardInputToRequest = (
  input: CreateBoardInput
): CreateBoardRequest => {
  const description = input.description?.trim() ?? "";

  return {
    ...(description.length > 0 ? { description } : {}),
    name: input.name.trim()
  };
};
