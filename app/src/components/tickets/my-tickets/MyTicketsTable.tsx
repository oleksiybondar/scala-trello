import type { ReactElement } from "react";

import Box from "@mui/material/Box";
import Divider from "@mui/material/Divider";
import Link from "@mui/material/Link";
import Paper from "@mui/material/Paper";
import Stack from "@mui/material/Stack";
import Tooltip from "@mui/material/Tooltip";
import Typography from "@mui/material/Typography";
import { useTheme } from "@mui/material/styles";
import { Link as RouterLink } from "react-router-dom";

import { Person } from "@components/avatar/Person";
import {
  getPriorityMeta,
  getSeverityMeta,
  resolveMetadataToneColor
} from "@components/tickets/ticketMetadata";
import type { Ticket } from "../../../domain/ticket/graphql";

interface MyTicketsTableProps {
  tickets: Ticket[];
}

const ColumnHeader = ({ label }: { label: string }): ReactElement => {
  return (
    <Typography color="text.secondary" fontWeight={700} variant="caption">
      {label}
    </Typography>
  );
};

export const MyTicketsTable = ({ tickets }: MyTicketsTableProps): ReactElement => {
  const theme = useTheme();

  if (tickets.length === 0) {
    return (
      <Paper sx={{ p: 2 }} variant="outlined">
        <Typography color="text.secondary" variant="body2">
          No tickets found.
        </Typography>
      </Paper>
    );
  }

  return (
    <Paper sx={{ overflow: "hidden" }} variant="outlined">
      <Box
        sx={{
          display: { md: "grid", xs: "none" },
          gap: 2,
          gridTemplateColumns:
            "92px 92px minmax(240px, 2fr) minmax(170px, 1fr) minmax(170px, 1fr) minmax(170px, 1fr)",
          px: 2,
          py: 1.5
        }}
      >
        <ColumnHeader label="Severity" />
        <ColumnHeader label="Priority" />
        <ColumnHeader label="Ticket" />
        <ColumnHeader label="Board" />
        <ColumnHeader label="Created by" />
        <ColumnHeader label="Assigned to" />
      </Box>

      <Divider />

      <Stack divider={<Divider />} sx={{ p: 0 }}>
        {tickets.map(ticket => {
          const severityMeta = getSeverityMeta(ticket.severityName);
          const priorityMeta = getPriorityMeta(ticket.priority);
          const SeverityIcon = severityMeta?.icon;
          const PriorityIcon = priorityMeta?.icon;

          return (
            <Box
              key={ticket.ticketId}
              sx={{
                display: "grid",
                gap: 2,
                gridTemplateColumns: {
                  md: "92px 92px minmax(240px, 2fr) minmax(170px, 1fr) minmax(170px, 1fr) minmax(170px, 1fr)",
                  xs: "1fr"
                },
                px: 2,
                py: 1.5
              }}
            >
              <Stack alignItems="center" direction="row" spacing={1}>
                {severityMeta !== null && SeverityIcon !== undefined ? (
                  <Tooltip title={`Severity: ${severityMeta.label}`}>
                    <Box
                      sx={{
                        color: resolveMetadataToneColor(severityMeta.tone, theme.palette),
                        display: "flex"
                      }}
                    >
                      <SeverityIcon fontSize="small" />
                    </Box>
                  </Tooltip>
                ) : (
                  <Typography color="text.secondary" variant="caption">
                    -
                  </Typography>
                )}
              </Stack>

              <Stack alignItems="center" direction="row" spacing={1}>
                {priorityMeta !== null && PriorityIcon !== undefined ? (
                  <Tooltip title={`Priority: ${priorityMeta.label}`}>
                    <Box
                      sx={{
                        color: resolveMetadataToneColor(priorityMeta.tone, theme.palette),
                        display: "flex"
                      }}
                    >
                      <PriorityIcon fontSize="small" />
                    </Box>
                  </Tooltip>
                ) : (
                  <Typography color="text.secondary" variant="caption">
                    -
                  </Typography>
                )}
              </Stack>

              <Link
                color="text.primary"
                component={RouterLink}
                fontWeight={600}
                sx={{ width: "fit-content" }}
                to={`/tickets/${ticket.ticketId}`}
                underline="hover"
                variant="body2"
              >
                {ticket.name}
              </Link>

              {ticket.board !== null ? (
                <Link
                  color="text.secondary"
                  component={RouterLink}
                  sx={{ width: "fit-content" }}
                  to={`/boards/${ticket.board.boardId}`}
                  underline="hover"
                  variant="body2"
                >
                  {ticket.board.name}
                </Link>
              ) : (
                <Typography color="text.secondary" variant="body2">
                  Unknown board
                </Typography>
              )}

              <Person fallbackLabel="Unknown creator" person={ticket.createdBy} />
              <Person fallbackLabel="Unassigned" person={ticket.assignedTo} />
            </Box>
          );
        })}
      </Stack>
    </Paper>
  );
};
