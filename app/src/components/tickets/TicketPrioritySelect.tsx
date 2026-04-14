import type { ReactElement } from "react";

import Box from "@mui/material/Box";
import FormControl from "@mui/material/FormControl";
import InputLabel from "@mui/material/InputLabel";
import MenuItem from "@mui/material/MenuItem";
import Select from "@mui/material/Select";
import Typography from "@mui/material/Typography";

import {
  buildPrioritySelectOptions,
  findMetadataOption
} from "@components/tickets/metadataSelectOptions";
import {
  TicketMetadataOptionMenuLayout,
  TicketMetadataOptionValueLayout
} from "@components/tickets/TicketMetadataOptionLayout";
import { DEFAULT_PRIORITY_OPTION } from "@components/tickets/ticketMetadata";

interface TicketPrioritySelectProps {
  disabled?: boolean | undefined;
  onChange: (priority: number) => void;
  value: number;
}

export const TicketPrioritySelect = ({
  disabled = false,
  onChange,
  value
}: TicketPrioritySelectProps): ReactElement => {
  const priorityOptions = buildPrioritySelectOptions();
  const fallbackOption = {
    description: DEFAULT_PRIORITY_OPTION.description,
    icon: DEFAULT_PRIORITY_OPTION.icon,
    label: DEFAULT_PRIORITY_OPTION.label,
    tone: DEFAULT_PRIORITY_OPTION.tone,
    value: DEFAULT_PRIORITY_OPTION.key
  };
  const selectedOption = findMetadataOption(priorityOptions, value) ?? fallbackOption;

  return (
    <FormControl fullWidth size="small">
      <InputLabel id="create-ticket-priority-label">Priority</InputLabel>
      <Select
        disabled={disabled}
        label="Priority"
        labelId="create-ticket-priority-label"
        onChange={event => {
          onChange(Number(event.target.value));
        }}
        renderValue={() => {
          return <TicketMetadataOptionValueLayout option={selectedOption} />;
        }}
        value={String(value)}
      >
        {priorityOptions.map(option => {
          return (
            <MenuItem key={option.value} value={String(option.value)}>
              <TicketMetadataOptionMenuLayout option={option} />
            </MenuItem>
          );
        })}
      </Select>
      <Box sx={{ minHeight: 20, px: 1.75, pt: 0.75 }}>
        <Typography color="text.secondary" variant="caption">
          0-2 hot, 3-7 planned, 8-9 low urgency.
        </Typography>
      </Box>
    </FormControl>
  );
};
