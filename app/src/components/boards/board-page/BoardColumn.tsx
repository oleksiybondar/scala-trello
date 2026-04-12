import type { ReactElement } from "react";

import Box from "@mui/material/Box";
import Paper from "@mui/material/Paper";
import Stack from "@mui/material/Stack";
import Divider from "@mui/material/Divider";
import Typography from "@mui/material/Typography";

import type { Ticket } from "../../../domain/ticket/graphql";

interface BoardColumnProps {
  tickets: Ticket[];
  title: string;
}

export const BoardColumn = ({ tickets, title }: BoardColumnProps): ReactElement => {
  return (
    <Paper
      sx={{
        bgcolor: "background.default",
        display: "flex",
        flexDirection: "column",
        minHeight: {
          lg: "750px",
          xs: "320px"
        },
        overflow: "hidden"
      }}
      variant="outlined"
    >
      <Box
        sx={{
          backdropFilter: "blur(10px)",
          bgcolor: "background.paper",
          borderBottom: theme => "1px solid " + theme.palette.divider,
          position: "sticky",
          px: 2,
          py: 1.5,
          top: 0,
          zIndex: 1
        }}
      >
        <Typography variant="h6">{title}</Typography>
      </Box>

      <Stack
        spacing={1}
        sx={{
          color: "text.secondary",
          flex: 1,
          minHeight: 280,
          px: 2,
          py: 3
        }}
      >
        {tickets.length === 0 ? (
          <Typography variant="body2">No tickets yet.</Typography>
        ) : (
          tickets.map(ticket => (
            <Paper key={ticket.ticketId} sx={{ p: 1.5 }} variant="outlined">
              <Stack spacing={1}>
                <Typography color="text.primary" fontWeight={700} variant="body2">
                  {ticket.name}
                </Typography>
                {ticket.description !== null && ticket.description.trim().length > 0 ? (
                  <Typography variant="caption">{ticket.description}</Typography>
                ) : null}
                <Divider />
                <Stack direction="row" justifyContent="space-between" spacing={1}>
                  <Typography variant="caption">
                    {ticket.assignedTo === null
                      ? "Unassigned"
                      : ticket.assignedTo.firstName + " " + ticket.assignedTo.lastName}
                  </Typography>
                  <Typography variant="caption">
                    {String(ticket.trackedMinutes)}m
                  </Typography>
                </Stack>
              </Stack>
            </Paper>
          ))
        )}
      </Stack>
    </Paper>
  );
};
