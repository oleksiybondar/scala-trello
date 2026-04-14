import type { ReactElement } from "react";
import { useEffect } from "react";

import Chip from "@mui/material/Chip";
import Paper from "@mui/material/Paper";
import Stack from "@mui/material/Stack";
import TextField from "@mui/material/TextField";

import { BoardTicketsPriorityFilter } from "@components/boards/board-page/BoardTicketsPriorityFilter";
import { BoardTicketsSeverityFilter } from "@components/boards/board-page/BoardTicketsSeverityFilter";
import { ToolbarSwitchControl } from "@components/toolbar/ToolbarSwitchControl";
import { useAuth } from "@hooks/useAuth";
import { useMyTickets } from "@hooks/useMyTickets";
import { useSeverities } from "@hooks/useSeverities";

export const MyTicketsToolbar = (): ReactElement => {
  const { isAuthenticated } = useAuth();
  const {
    currentParams,
    myTickets,
    queryMyTickets,
    totalMyTickets
  } = useMyTickets();
  const {
    hasLoadedSeverities,
    isLoadingSeverities,
    loadSeverities,
    severities
  } = useSeverities();

  useEffect(() => {
    if (!isAuthenticated || hasLoadedSeverities || isLoadingSeverities) {
      return;
    }

    void loadSeverities();
  }, [hasLoadedSeverities, isAuthenticated, isLoadingSeverities, loadSeverities]);

  return (
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
              label={`${String(myTickets.length)}/${String(totalMyTickets)} tickets`}
              size="small"
              variant="outlined"
            />
          </Stack>

          <Stack direction={{ md: "row", xs: "column" }} spacing={1.5} sx={{ flex: 1 }}>
            <TextField
              label="Search tickets"
              onChange={event => {
                queryMyTickets({
                  keyword: event.target.value,
                  page: 1
                });
              }}
              size="small"
              sx={{ flex: 1, minWidth: { md: 220, xs: "100%" } }}
              value={currentParams.keyword ?? ""}
            />
            <ToolbarSwitchControl
              checked={currentParams.assignedOnly}
              label="Assigned on me"
              onChange={checked => {
                queryMyTickets({
                  assignedOnly: checked,
                  page: 1
                });
              }}
              size="small"
            />

            <BoardTicketsSeverityFilter
              selectedSeverityIds={currentParams.severityIds}
              setSelectedSeverityIds={severityIds => {
                queryMyTickets({
                  page: 1,
                  severityIds
                });
              }}
              severities={severities}
            />
            <BoardTicketsPriorityFilter
              selectedPriorities={currentParams.priorities}
              setSelectedPriorities={priorities => {
                queryMyTickets({
                  page: 1,
                  priorities
                });
              }}
            />
          </Stack>
        </Stack>
      </Stack>
    </Paper>
  );
};
