import type { ReactElement } from "react";
import { useEffect } from "react";

import Chip from "@mui/material/Chip";
import Paper from "@mui/material/Paper";
import Stack from "@mui/material/Stack";
import TextField from "@mui/material/TextField";

import { TimeTrackingActivitySelect } from "@components/time-tracking/TimeTrackingActivitySelect";
import { useActivities } from "@hooks/useActivities";
import { useAuth } from "@hooks/useAuth";
import { useTimeTrackingEntries } from "@hooks/useTimeTrackingEntries";

export const MyTimeTrackingToolbar = (): ReactElement => {
  const {
    currentParams,
    queryEntries,
    timeTrackingEntries,
    totalTimeTrackingEntries
  } = useTimeTrackingEntries();
  const { isAuthenticated } = useAuth();
  const {
    activities,
    hasLoadedActivities,
    isLoadingActivities,
    loadActivities
  } = useActivities();

  useEffect(() => {
    if (!isAuthenticated || hasLoadedActivities || isLoadingActivities) {
      return;
    }

    void loadActivities();
  }, [hasLoadedActivities, isAuthenticated, isLoadingActivities, loadActivities]);

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
              label={`${String(timeTrackingEntries.length)}/${String(totalTimeTrackingEntries)} entries`}
              size="small"
              variant="outlined"
            />
          </Stack>

          <Stack direction={{ md: "row", xs: "column" }} spacing={1.5} sx={{ flex: 1 }}>
            <TextField
              label="Search entries"
              onChange={event => {
                queryEntries({
                  keyword: event.target.value,
                  page: 1
                });
              }}
              size="small"
              sx={{ flex: 1, minWidth: { md: 220, xs: "100%" } }}
              value={currentParams.keyword ?? ""}
            />

            <TimeTrackingActivitySelect
              activities={activities}
              emptyLabel="Any"
              label="Activity"
              labelId="time-tracking-activity-filter-label"
              multiple
              onChange={nextValue => {
                const activityIds =
                  typeof nextValue === "string" ? (nextValue.length === 0 ? [] : [nextValue]) : nextValue;

                queryEntries({
                  activityIds,
                  page: 1
                });
              }}
              value={currentParams.activityIds}
            />
          </Stack>
        </Stack>
      </Stack>
    </Paper>
  );
};
