import type { ReactElement } from "react";

import Avatar from "@mui/material/Avatar";

type AppAvatarSize = "small" | "medium" | "large";
type AppAvatarTone = "default" | "muted" | "primary";

interface AppAvatarProps {
  avatarUrl?: string | null | undefined;
  fallbackText?: string | undefined;
  firstName?: string | null | undefined;
  lastName?: string | null | undefined;
  label?: string | undefined;
  size?: AppAvatarSize | undefined;
  tone?: AppAvatarTone | undefined;
}

const avatarSizeMap: Record<AppAvatarSize, number> = {
  large: 96,
  medium: 48,
  small: 36
};

const fontSizeMap: Record<AppAvatarSize, number> = {
  large: 32,
  medium: 18,
  small: 14
};

const getInitials = (
  firstName: string | null | undefined,
  lastName: string | null | undefined,
  label: string | undefined
): string => {
  const explicitInitials = [firstName, lastName]
    .filter((part): part is string => part !== null && part !== undefined && part.trim().length > 0)
    .slice(0, 2)
    .map(part => part[0]?.toUpperCase() ?? "")
    .join("");

  if (explicitInitials.length > 0) {
    return explicitInitials;
  }

  if (label === undefined || label.trim().length === 0) {
    return "";
  }

  return label
    .split(" ")
    .filter(Boolean)
    .slice(0, 2)
    .map(part => part[0]?.toUpperCase() ?? "")
    .join("");
};

const getBackgroundColor = (tone: AppAvatarTone): string => {
  switch (tone) {
    case "muted":
      return "grey.500";
    case "primary":
      return "primary.main";
    default:
      return "action.selected";
  }
};

/**
 * Shared user avatar with image, initials, and simple size variants.
 */
export const AppAvatar = ({
  avatarUrl,
  fallbackText = "?",
  firstName,
  label,
  lastName,
  size = "medium",
  tone = "default"
}: AppAvatarProps): ReactElement => {
  const initials = getInitials(firstName, lastName, label);
  const resolvedSize = avatarSizeMap[size];

  return (
    <Avatar
      alt={label}
      src={avatarUrl ?? undefined}
      sx={{
        bgcolor: avatarUrl === null || avatarUrl === undefined || avatarUrl.length === 0
          ? getBackgroundColor(tone)
          : undefined,
        color: "primary.contrastText",
        fontSize: fontSizeMap[size],
        fontWeight: 800,
        height: resolvedSize,
        width: resolvedSize
      }}
    >
      {initials.length > 0 ? initials : fallbackText}
    </Avatar>
  );
};
