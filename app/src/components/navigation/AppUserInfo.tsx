import type { MouseEvent, ReactElement } from "react";
import type { SxProps, Theme } from "@mui/material/styles";

import Box from "@mui/material/Box";
import ButtonBase from "@mui/material/ButtonBase";
import Stack from "@mui/material/Stack";
import Typography from "@mui/material/Typography";

import { AppAvatar } from "@components/avatar/AppAvatar";

interface AppUserInfoProps {
  ariaControls?: string | undefined;
  ariaExpanded?: "false" | "true" | undefined;
  avatarUrl?: string | undefined;
  firstName?: string | undefined;
  fullName: string;
  isAuthenticated: boolean;
  lastName?: string | undefined;
  onClick: (event: MouseEvent<HTMLElement>) => void;
  secondaryLabel?: string | undefined;
}

const triggerSx: SxProps<Theme> = {
  borderRadius: 2.5,
  px: 1.25,
  py: 0.75
};

const textBoxSx: SxProps<Theme> = {
  minWidth: 0,
  textAlign: "left"
};

export const AppUserInfo = ({
  ariaControls,
  ariaExpanded,
  avatarUrl,
  firstName,
  fullName,
  isAuthenticated,
  lastName,
  onClick,
  secondaryLabel
}: AppUserInfoProps): ReactElement => {
  return (
    <ButtonBase
      aria-controls={ariaControls}
      aria-expanded={ariaExpanded}
      aria-haspopup="menu"
      onClick={onClick}
      sx={triggerSx}
    >
      <Stack alignItems="center" direction="row" spacing={1.25}>
        <AppAvatar
          avatarUrl={isAuthenticated ? avatarUrl : undefined}
          fallbackText="?"
          firstName={firstName}
          label={fullName}
          lastName={lastName}
          size="small"
          tone={isAuthenticated ? "primary" : "muted"}
        />

        <Box sx={textBoxSx}>
          <Typography fontWeight={700} noWrap variant="body2">
            {fullName}
          </Typography>
          {secondaryLabel !== undefined ? (
            <Typography noWrap variant="caption">
              {secondaryLabel}
            </Typography>
          ) : null}
        </Box>

      </Stack>
    </ButtonBase>
  );
};
