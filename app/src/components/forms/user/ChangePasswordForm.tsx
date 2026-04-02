import type { ChangeEvent, ReactElement } from "react";
import { useState } from "react";

import Alert from "@mui/material/Alert";
import Button from "@mui/material/Button";
import Card from "@mui/material/Card";
import CardContent from "@mui/material/CardContent";
import Stack from "@mui/material/Stack";
import TextField from "@mui/material/TextField";
import Typography from "@mui/material/Typography";

import { PasswordInputWithConfirmation } from "@components/form-elements/password/PasswordInputWithConfirmation";
import type { PasswordInputWithConfirmationValidation } from "@components/form-elements/password/PasswordInputWithConfirmation";
import { useUserSettingsMutation } from "@features/user/useUserSettingsMutation";
import { createAsyncSubmitHandler } from "@helpers/createAsyncActionBuilder";
import { requestGraphQL } from "@helpers/requestGraphQL";
import { buildChangePasswordMutation } from "@models/user";
import type { ChangePasswordMutationResponse } from "@models/user";

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
  const { getGraphQLAuthContext, refreshUserState } = useUserSettingsMutation();
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

  const handleApply = createAsyncSubmitHandler<
    ChangePasswordMutationResponse,
    ChangePasswordMutationResponse
  >()
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
    .request(() =>
      requestGraphQL<ChangePasswordMutationResponse>({
        ...getGraphQLAuthContext(),
        document: buildChangePasswordMutation(
          formState.currentPassword,
          formState.password
        )
      })
    )
    .verify((response: ChangePasswordMutationResponse) => {
      if (!response.changePassword) {
        throw new Error("Password update was rejected.");
      }

      return response;
    })
    .onSuccess(async () => {
      handleCancel();
      await refreshUserState();
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
            <Stack
              direction={{ xs: "column-reverse", sm: "row" }}
              justifyContent="flex-end"
              spacing={1.5}
            >
              <Button disabled={isDisabled} onClick={handleCancel} variant="outlined">
                Cancel
              </Button>
              <Button
                disabled={
                  isDisabled ||
                  trimmedCurrentPassword.length === 0 ||
                  !isPasswordValid ||
                  isSameAsCurrentPassword
                }
                onClick={handleApply}
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
