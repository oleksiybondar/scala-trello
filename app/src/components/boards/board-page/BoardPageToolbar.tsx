import type { ReactElement } from "react";
import { useState } from "react";

import AddOutlinedIcon from "@mui/icons-material/AddOutlined";
import Button from "@mui/material/Button";
import Chip from "@mui/material/Chip";
import Paper from "@mui/material/Paper";
import Stack from "@mui/material/Stack";
import TextField from "@mui/material/TextField";

import { BoardTicketsAssigneeFilter } from "@components/boards/board-page/BoardTicketsAssigneeFilter";
import { CreateTicketDialog } from "@components/tickets/CreateTicketDialog";
import { useBoard } from "@hooks/useBoard";
import { useTickets } from "@hooks/useTickets";

export const BoardPageToolbar = (): ReactElement => {
  const [isCreateTicketDialogOpen, setIsCreateTicketDialogOpen] = useState(false);
  const { members } = useBoard();
  const {
    assignedToUserIds,
    codeReviewTickets,
    doneTickets,
    inProgressTickets,
    inTestingTickets,
    newTickets,
    searchKeywords,
    setAssignedToUserIds,
    setSearchKeywords,
    ticketsCount
  } = useTickets();
  const filteredTicketsCount =
    newTickets.length +
    inProgressTickets.length +
    codeReviewTickets.length +
    inTestingTickets.length +
    doneTickets.length;

  return (
    <>
      <Paper
        sx={{
          minHeight: 56,
          px: 2,
          py: 1.5
        }}
        variant="outlined"
      >
        <Stack
          alignItems={{ md: "center", xs: "stretch" }}
          direction={{ md: "row", xs: "column" }}
          justifyContent="space-between"
          spacing={1.5}
        >
          <Stack
            alignItems={{ md: "center", xs: "stretch" }}
            direction={{ md: "row", xs: "column" }}
            spacing={1.5}
            sx={{ flex: 1 }}
          >
            <Stack alignItems="center" direction="row" spacing={1}>
              <Chip
                color="primary"
                label={`${String(filteredTicketsCount)}/${String(ticketsCount)} tickets`}
                size="small"
                variant="outlined"
              />
            </Stack>

            <Stack direction={{ md: "row", xs: "column" }} spacing={1.5} sx={{ flex: 1 }}>
              <TextField
                label="Search tickets"
                onChange={event => {
                  setSearchKeywords(event.target.value);
                }}
                size="small"
                sx={{ flex: 1, minWidth: { md: 220, xs: "100%" } }}
                value={searchKeywords}
              />

              <BoardTicketsAssigneeFilter
                members={members}
                selectedUserIds={assignedToUserIds}
                setSelectedUserIds={setAssignedToUserIds}
              />
            </Stack>
          </Stack>

          <Button
            onClick={() => {
              setIsCreateTicketDialogOpen(true);
            }}
            startIcon={<AddOutlinedIcon />}
            variant="contained"
          >
            New ticket
          </Button>
        </Stack>
      </Paper>

      <CreateTicketDialog
        onClose={() => {
          setIsCreateTicketDialogOpen(false);
        }}
        open={isCreateTicketDialogOpen}
      />
    </>
  );
};
