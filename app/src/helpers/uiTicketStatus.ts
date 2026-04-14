export type UiTicketStatus =
  | "new"
  | "in progress"
  | "code review"
  | "in testing"
  | "done";

export type UiTicketStateKey =
  | "new"
  | "in_progress"
  | "code_review"
  | "in_testing"
  | "done";

export const normalizeUiTicketStatus = (
  status: string | null
): UiTicketStatus | null => {
  switch (status?.trim().toLowerCase()) {
    case "new":
      return "new";
    case "in progress":
    case "in_progress":
      return "in progress";
    case "code review":
    case "code_review":
      return "code review";
    case "in testing":
    case "in_testing":
      return "in testing";
    case "done":
      return "done";
    default:
      return null;
  }
};

export const mapUiTicketStatusToStateKey = (
  status: string | null
): UiTicketStateKey | null => {
  const normalizedStatus = normalizeUiTicketStatus(status);

  switch (normalizedStatus) {
    case "new":
      return "new";
    case "in progress":
      return "in_progress";
    case "code review":
      return "code_review";
    case "in testing":
      return "in_testing";
    case "done":
      return "done";
    default:
      return null;
  }
};
