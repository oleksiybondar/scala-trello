import type { MouseEvent, ReactElement } from "react";
import type { SxProps, Theme } from "@mui/material/styles";


import Avatar from "@mui/material/Avatar";
import Box from "@mui/material/Box";
import ButtonBase from "@mui/material/ButtonBase";
import Stack from "@mui/material/Stack";
import Typography from "@mui/material/Typography";

interface AppUserInfoProps {
  ariaControls?: string | undefined;
  ariaExpanded?: "false" | "true" | undefined;
  avatarUrl?: string | undefined;
  fullName: string;
  isAuthenticated: boolean;
  onClick: (event: MouseEvent<HTMLElement>) => void;
  secondaryLabel?: string | undefined;
}

const triggerSx: SxProps<Theme> = {
  borderRadius: 2.5,
  px: 1.25,
  py: 0.75
};

const avatarSx = (isAuthenticated: boolean): SxProps<Theme> => ({
  bgcolor: isAuthenticated ? "primary.main" : "grey.500",
  color: "primary.contrastText",
  fontSize: 14,
  fontWeight: 800,
  height: 36,
  width: 36
});

const textBoxSx: SxProps<Theme> = {
  minWidth: 0,
  textAlign: "left"
};

const getInitials = (fullName: string): string => {
  return fullName
    .split(" ")
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0]?.toUpperCase() ?? "")
    .join("");
};

export const AppUserInfo = ({
  ariaControls,
  ariaExpanded,
  avatarUrl,
  fullName,
  isAuthenticated,
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
        <Avatar
          alt={fullName}
          src={isAuthenticated ? avatarUrl : undefined}
          sx={avatarSx(isAuthenticated)}
        >
          {isAuthenticated ? getInitials(fullName) : "?"}
        </Avatar>

        <Box sx={textBoxSx}>
          <Typography fontWeight={700} noWrap variant="body2">
            {fullName}
          </Typography>
          {secondaryLabel !== undefined ? (
            <Typography color="text.secondary" noWrap variant="caption">
              {secondaryLabel}
            </Typography>
          ) : null}
        </Box>

      </Stack>
    </ButtonBase>
  );
};
