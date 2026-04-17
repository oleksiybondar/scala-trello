import type { ReactElement } from "react";

import Chip from "@mui/material/Chip";
import Stack from "@mui/material/Stack";
import Typography from "@mui/material/Typography";
import { alpha } from "@mui/material/styles";

import { Person } from "@components/avatar/Person";
import { formatMinutesToTimeTrackingDuration } from "@helpers/timeTrackingConversions";
import type { TicketTimeTrackingEntry } from "../../../domain/ticket/graphql";

interface TicketActivityEntryItemProps {
  activityColor: string;
  activityLabel: string;
  entry: TicketTimeTrackingEntry;
}

export const TicketActivityEntryItem = ({
  activityColor,
  activityLabel,
  entry
}: TicketActivityEntryItemProps): ReactElement => {
  return (
    <Stack
      alignItems="center"
      direction="row"
      justifyContent="space-between"
      spacing={1.5}
      sx={{
        py: 0.75
      }}
    >
      <Stack alignItems="center" direction="row" spacing={1} sx={{ flex: 1, minWidth: 0 }}>
        <Person fallbackLabel="Unknown user" person={entry.user} />
        <Chip
          label={activityLabel}
          size="small"
          sx={{
            backgroundColor: alpha(activityColor, 0.14),
            border: `1px solid ${alpha(activityColor, 0.35)}`,
            color: activityColor,
            flex: 1,
            minWidth: 0,
            "& .MuiChip-label": {
              overflow: "hidden",
              textOverflow: "ellipsis",
              whiteSpace: "nowrap"
            }
          }}
          variant="outlined"
        />
      </Stack>
      <Typography sx={{ flexShrink: 0 }} variant="caption">
        {formatMinutesToTimeTrackingDuration(Math.max(0, entry.durationMinutes))}
      </Typography>
    </Stack>
  );
};
