import type { ChangeEvent, ReactElement } from "react";
import { useEffect, useState } from "react";

import Button from "@mui/material/Button";
import Stack from "@mui/material/Stack";
import TextField from "@mui/material/TextField";
import Typography from "@mui/material/Typography";

import { useCurrentUser } from "@hooks/useCurrentUser";
import Card from "@mui/material/Card";
import CardContent from "@mui/material/CardContent";

interface UserProfileFormProps {
  disabled?: boolean;
  onSubmit?: (payload: {
    firstName: string;
    lastName: string;
  }) => Promise<void> | void;
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
  disabled = false,
  onSubmit
}: UserProfileFormProps): ReactElement => {
  const { currentUser } = useCurrentUser();
  const persistedState = getProfileState(
    currentUser?.firstName,
    currentUser?.lastName
  );
  const [formState, setFormState] = useState(persistedState);
  const [isTouched, setIsTouched] = useState(false);

  useEffect(() => {
    setFormState(persistedState);
    setIsTouched(false);
  }, [persistedState.firstName, persistedState.lastName]);

  const trimmedFirstName = formState.firstName.trim();
  const trimmedLastName = formState.lastName.trim();
  const firstNameError = isTouched && trimmedFirstName.length === 0;
  const lastNameError = isTouched && trimmedLastName.length === 0;
  const isValid = trimmedFirstName.length > 0 && trimmedLastName.length > 0;
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
  };

  const handleApply = async (): Promise<void> => {
    setIsTouched(true);

    if (!isValid || !isChanged || disabled) {
      return;
    }

    await onSubmit?.({
      firstName: trimmedFirstName,
      lastName: trimmedLastName
    });
  };

  return (
      <Card variant="outlined">
        <CardContent>
      <Stack padding={3} spacing={3}>
        <Stack spacing={1}>
          <Typography variant="h5">Profile details</Typography>
          <Typography color="textSecondary" variant="body2">
            Update your profile details.
          </Typography>
        </Stack>

        <TextField
          disabled={disabled}
          error={firstNameError}
          fullWidth
          helperText={firstNameError ? "First name is required." : " "}
          label="First name"
          onChange={handleChange("firstName")}
          required
          value={formState.firstName}
        />

        <TextField
          disabled={disabled}
          error={lastNameError}
          fullWidth
          helperText={lastNameError ? "Last name is required." : " "}
          label="Last name"
          onChange={handleChange("lastName")}
          required
          value={formState.lastName}
        />

        { isChanged && (<Stack direction={{ xs: "column-reverse", sm: "row" }} spacing={1.5} justifyContent="flex-end">
          <Button
            disabled={disabled}
            onClick={handleCancel}
            variant="outlined"
          >
            Cancel
          </Button>
          <Button
            disabled={disabled || !isValid}
            onClick={() => {
              void handleApply();
            }}
            variant="contained"
          >
            Apply
          </Button>
        </Stack> ) }
      </Stack>
        </CardContent>
      </Card>
  );
};
