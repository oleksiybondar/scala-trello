import type { ChangeEvent, ReactElement } from "react";
import { useEffect, useState } from "react";

import Stack from "@mui/material/Stack";
import TextField from "@mui/material/TextField";

import { PasswordInput } from "@components/form-elements/password/PasswordInput";
import type { PasswordInputValidation } from "@components/form-elements/password/PasswordInput";

type TextInputElement = HTMLInputElement | HTMLTextAreaElement;

export interface PasswordInputWithConfirmationValidation {
  confirmationErrorMessage: string | null;
  doPasswordsMatch: boolean;
  isConfirmationEmpty: boolean;
  isPasswordValid: boolean;
  isValid: boolean;
}

interface PasswordInputWithConfirmationProps {
  confirmationLabel?: string;
  confirmationName?: string;
  confirmationValue: string;
  disabled?: boolean;
  helperText?: ReactElement | string;
  onConfirmationChange?: (event: ChangeEvent<TextInputElement>) => void;
  onValidationChange?: (
    validation: PasswordInputWithConfirmationValidation
  ) => void;
  onPasswordChange?: (event: ChangeEvent<TextInputElement>) => void;
  passwordError?: boolean;
  passwordLabel?: string;
  passwordName?: string;
  passwordValue: string;
  required?: boolean;
}

export const PasswordInputWithConfirmation = ({
  confirmationLabel = "Confirm password",
  confirmationName = "confirmPassword",
  confirmationValue,
  disabled = false,
  helperText,
  onConfirmationChange,
  onPasswordChange,
  onValidationChange,
  passwordError = false,
  passwordLabel = "Password",
  passwordName = "password",
  passwordValue,
  required = false
}: PasswordInputWithConfirmationProps): ReactElement => {
  const [isConfirmationTouched, setIsConfirmationTouched] = useState(false);
  const [passwordValidation, setPasswordValidation] =
    useState<PasswordInputValidation | null>(null);
  const trimmedConfirmationValue = confirmationValue.trim();
  const isConfirmationEmpty = trimmedConfirmationValue.length === 0;
  const doPasswordsMatch =
    !isConfirmationEmpty && confirmationValue === passwordValue;
  const confirmationErrorMessage =
    required && isConfirmationTouched && isConfirmationEmpty
      ? "Please confirm the password"
      : !isConfirmationEmpty && !doPasswordsMatch
        ? "Passwords do not match"
        : null;
  const isPasswordValid = passwordValidation?.isValid ?? false;

  useEffect(() => {
    onValidationChange?.({
      confirmationErrorMessage,
      doPasswordsMatch,
      isConfirmationEmpty,
      isPasswordValid,
      isValid:
        isPasswordValid &&
        (!required || !isConfirmationEmpty) &&
        (isConfirmationEmpty ? !required : doPasswordsMatch)
    });
  }, [
    confirmationErrorMessage,
    doPasswordsMatch,
    isConfirmationEmpty,
    isPasswordValid,
    onValidationChange,
    required
  ]);

  return (
    <Stack spacing={2}>
      <PasswordInput
        disabled={disabled}
        error={passwordError}
        label={passwordLabel}
        name={passwordName}
        onValidationChange={setPasswordValidation}
        required={required}
        value={passwordValue}
        {...(helperText === undefined
          ? {}
          : {
              helperText
            })}
        {...(onPasswordChange === undefined
          ? {}
          : {
              onChange: onPasswordChange
            })}
      />

      <TextField
        disabled={disabled}
        error={confirmationErrorMessage !== null}
        fullWidth
        helperText={confirmationErrorMessage ?? undefined}
        label={confirmationLabel}
        name={confirmationName}
        onBlur={() => {
          setIsConfirmationTouched(true);
        }}
        onChange={onConfirmationChange}
        required={required}
        type="password"
        value={confirmationValue}
      />
    </Stack>
  );
};
