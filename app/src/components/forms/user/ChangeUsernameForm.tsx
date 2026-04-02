import type { ChangeEvent, ReactElement } from "react";
import { useEffect, useState } from "react";

import Alert from "@mui/material/Alert";
import Button from "@mui/material/Button";
import Card from "@mui/material/Card";
import CardContent from "@mui/material/CardContent";
import Stack from "@mui/material/Stack";
import TextField from "@mui/material/TextField";
import Typography from "@mui/material/Typography";

import { useUserSettingsMutation } from "@features/user/useUserSettingsMutation";
import { createAsyncSubmitHandler } from "@helpers/createAsyncActionBuilder";
import { requestGraphQL } from "@helpers/requestGraphQL";
import { useCurrentUser } from "@hooks/useCurrentUser";
import { buildChangeUsernameMutation } from "@models/user";
import type { GraphQLCurrentUserResponse, UserMutationResponse } from "@models/user";

interface ChangeUsernameFormProps {
  disabled?: boolean;
}

export const ChangeUsernameForm = ({
  disabled = false
}: ChangeUsernameFormProps): ReactElement => {
  const { currentUser } = useCurrentUser();
  const { applyUpdatedUser, getGraphQLAuthContext } = useUserSettingsMutation();
  const persistedUsername = currentUser?.username ?? "";
  const [username, setUsername] = useState(persistedUsername);
  const [isTouched, setIsTouched] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    setUsername(persistedUsername);
    setIsTouched(false);
  }, [persistedUsername]);

  const trimmedUsername = username.trim();
  const isEmpty = trimmedUsername.length === 0;
  const hasChanged = trimmedUsername !== persistedUsername.trim();
  const hasError = isTouched && isEmpty;
  const isDisabled = disabled || isSubmitting;

  const handleChange = (
    event: ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
  ): void => {
    setIsTouched(true);
    setUsername(event.target.value);
  };

  const handleCancel = (): void => {
    setUsername(persistedUsername);
    setIsTouched(false);
    setErrorMessage(null);
  };

  const handleApply = createAsyncSubmitHandler<
    UserMutationResponse,
    GraphQLCurrentUserResponse
  >()
    .when(() => {
      setIsTouched(true);

      return !isDisabled && !isEmpty && hasChanged;
    })
    .onStart(() => {
      setErrorMessage(null);
      setIsSubmitting(true);
    })
    .request(() =>
      requestGraphQL<UserMutationResponse>({
        ...getGraphQLAuthContext(),
        document: buildChangeUsernameMutation(trimmedUsername)
      })
    )
    .verify((response: UserMutationResponse) => {
      if (response.changeUsername === undefined) {
        throw new Error("GraphQL response did not include the updated user.");
      }

      return response.changeUsername;
    })
    .onSuccess(applyUpdatedUser)
    .onError((error: unknown) => {
      setErrorMessage(
        error instanceof Error ? error.message : "Failed to update the username."
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
            <Typography variant="h5">Username</Typography>
            <Typography color="textSecondary" variant="body2">
              Update the username used to identify your account.
            </Typography>
          </Stack>

          <TextField
            disabled={isDisabled}
            error={hasError}
            fullWidth
            helperText={hasError ? "Username is required." : " "}
            label="Username"
            onChange={handleChange}
            required
            value={username}
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
                disabled={isDisabled || isEmpty}
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
