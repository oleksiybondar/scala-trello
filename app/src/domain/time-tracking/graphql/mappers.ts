import type {
  TimeTrackingEntryResponse,
  TimeTrackingUserSummaryResponse
} from "./dto";
import type {
  TimeTrackingEntry,
  TimeTrackingTicketSummary,
  TimeTrackingUserSummary
} from "./types";

const mapTimeTrackingUserSummary = (
  response: TimeTrackingUserSummaryResponse | null
): TimeTrackingUserSummary | null => {
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

const mapTimeTrackingTicketSummary = (
  response: TimeTrackingEntryResponse["ticket"]
): TimeTrackingTicketSummary | null => {
  if (response === null) {
    return null;
  }

  return {
    description: response.description,
    ticketId: response.id,
    title: response.title
  };
};

export const mapTimeTrackingEntryResponseToTimeTrackingEntry = (
  response: TimeTrackingEntryResponse
): TimeTrackingEntry => {
  return {
    activityCode: response.activityCode,
    activityId: response.activityId,
    activityName: response.activityName,
    description: response.description,
    durationMinutes: response.durationMinutes,
    entryId: response.id,
    loggedAt: response.loggedAt,
    ticket: mapTimeTrackingTicketSummary(response.ticket),
    ticketId: response.ticketId,
    user: mapTimeTrackingUserSummary(response.user),
    userId: response.userId
  };
};
