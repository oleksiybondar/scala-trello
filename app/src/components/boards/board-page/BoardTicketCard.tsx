import { useState } from "react";
import type { DragEvent, MouseEvent, ReactElement } from "react";

import AccessTimeRoundedIcon from "@mui/icons-material/AccessTimeRounded";
import MoreVertRoundedIcon from "@mui/icons-material/MoreVertRounded";
import Box from "@mui/material/Box";
import Divider from "@mui/material/Divider";
import IconButton from "@mui/material/IconButton";
import Link from "@mui/material/Link";
import Menu from "@mui/material/Menu";
import MenuItem from "@mui/material/MenuItem";
import Paper from "@mui/material/Paper";
import Stack from "@mui/material/Stack";
import Tooltip from "@mui/material/Tooltip";
import Typography from "@mui/material/Typography";
import { useTheme } from "@mui/material/styles";
import { Link as RouterLink } from "react-router-dom";

import { TicketAssigneeMenu } from "@components/boards/board-page/TicketAssigneeMenu";
import {
  getPriorityMeta,
  getSeverityMeta,
  resolveMetadataToneColor
} from "@components/tickets/ticketMetadata";
import { boardTicketStates } from "@helpers/boardTicketState";
import { formatMinutesToTimeInput } from "@helpers/timeTrackingConversions";
import { normalizeUiTicketStatus } from "@helpers/uiTicketStatus";
import { useBoard } from "@hooks/useBoard";
import { useTickets } from "@hooks/useTickets";
import { useTimeTracking } from "@hooks/useTimeTracking";
import type { Ticket } from "../../../domain/ticket/graphql";

interface BoardTicketCardProps {
  onDragEnd: () => void;
  onDragStart: (event: DragEvent<HTMLDivElement>, ticketId: string) => void;
  ticket: Ticket;
}

const TITLE_LINE_HEIGHT = 1.3;
const TITLE_LINES = 2;
const DESCRIPTION_LINE_HEIGHT = 1.2;
const DESCRIPTION_LINES = 4;

export const BoardTicketCard = ({
  onDragEnd,
  onDragStart,
  ticket
}: BoardTicketCardProps): ReactElement => {
  const [anchorEl, setAnchorEl] = useState<HTMLElement | null>(null);
  const theme = useTheme();
  const { boardPermissionAccess, members } = useBoard();
  const { reassignTicket, transitionTicketState } = useTickets();
  const { openLogTimeModal } = useTimeTracking();
  const normalizedStatus = normalizeUiTicketStatus(ticket.status);
  const trackedTimeLabel = formatMinutesToTimeInput(ticket.trackedMinutes) || "00:00";
  const hasLongTitle = ticket.name.trim().length > 60;
  const hasLongDescription =
    ticket.description !== null && ticket.description.trim().length > 160;
  const severityMeta = getSeverityMeta(ticket.severityName);
  const priorityMeta = getPriorityMeta(ticket.priority);
  const SeverityIcon = severityMeta?.icon;
  const PriorityIcon = priorityMeta?.icon;

  const handleOpenStateMenu = (event: MouseEvent<HTMLButtonElement>): void => {
    setAnchorEl(event.currentTarget);
  };

  const handleCloseStateMenu = (): void => {
    setAnchorEl(null);
  };

  const handleStateChange = (status: (typeof boardTicketStates)[number]["status"]): void => {
    void transitionTicketState(ticket.ticketId, status);
    handleCloseStateMenu();
  };

  const handleReassign = (assignedToUserId: string | null): void => {
    void reassignTicket(ticket.ticketId, assignedToUserId);
  };

  return (
    <Paper
      draggable
      onDragEnd={onDragEnd}
      onDragStart={event => {
        onDragStart(event, ticket.ticketId);
      }}
      sx={{
        cursor: "grab",
        height: 208,
        minHeight: 208,
        overflow: "hidden",
        p: 1.5
      }}
      variant="outlined"
    >
      <Stack spacing={1.5} sx={{ height: "100%" }}>
        <Stack alignItems="flex-start" direction="row" justifyContent="space-between" spacing={1}>
          <Box
            sx={{
              flex: 1,
              minHeight: `${String(TITLE_LINE_HEIGHT * TITLE_LINES)}em`
            }}
          >
            <Stack alignItems="flex-start" direction="row" spacing={0.75}>
              {severityMeta !== null && SeverityIcon !== undefined ? (
                <Tooltip title={`Severity: ${severityMeta.label}`}>
                  <Box
                    sx={{
                      color: resolveMetadataToneColor(severityMeta.tone, theme.palette),
                      display: "flex",
                      flexShrink: 0,
                      mt: 0.15
                    }}
                  >
                    <SeverityIcon fontSize="small" />
                  </Box>
                </Tooltip>
              ) : null}
              {priorityMeta !== null && PriorityIcon !== undefined ? (
                <Tooltip title={`Priority: ${priorityMeta.label}`}>
                  <Box
                    sx={{
                      color: resolveMetadataToneColor(priorityMeta.tone, theme.palette),
                      display: "flex",
                      flexShrink: 0,
                      mt: 0.15
                    }}
                  >
                    <PriorityIcon fontSize="small" />
                  </Box>
                </Tooltip>
              ) : null}
              <Tooltip
                disableHoverListener={!hasLongTitle}
                slotProps={{
                  tooltip: {
                    sx: {
                      maxWidth: 320
                    }
                  }
                }}
                title={ticket.name}
              >
                <Link
                  className="BoardTicketCard-title"
                  color="text.primary"
                  component={RouterLink}
                  fontWeight={700}
                  onClick={event => {
                    event.stopPropagation();
                  }}
                  sx={{
                    display: "-webkit-box",
                    flex: 1,
                    lineHeight: String(TITLE_LINE_HEIGHT),
                    overflow: "hidden",
                    textOverflow: "ellipsis",
                    WebkitBoxOrient: "vertical",
                    WebkitLineClamp: TITLE_LINES
                  }}
                  to={`/tickets/${ticket.ticketId}`}
                  underline="hover"
                  variant="body2"
                >
                  {ticket.name}
                </Link>
              </Tooltip>
            </Stack>
          </Box>
          <Tooltip title="Move ticket">
            <IconButton onClick={handleOpenStateMenu} size="small">
              <MoreVertRoundedIcon fontSize="small" />
            </IconButton>
          </Tooltip>
          <Menu anchorEl={anchorEl} onClose={handleCloseStateMenu} open={anchorEl !== null}>
            {boardTicketStates
              .filter(state => state.status !== normalizedStatus)
              .map(state => (
                <MenuItem
                  key={state.key}
                  onClick={() => {
                    handleStateChange(state.status);
                  }}
                >
                  {state.title}
                </MenuItem>
              ))}
          </Menu>
        </Stack>

        <Box
          sx={{
            minHeight: `${String(DESCRIPTION_LINE_HEIGHT * DESCRIPTION_LINES)}em`
          }}
        >
          {ticket.description !== null && ticket.description.trim().length > 0 ? (
            <Tooltip
              disableHoverListener={!hasLongDescription}
              slotProps={{
                tooltip: {
                  sx: {
                    maxWidth: 320
                  }
                }
              }}
              title={ticket.description}
            >
              <Typography
                className="BoardTicketCard-description"
                sx={{
                  display: "-webkit-box",
                  lineHeight: String(DESCRIPTION_LINE_HEIGHT),
                  overflow: "hidden",
                  textOverflow: "ellipsis",
                  WebkitBoxOrient: "vertical",
                  WebkitLineClamp: DESCRIPTION_LINES
                }}
                variant="caption"
              >
                {ticket.description}
              </Typography>
            </Tooltip>
          ) : (
            <Typography
              sx={{
                lineHeight: String(DESCRIPTION_LINE_HEIGHT),
                visibility: "hidden"
              }}
              variant="caption"
            >
              {"\u00A0"}
            </Typography>
          )}
        </Box>

        <Divider sx={{ mt: "auto" }} />

        <Stack
          alignItems="center"
          direction="row"
          justifyContent="space-between"
          spacing={1}
          sx={{ pb: 0.25 }}
        >
          <Box sx={{ flex: 1, minWidth: 0 }}>
            {boardPermissionAccess.canReassign ? (
              <TicketAssigneeMenu
                assignedTo={ticket.assignedTo}
                assignedToUserId={ticket.assignedToUserId}
                members={members}
                onReassign={handleReassign}
              />
            ) : (
              <Typography color="text.secondary" variant="caption">
                {ticket.assignedTo === null
                  ? "Unassigned"
                  : `${ticket.assignedTo.firstName} ${ticket.assignedTo.lastName}`}
              </Typography>
            )}
          </Box>

          <Stack alignItems="center" direction="row" spacing={0.5}>
            <Typography color="text.secondary" variant="caption">
              {trackedTimeLabel}
            </Typography>
            <Tooltip title="Log time">
              <IconButton
                onClick={() => {
                  openLogTimeModal(ticket.ticketId);
                }}
                size="small"
              >
                <AccessTimeRoundedIcon fontSize="small" />
              </IconButton>
            </Tooltip>
          </Stack>
        </Stack>
      </Stack>
    </Paper>
  );
};
