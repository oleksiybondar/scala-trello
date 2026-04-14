import type { ReactElement } from "react";

import ListItemIcon from "@mui/material/ListItemIcon";
import ListItemText from "@mui/material/ListItemText";
import Stack from "@mui/material/Stack";
import Typography from "@mui/material/Typography";
import { useTheme } from "@mui/material/styles";

import type { TicketMetadataSelectOption } from "@components/tickets/metadataSelectOptions";
import { resolveMetadataToneColor } from "@components/tickets/ticketMetadata";

interface TicketMetadataOptionMenuLayoutProps {
  option: TicketMetadataSelectOption<string | number>;
  showDescription?: boolean | undefined;
}

export const TicketMetadataOptionMenuLayout = ({
  option,
  showDescription = true
}: TicketMetadataOptionMenuLayoutProps): ReactElement => {
  const theme = useTheme();
  const Icon = option.icon;

  return (
    <>
      <ListItemIcon>
        <Icon
          fontSize="small"
          sx={{
            color: resolveMetadataToneColor(option.tone, theme.palette)
          }}
        />
      </ListItemIcon>
      <ListItemText
        primary={option.label}
        secondary={showDescription ? option.description : undefined}
        slotProps={{
          primary: {
            variant: "body2"
          },
          ...(showDescription
            ? {
                secondary: {
                  variant: "caption"
                }
              }
            : {})
        }}
      />
    </>
  );
};

interface TicketMetadataOptionValueLayoutProps {
  option: TicketMetadataSelectOption<string | number>;
}

export const TicketMetadataOptionValueLayout = ({
  option
}: TicketMetadataOptionValueLayoutProps): ReactElement => {
  const theme = useTheme();
  const Icon = option.icon;

  return (
    <Stack alignItems="center" direction="row" spacing={1}>
      <Icon
        fontSize="small"
        sx={{
          color: resolveMetadataToneColor(option.tone, theme.palette)
        }}
      />
      <Typography noWrap variant="body2">
        {option.label}
      </Typography>
    </Stack>
  );
};
