import type { ChangeEvent, ReactElement } from "react";
import { useState } from "react";

import VisibilityIcon from "@mui/icons-material/Visibility";
import VisibilityOffIcon from "@mui/icons-material/VisibilityOff";
import InputAdornment from "@mui/material/InputAdornment";
import IconButton from "@mui/material/IconButton";
import Stack from "@mui/material/Stack";
import TextField from "@mui/material/TextField";

import { passwordStrengthConfig } from "@configs/passwordStrengthConfig";
import { evaluatePasswordStrength } from "@helpers/passwordStrength";
import { PasswordStrengthBar } from "@components/forms/form-elements/PasswordStrengthBar";

interface PasswordInputProps {
  autoComplete?: string;
  disabled?: boolean;
  error?: boolean;
  helperText?: ReactElement | string;
  label?: string;
  name?: string;
  onChange?: (event: ChangeEvent<HTMLInputElement>) => void;
  required?: boolean;
  value: string;
}

export const PasswordInput = ({
  autoComplete = "new-password",
  disabled = false,
  error = false,
  helperText,
  label = "Password",
  name = "password",
  onChange,
  required = false,
  value
}: PasswordInputProps): ReactElement => {
  const [isVisible, setIsVisible] = useState(false);
  const strength = evaluatePasswordStrength(value, passwordStrengthConfig);

  return (
    <Stack spacing={1.5}>
      <TextField
        autoComplete={autoComplete}
        disabled={disabled}
        error={error}
        fullWidth
        helperText={helperText}
        label={label}
        name={name}
        onChange={onChange}
        required={required}
        slotProps={{
          input: {
            endAdornment: (
              <InputAdornment position="end">
                <IconButton
                  aria-label={isVisible ? "Hide password" : "Show password"}
                  edge="end"
                  onClick={() => {
                    setIsVisible(currentValue => !currentValue);
                  }}
                >
                  {isVisible ? <VisibilityOffIcon /> : <VisibilityIcon />}
                </IconButton>
              </InputAdornment>
            )
          }
        }}
        type={isVisible ? "text" : "password"}
        value={value}
      />

      <PasswordStrengthBar strength={strength} />
    </Stack>
  );
};
