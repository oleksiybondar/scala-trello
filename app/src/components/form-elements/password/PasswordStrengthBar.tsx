import type { ReactElement } from "react";

import LinearProgress from "@mui/material/LinearProgress";
import Stack from "@mui/material/Stack";
import Typography from "@mui/material/Typography";

import type { PasswordStrengthResult } from "@helpers/passwordStrength";

interface PasswordStrengthBarProps {
  strength: PasswordStrengthResult;
}

type StrengthColor = "error" | "info" | "primary" | "success" | "warning";

const strengthLabels: Record<PasswordStrengthResult["value"], string> = {
  empty: "Enter a password",
  fair: "Fair",
  good: "Good",
  strong: "Strong",
  weak: "Weak"
};

const strengthColors: Record<PasswordStrengthResult["value"], StrengthColor> = {
  empty: "primary",
  fair: "warning",
  good: "info",
  strong: "success",
  weak: "error"
};

export const PasswordStrengthBar = ({
  strength
}: PasswordStrengthBarProps): ReactElement => {
  const color = strengthColors[strength.value];

  return (
    <Stack alignItems="center" direction="row" spacing={1.5}>
      <LinearProgress
        aria-label="Password strength"
        color={color}
        sx={{
          flexGrow: 1,
          minWidth: 120
        }}
        value={strength.score}
        variant="determinate"
      />
      <Typography color={color} variant="body2">
        {strengthLabels[strength.value]}
      </Typography>
    </Stack>
  );
};
