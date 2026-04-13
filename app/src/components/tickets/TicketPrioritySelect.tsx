import type { ReactElement } from "react";

import Box from "@mui/material/Box";
import FormControl from "@mui/material/FormControl";
import InputLabel from "@mui/material/InputLabel";
import ListItemIcon from "@mui/material/ListItemIcon";
import ListItemText from "@mui/material/ListItemText";
import MenuItem from "@mui/material/MenuItem";
import Select from "@mui/material/Select";
import Stack from "@mui/material/Stack";
import Typography from "@mui/material/Typography";
import { useTheme } from "@mui/material/styles";
import {
  DEFAULT_PRIORITY_OPTION,
  PRIORITY_OPTIONS,
  resolveMetadataToneColor
} from "@components/tickets/ticketMetadata";

interface TicketPrioritySelectProps {
  disabled?: boolean | undefined;
  onChange: (priority: number) => void;
  value: number;
}

const renderPriorityValue = (
  priority: number,
  color: string
): ReactElement => {
  const selectedOption =
    PRIORITY_OPTIONS.find(option => option.key === priority) ?? DEFAULT_PRIORITY_OPTION;
  const Icon = selectedOption.icon;

  return (
    <Stack alignItems="center" direction="row" spacing={1}>
      <Icon fontSize="small" sx={{ color }} />
      <Typography noWrap variant="body2">
        {selectedOption.label}
      </Typography>
    </Stack>
  );
};

export const TicketPrioritySelect = ({
  disabled = false,
  onChange,
  value
}: TicketPrioritySelectProps): ReactElement => {
  const theme = useTheme();

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
        renderValue={selected => {
          const option =
            PRIORITY_OPTIONS.find(priorityOption => priorityOption.key === Number(selected)) ??
            DEFAULT_PRIORITY_OPTION;

          return renderPriorityValue(
            Number(selected),
            resolveMetadataToneColor(option.tone, theme.palette)
          );
        }}
        value={String(value)}
      >
        {PRIORITY_OPTIONS.map(option => {
          const Icon = option.icon;

          return (
            <MenuItem key={option.key} value={String(option.key)}>
              <ListItemIcon>
                <Icon
                  fontSize="small"
                  sx={{ color: resolveMetadataToneColor(option.tone, theme.palette) }}
                />
              </ListItemIcon>
              <ListItemText
                primary={option.label}
                secondary={option.description}
                slotProps={{
                  primary: {
                    variant: "body2"
                  },
                  secondary: {
                    variant: "caption"
                  }
                }}
              />
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
