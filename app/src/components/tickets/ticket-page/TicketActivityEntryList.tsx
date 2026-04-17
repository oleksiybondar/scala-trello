import type { ReactElement } from "react";

import Divider from "@mui/material/Divider";
import Stack from "@mui/material/Stack";
import Typography from "@mui/material/Typography";

import { TicketActivityEntryItem } from "@components/tickets/ticket-page/TicketActivityEntryItem";
import { useActivities } from "@hooks/useActivities";
import { resolveTimeTrackingActivityName } from "@helpers/timeTrackingActivities";
import type { TicketTimeTrackingEntry } from "../../../domain/ticket/graphql";

interface TicketActivityEntryListProps {
  activityColorByName: Record<string, string>;
  entries: TicketTimeTrackingEntry[];
}

export const TicketActivityEntryList = ({
  activityColorByName,
  entries
}: TicketActivityEntryListProps): ReactElement => {
  const { activities } = useActivities();
  const activityNameById = Object.fromEntries(
    activities.map(activity => [activity.activityId, activity.name])
  );
  const sortedEntries = [...entries].sort((left, right) => {
    return new Date(right.loggedAt).getTime() - new Date(left.loggedAt).getTime();
  });

  if (sortedEntries.length === 0) {
    return (
      <Typography color="text.secondary" variant="body2">
        No time entries yet.
      </Typography>
    );
  }

  return (
    <Stack divider={<Divider flexItem />}>
      {sortedEntries.map(entry => (
        <TicketActivityEntryItem
          activityColor={
            activityColorByName[
              resolveTimeTrackingActivityName(
                entry.activityName,
                entry.activityCode,
                entry.activityId,
                activityNameById
              )
            ] ?? "#9e9e9e"
          }
          activityLabel={resolveTimeTrackingActivityName(
            entry.activityName,
            entry.activityCode,
            entry.activityId,
            activityNameById
          )}
          entry={entry}
          key={entry.entryId}
        />
      ))}
    </Stack>
  );
};
