import type { ChangeEvent, ReactElement } from "react";
import { useState } from "react";

import Button from "@mui/material/Button";
import Card from "@mui/material/Card";
import CardContent from "@mui/material/CardContent";
import Stack from "@mui/material/Stack";
import TextField from "@mui/material/TextField";
import Typography from "@mui/material/Typography";

import { PasswordInputWithConfirmation } from "@components/form-elements/password/PasswordInputWithConfirmation";
import type { PasswordInputWithConfirmationValidation } from "@components/form-elements/password/PasswordInputWithConfirmation";

interface ChangePasswordFormProps {
  disabled?: boolean;
  onSubmit?: (payload: {
    currentPassword: string;
    password: string;
  }) => Promise<void> | void;
}

interface ChangePasswordFormState {
  currentPassword: string;
  password: string;
  passwordConfirmation: string;
}

const initialFormState: ChangePasswordFormState = {
  currentPassword: "",
  password: "",
  passwordConfirmation: ""
};

export const ChangePasswordForm = ({
  disabled = false,
  onSubmit
}: ChangePasswordFormProps): ReactElement => {
  const [formState, setFormState] = useState(initialFormState);
  const [isTouched, setIsTouched] = useState(false);
  const [passwordValidation, setPasswordValidation] =
    useState<PasswordInputWithConfirmationValidation | null>(null);

  const trimmedCurrentPassword = formState.currentPassword.trim();
  const isSameAsCurrentPassword =
    trimmedCurrentPassword.length > 0 &&
    formState.password.length > 0 &&
    formState.currentPassword === formState.password;
  const currentPasswordError = isTouched && trimmedCurrentPassword.length === 0;
  const isPasswordValid = passwordValidation?.isValid ?? false;
  const nextPasswordErrorMessage = isSameAsCurrentPassword
    ? "New password must be different from the current password."
    : null;
  const hasChanged =
    formState.currentPassword.length > 0 ||
    formState.password.length > 0 ||
    formState.passwordConfirmation.length > 0;

  const handleChange =
    (field: keyof ChangePasswordFormState) =>
    (event: ChangeEvent<HTMLInputElement | HTMLTextAreaElement>): void => {
      setIsTouched(true);
      setFormState(currentState => ({
        ...currentState,
        [field]: event.target.value
      }));
    };

  const handleCancel = (): void => {
    setFormState(initialFormState);
    setIsTouched(false);
  };

  const handleApply = async (): Promise<void> => {
    setIsTouched(true);

    if (
      disabled ||
      trimmedCurrentPassword.length === 0 ||
      !isPasswordValid ||
      isSameAsCurrentPassword
    ) {
      return;
    }

    await onSubmit?.({
      currentPassword: formState.currentPassword,
      password: formState.password
    });
  };

  return (
    <Card variant="outlined">
      <CardContent>
        <Stack padding={3} spacing={3}>
          <Stack spacing={1}>
            <Typography variant="h5">Password</Typography>
            <Typography color="textSecondary" variant="body2">
              Confirm your current password before setting a new one.
            </Typography>
          </Stack>

          <TextField
            disabled={disabled}
            error={currentPasswordError}
            fullWidth
            helperText={currentPasswordError ? "Current password is required." : " "}
            label="Current password"
            onChange={handleChange("currentPassword")}
            required
            type="password"
            value={formState.currentPassword}
          />

          <PasswordInputWithConfirmation
            confirmationValue={formState.passwordConfirmation}
            disabled={disabled}
            onConfirmationChange={handleChange("passwordConfirmation")}
            onPasswordChange={handleChange("password")}
            onValidationChange={setPasswordValidation}
            passwordLabel="New password"
            passwordValue={formState.password}
            required
            {...(nextPasswordErrorMessage === null
              ? {}
              : {
                  helperText: nextPasswordErrorMessage
                })}
            {...(nextPasswordErrorMessage === null
              ? {}
              : {
                  passwordError: true
                })}
          />

          {hasChanged ? (
            <Stack
              direction={{ xs: "column-reverse", sm: "row" }}
              justifyContent="flex-end"
              spacing={1.5}
            >
              <Button disabled={disabled} onClick={handleCancel} variant="outlined">
                Cancel
              </Button>
              <Button
                disabled={
                  disabled ||
                  trimmedCurrentPassword.length === 0 ||
                  !isPasswordValid ||
                  isSameAsCurrentPassword
                }
                onClick={() => {
                  void handleApply();
                }}
                variant="contained"
              >
                Apply
              </Button>
            </Stack>
          ) : null}
        </Stack>
      </CardContent>
    </Card>
  );
};
