import type { ChangeEvent, ReactElement } from "react";
import { useEffect, useState } from "react";

import Alert from "@mui/material/Alert";
import Card from "@mui/material/Card";
import CardContent from "@mui/material/CardContent";
import Stack from "@mui/material/Stack";
import TextField from "@mui/material/TextField";
import Typography from "@mui/material/Typography";

import { FormActionButtons } from "@components/forms/user/FormActionButtons";
import { createAsyncSubmitHandler } from "@helpers/createAsyncActionBuilder";
import { useCurrentUser } from "@hooks/useCurrentUser";

interface UserProfileFormProps {
  disabled?: boolean;
}

interface UserProfileFormState {
  firstName: string;
  lastName: string;
}

const getProfileState = (
  firstName: string | undefined,
  lastName: string | undefined
): UserProfileFormState => {
  return {
    firstName: firstName ?? "",
    lastName: lastName ?? ""
  };
};

export const UserProfileForm = ({
  disabled = false
}: UserProfileFormProps): ReactElement => {
  const { currentUser, updateProfile } = useCurrentUser();
  const persistedState = getProfileState(
    currentUser?.firstName,
    currentUser?.lastName
  );
  const [formState, setFormState] = useState(persistedState);
  const [isTouched, setIsTouched] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    setFormState(persistedState);
    setIsTouched(false);
  }, [persistedState.firstName, persistedState.lastName]);

  const trimmedFirstName = formState.firstName.trim();
  const trimmedLastName = formState.lastName.trim();
  const firstNameError = isTouched && trimmedFirstName.length === 0;
  const lastNameError = isTouched && trimmedLastName.length === 0;
  const isValid = trimmedFirstName.length > 0 && trimmedLastName.length > 0;
  const isDisabled = disabled || isSubmitting;
  const isChanged =
    trimmedFirstName !== persistedState.firstName.trim() ||
    trimmedLastName !== persistedState.lastName.trim();

  const handleChange =
    (field: keyof UserProfileFormState) =>
    (event: ChangeEvent<HTMLInputElement | HTMLTextAreaElement>): void => {
      setIsTouched(true);
      setFormState(currentState => ({
        ...currentState,
        [field]: event.target.value
      }));
    };

  const handleCancel = (): void => {
    setFormState(persistedState);
    setIsTouched(false);
    setErrorMessage(null);
  };

  const handleApply = createAsyncSubmitHandler()
    .when(() => {
      setIsTouched(true);

      return isValid && isChanged && !isDisabled;
    })
    .onStart(() => {
      setErrorMessage(null);
      setIsSubmitting(true);
    })
    .request(() => updateProfile(trimmedFirstName, trimmedLastName))
    .onSuccess(() => {
      setIsTouched(false);
    })
    .onError((error: unknown) => {
      setErrorMessage(
        error instanceof Error
          ? error.message
          : "Failed to update the profile details."
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
          <Typography variant="h5">Profile details</Typography>
          <Typography color="textSecondary" variant="body2">
            Update your profile details.
          </Typography>
        </Stack>

        <TextField
          disabled={isDisabled}
          error={firstNameError}
          fullWidth
          helperText={firstNameError ? "First name is required." : " "}
          label="First name"
          onChange={handleChange("firstName")}
          required
          value={formState.firstName}
        />

        <TextField
          disabled={isDisabled}
          error={lastNameError}
          fullWidth
          helperText={lastNameError ? "Last name is required." : " "}
          label="Last name"
          onChange={handleChange("lastName")}
          required
          value={formState.lastName}
        />

          {isChanged ? (
            <FormActionButtons
              applyDisabled={!isValid}
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
