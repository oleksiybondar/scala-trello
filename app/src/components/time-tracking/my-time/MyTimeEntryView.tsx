import type { ReactElement } from "react";

import DeleteOutlineOutlinedIcon from "@mui/icons-material/DeleteOutlineOutlined";
import EditOutlinedIcon from "@mui/icons-material/EditOutlined";
import Button from "@mui/material/Button";
import Chip from "@mui/material/Chip";
import Link from "@mui/material/Link";
import Paper from "@mui/material/Paper";
import Stack from "@mui/material/Stack";
import Typography from "@mui/material/Typography";
import { alpha, useTheme } from "@mui/material/styles";
import type { Theme } from "@mui/material/styles";
import { Link as RouterLink } from "react-router-dom";

import { formatMinutesToTimeTrackingDuration } from "@helpers/timeTrackingConversions";
import { resolveActivityThemeColorToken } from "@helpers/timeTrackingActivities";
import type { TimeTrackingEntry } from "../../../domain/time-tracking/graphql";

interface MyTimeEntryViewProps {
  entry: TimeTrackingEntry;
  isDeleting: boolean;
  onDelete: (entryId: string) => void;
  onEdit: (entryId: string) => void;
}

const resolveThemeColorByCode = (theme: Theme, code: string | null): string => {
  if (code === null) {
    return theme.palette.grey[500];
  }

  const token = resolveActivityThemeColorToken(code);

  switch (token) {
    case "error.main":
      return theme.palette.error.main;
    case "info.main":
      return theme.palette.info.main;
    case "primary.main":
      return theme.palette.primary.main;
    case "secondary.main":
      return theme.palette.secondary.main;
    case "success.main":
      return theme.palette.success.main;
    case "warning.main":
      return theme.palette.warning.main;
    default:
      return theme.palette.grey[500];
  }
};

const formatLoggedAt = (value: string): string => {
  const parsedDate = new Date(value);

  if (Number.isNaN(parsedDate.getTime())) {
    return value;
  }

  return parsedDate.toLocaleString();
};

export const MyTimeEntryView = ({
  entry,
  isDeleting,
  onDelete,
  onEdit
}: MyTimeEntryViewProps): ReactElement => {
  const theme = useTheme();
  const color = resolveThemeColorByCode(theme, entry.activityCode);

  return (
    <Paper sx={{ p: 2 }} variant="outlined">
      <Stack spacing={1.5}>
        <Stack
          alignItems={{ sm: "center", xs: "flex-start" }}
          direction={{ sm: "row", xs: "column" }}
          justifyContent="space-between"
          spacing={1}
        >
          <Stack direction="row" spacing={1}>
            <Link
              component={RouterLink}
              to={`/tickets/${entry.ticketId}`}
              underline="hover"
              variant="body2"
            >
              {entry.ticket?.title ?? `Ticket #${entry.ticketId}`}
            </Link>
            {entry.ticket?.board !== null && entry.ticket?.board !== undefined ? (
              <Link
                color="text.secondary"
                component={RouterLink}
                to={`/boards/${entry.ticket.board.boardId}`}
                underline="hover"
                variant="body2"
              >
                {entry.ticket.board.title}
              </Link>
            ) : null}
          </Stack>
          <Stack direction="row" spacing={1}>
            <Button
              onClick={() => {
                onEdit(entry.entryId);
              }}
              size="small"
              startIcon={<EditOutlinedIcon fontSize="small" />}
              variant="outlined"
            >
              Edit
            </Button>
            <Button
              color="error"
              disabled={isDeleting}
              onClick={() => {
                onDelete(entry.entryId);
              }}
              size="small"
              startIcon={<DeleteOutlineOutlinedIcon fontSize="small" />}
              variant="outlined"
            >
              {isDeleting ? "Deleting..." : "Delete"}
            </Button>
          </Stack>
        </Stack>

        <Stack
          alignItems={{ sm: "center", xs: "flex-start" }}
          direction={{ sm: "row", xs: "column" }}
          spacing={1}
        >
          <Chip
            label={entry.activityName ?? entry.activityCode ?? entry.activityId}
            size="small"
            sx={{
              backgroundColor: alpha(color, 0.14),
              border: `1px solid ${alpha(color, 0.35)}`,
              color
            }}
            variant="outlined"
          />
          <Typography variant="body2">
            {formatMinutesToTimeTrackingDuration(Math.max(0, entry.durationMinutes))}
          </Typography>
          <Typography color="text.secondary" variant="body2">
            {formatLoggedAt(entry.loggedAt)}
          </Typography>
        </Stack>

        <Typography color={entry.description === null ? "text.secondary" : "text.primary"} variant="body2">
          {entry.description ?? "No description"}
        </Typography>
      </Stack>
    </Paper>
  );
};
