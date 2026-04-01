import type { ChangeEvent, FocusEvent, ReactElement } from "react";
import { useEffect, useState } from "react";

import TextField from "@mui/material/TextField";

import { isEmailAddress } from "@helpers/email";

export interface EmailInputValidation {
  errorMessage: string | null;
  isEmpty: boolean;
  isValid: boolean;
}

type TextInputElement = HTMLInputElement | HTMLTextAreaElement;

interface EmailInputProps {
  autoComplete?: string;
  disabled?: boolean;
  error?: boolean;
  helperText?: ReactElement | string;
  label?: string;
  name?: string;
  onBlur?: (event: FocusEvent<TextInputElement>) => void;
  onChange?: (event: ChangeEvent<TextInputElement>) => void;
  onValidationChange?: (validation: EmailInputValidation) => void;
  required?: boolean;
  value: string;
}

export const EmailInput = ({
  autoComplete = "email",
  disabled = false,
  error = false,
  helperText,
  label = "Email",
  name = "email",
  onBlur,
  onChange,
  onValidationChange,
  required = false,
  value
}: EmailInputProps): ReactElement => {
  const [isTouched, setIsTouched] = useState(false);
  const trimmedValue = value.trim();
  const isEmpty = trimmedValue.length === 0;
  const hasFormatError = !isEmpty && !isEmailAddress(value);
  const validationErrorMessage =
    required && isTouched && isEmpty
      ? "Email is required"
      : hasFormatError
        ? "Enter a valid email address"
        : null;
  const hasError = error || validationErrorMessage !== null;
  const resolvedHelperText = validationErrorMessage ?? helperText;

  useEffect(() => {
    onValidationChange?.({
      errorMessage: validationErrorMessage,
      isEmpty,
      isValid: !required ? isEmpty || !hasFormatError : !isEmpty && !hasFormatError
    });
  }, [hasFormatError, isEmpty, onValidationChange, required, validationErrorMessage]);

  return (
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
      type="email"
      value={value}
    />
  );
};
