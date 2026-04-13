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
  getSeverityMeta,
  resolveMetadataToneColor
} from "@components/tickets/ticketMetadata";
import type { DictionarySeverity } from "../../domain/dictionaries/graphql";

interface TicketSeveritySelectProps {
  disabled?: boolean | undefined;
  onChange: (severityId: string | null) => void;
  severities: DictionarySeverity[];
  value: string | null;
}

const toSeverityLabel = (severity: DictionarySeverity): string => {
  return severity.name.charAt(0).toUpperCase() + severity.name.slice(1);
};

const renderSeverityValue = (
  severityId: string | null,
  severities: DictionarySeverity[],
  color: string
): ReactElement => {
  const selectedSeverity =
    severities.find(severity => severity.severityId === severityId) ?? severities[0];

  if (selectedSeverity === undefined) {
    return <Typography variant="body2">No severity</Typography>;
  }

  const severityMeta = getSeverityMeta(selectedSeverity.name);

  if (severityMeta === null) {
    return <Typography variant="body2">{toSeverityLabel(selectedSeverity)}</Typography>;
  }

  const Icon = severityMeta.icon;

  return (
    <Stack alignItems="center" direction="row" spacing={1}>
      <Icon fontSize="small" sx={{ color }} />
      <Typography noWrap variant="body2">
        {toSeverityLabel(selectedSeverity)}
      </Typography>
    </Stack>
  );
};

export const TicketSeveritySelect = ({
  disabled = false,
  onChange,
  severities,
  value
}: TicketSeveritySelectProps): ReactElement => {
  const theme = useTheme();

  return (
    <FormControl fullWidth size="small">
      <InputLabel id="create-ticket-severity-label">Severity</InputLabel>
      <Select
        disabled={disabled}
        label="Severity"
        labelId="create-ticket-severity-label"
        onChange={event => {
          onChange(event.target.value.length === 0 ? null : event.target.value);
        }}
        renderValue={() => {
          const selectedSeverity =
            severities.find(severity => severity.severityId === value) ?? severities[0];

          return renderSeverityValue(
            value,
            severities,
            selectedSeverity === undefined
              ? theme.palette.text.secondary
              : resolveMetadataToneColor(
                  getSeverityMeta(selectedSeverity.name)?.tone ?? "info",
                  theme.palette
                )
          );
        }}
        value={value ?? ""}
      >
        {severities.map(severity => {
          const severityMeta = getSeverityMeta(severity.name);

          if (severityMeta === null) {
            return null;
          }

          const Icon = severityMeta.icon;

          return (
            <MenuItem key={severity.severityId} value={severity.severityId}>
              <ListItemIcon>
                <Icon
                  fontSize="small"
                  sx={{
                    color: resolveMetadataToneColor(severityMeta.tone, theme.palette)
                  }}
                />
              </ListItemIcon>
              <ListItemText
                primary={toSeverityLabel(severity)}
                secondary={severity.description ?? "No description"}
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
          Severity reflects impact, priority reflects ordering.
        </Typography>
      </Box>
    </FormControl>
  );
};
