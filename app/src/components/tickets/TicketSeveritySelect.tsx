import type { ReactElement } from "react";

import Box from "@mui/material/Box";
import FormControl from "@mui/material/FormControl";
import InputLabel from "@mui/material/InputLabel";
import MenuItem from "@mui/material/MenuItem";
import Select from "@mui/material/Select";
import Typography from "@mui/material/Typography";

import {
  buildSeveritySelectOptions,
  findMetadataOption
} from "@components/tickets/metadataSelectOptions";
import {
  TicketMetadataOptionMenuLayout,
  TicketMetadataOptionValueLayout
} from "@components/tickets/TicketMetadataOptionLayout";
import type { DictionarySeverity } from "../../domain/dictionaries/graphql";

interface TicketSeveritySelectProps {
  disabled?: boolean | undefined;
  onChange: (severityId: string | null) => void;
  severities: DictionarySeverity[];
  value: string | null;
}

export const TicketSeveritySelect = ({
  disabled = false,
  onChange,
  severities,
  value
}: TicketSeveritySelectProps): ReactElement => {
  const severityOptions = buildSeveritySelectOptions(severities);
  const fallbackSeverity = severities[0];
  const fallbackOption =
    fallbackSeverity === undefined
      ? null
      : findMetadataOption(severityOptions, fallbackSeverity.severityId) ?? null;
  const selectedOption = value === null ? fallbackOption : findMetadataOption(severityOptions, value);

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
        renderValue={selected => {
          const option =
            findMetadataOption(severityOptions, selected) ??
            selectedOption;

          if (option === null) {
            return <Typography variant="body2">No severity</Typography>;
          }

          return <TicketMetadataOptionValueLayout option={option} />;
        }}
        value={value ?? ""}
      >
        {severityOptions.map(option => {
          return (
            <MenuItem key={option.value} value={option.value}>
              <TicketMetadataOptionMenuLayout option={option} />
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
