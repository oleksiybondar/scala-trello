import type { ReactElement } from "react";

import Card from "@mui/material/Card";
import CardContent from "@mui/material/CardContent";
import Divider from "@mui/material/Divider";
import Stack from "@mui/material/Stack";
import TextField from "@mui/material/TextField";
import Typography from "@mui/material/Typography";

interface BoardSettingsPlaceholderPanelProps {
  description: string;
  helperText: string;
  title: string;
}

export const BoardSettingsPlaceholderPanel = ({
  description,
  helperText,
  title
}: BoardSettingsPlaceholderPanelProps): ReactElement => {
  return (
    <Card variant="outlined">
      <CardContent>
        <Stack spacing={3}>
          <Stack spacing={1}>
            <Typography color="primary" variant="overline">
              Board Settings
            </Typography>
            <Typography variant="h3">{title}</Typography>
            <Typography color="text.secondary" variant="body1">
              {description}
            </Typography>
          </Stack>

          <Divider />

          <Stack spacing={2}>
            <TextField
              disabled
              fullWidth
              helperText="Temporary placeholder until this section gets its own persisted settings."
              label={helperText}
            />
            <Typography color="text.secondary" variant="body2">
              This section is a layout scaffold for the upcoming authenticated
              board settings flow.
            </Typography>
          </Stack>
        </Stack>
      </CardContent>
    </Card>
  );
};
