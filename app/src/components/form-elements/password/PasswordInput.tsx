import type { ChangeEvent, FocusEvent, ReactElement } from "react";
import { useEffect, useState } from "react";

import VisibilityIcon from "@mui/icons-material/Visibility";
import VisibilityOffIcon from "@mui/icons-material/VisibilityOff";
import InputAdornment from "@mui/material/InputAdornment";
import IconButton from "@mui/material/IconButton";
import Stack from "@mui/material/Stack";
import TextField from "@mui/material/TextField";

import { passwordStrengthConfig } from "@configs/passwordStrengthConfig";
import { evaluatePasswordStrength } from "@helpers/passwordStrength";
import type { PasswordStrengthResult } from "@helpers/passwordStrength";
import { PasswordStrengthBar } from "@components/form-elements/password/PasswordStrengthBar";

type TextInputElement = HTMLInputElement | HTMLTextAreaElement;

export interface PasswordInputValidation {
  errorMessage: string | null;
  isEmpty: boolean;
  isStrong: boolean;
  isValid: boolean;
  strength: PasswordStrengthResult;
}

interface PasswordInputProps {
  autoComplete?: string;
  disabled?: boolean;
  error?: boolean;
  helperText?: ReactElement | string;
  label?: string;
  name?: string;
  onChange?: (event: ChangeEvent<TextInputElement>) => void;
  onValidationChange?: (validation: PasswordInputValidation) => void;
  onBlur?: (event: FocusEvent<TextInputElement>) => void;
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
  onValidationChange,
  onBlur,
  required = false,
  value
}: PasswordInputProps): ReactElement => {
  const [isVisible, setIsVisible] = useState(false);
  const [isTouched, setIsTouched] = useState(false);
  const strength = evaluatePasswordStrength(value, passwordStrengthConfig);
  const trimmedValue = value.trim();
  const isEmpty = trimmedValue.length === 0;
  const isStrong = strength.value === "strong";
  const shouldShowRequiredError = required && isTouched && isEmpty;
  const shouldShowWeakError = !isEmpty && !isStrong;
  const validationErrorMessage = shouldShowRequiredError
    ? "Password is required"
    : shouldShowWeakError
      ? strength.firstUnmetRequirement?.label ?? "Password is too weak"
      : null;
  const hasError = error || validationErrorMessage !== null;
  const resolvedHelperText = validationErrorMessage ?? helperText;

  useEffect(() => {
    onValidationChange?.({
      errorMessage: validationErrorMessage,
      isEmpty,
      isStrong,
      isValid: !required ? isStrong || isEmpty : !isEmpty && isStrong,
      strength
    });
  }, [
    isEmpty,
    isStrong,
    onValidationChange,
    required,
    strength.firstUnmetRequirement?.key,
    strength.firstUnmetRequirement?.label,
    strength.metRequirements,
    strength.score,
    strength.totalRequirements,
    strength.value,
    validationErrorMessage
  ]);

  return (
    <Stack spacing={1.5}>
      <TextField
        autoComplete={autoComplete}
        disabled={disabled}
        error={hasError}
        fullWidth
        helperText={resolvedHelperText}
        label={label}
        name={name}
        onBlur={event => {
          setIsTouched(true);
          onBlur?.(event);
        }}
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
