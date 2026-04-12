import type { ChangeEvent, ReactElement } from "react";
import { useState } from "react";

import Alert from "@mui/material/Alert";
import Card from "@mui/material/Card";
import CardContent from "@mui/material/CardContent";
import Stack from "@mui/material/Stack";
import TextField from "@mui/material/TextField";
import Typography from "@mui/material/Typography";

import { FormActionButtons } from "@components/forms/user/FormActionButtons";
import { PasswordInputWithConfirmation } from "@components/form-elements/password/PasswordInputWithConfirmation";
import type { PasswordInputWithConfirmationValidation } from "@components/form-elements/password/PasswordInputWithConfirmation";
import { createAsyncSubmitHandler } from "@helpers/createAsyncActionBuilder";
import { useCurrentUser } from "@hooks/useCurrentUser";

interface ChangePasswordFormProps {
  disabled?: boolean;
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
  disabled = false
}: ChangePasswordFormProps): ReactElement => {
  const { changePassword } = useCurrentUser();
  const [formState, setFormState] = useState(initialFormState);
  const [isTouched, setIsTouched] = useState(false);
  const [passwordValidation, setPasswordValidation] =
    useState<PasswordInputWithConfirmationValidation | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

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
  const isDisabled = disabled || isSubmitting;
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
    setErrorMessage(null);
  };

  const handleApply = createAsyncSubmitHandler()
    .when(() => {
      setIsTouched(true);

      return (
        !isDisabled &&
        trimmedCurrentPassword.length > 0 &&
        isPasswordValid &&
        !isSameAsCurrentPassword
      );
    })
    .onStart(() => {
      setErrorMessage(null);
      setIsSubmitting(true);
    })
    .request(() => changePassword(formState.currentPassword, formState.password))
    .onSuccess(() => {
      handleCancel();
    })
    .onError((error: unknown) => {
      setErrorMessage(
        error instanceof Error ? error.message : "Failed to update the password."
      );
    })
    .onFinally(() => {
      setIsSubmitting(false);
    })
    .handle;

  return (
    <Card variant="outlined">
      <CardContent>
        <Stack padding={3} spacing={3}>
          {errorMessage !== null ? <Alert severity="error">{errorMessage}</Alert> : null}
          <Stack spacing={1}>
            <Typography variant="h5">Password</Typography>
            <Typography color="textSecondary" variant="body2">
              Confirm your current password before setting a new one.
            </Typography>
          </Stack>

          <TextField
            disabled={isDisabled}
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
            disabled={isDisabled}
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
            <FormActionButtons
              applyDisabled={
                trimmedCurrentPassword.length === 0 ||
                !isPasswordValid ||
                isSameAsCurrentPassword
              }
              isDisabled={isDisabled}
              onApply={handleApply}
              onCancel={handleCancel}
            />
          ) : null}
        </Stack>
      </CardContent>
    </Card>
  );
};
