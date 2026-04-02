import type { ChangeEvent, ReactElement } from "react";
import { useEffect, useState } from "react";

import Alert from "@mui/material/Alert";
import Button from "@mui/material/Button";
import Card from "@mui/material/Card";
import CardContent from "@mui/material/CardContent";
import Stack from "@mui/material/Stack";
import Typography from "@mui/material/Typography";

import { EmailInput } from "@components/form-elements/email/EmailInput";
import type { EmailInputValidation } from "@components/form-elements/email/EmailInput";
import { useUserSettingsMutation } from "@features/user/useUserSettingsMutation";
import { createAsyncSubmitHandler } from "@helpers/createAsyncActionBuilder";
import { requestGraphQL } from "@helpers/requestGraphQL";
import { useCurrentUser } from "@hooks/useCurrentUser";
import { buildChangeEmailMutation } from "@models/user";
import type { GraphQLCurrentUserResponse, UserMutationResponse } from "@models/user";

interface ChangeEmailFormProps {
  disabled?: boolean;
}

export const ChangeEmailForm = ({
  disabled = false
}: ChangeEmailFormProps): ReactElement => {
  const { currentUser } = useCurrentUser();
  const { applyUpdatedUser, getGraphQLAuthContext } = useUserSettingsMutation();
  const persistedEmail = currentUser?.email ?? "";
  const [email, setEmail] = useState(persistedEmail);
  const [validation, setValidation] = useState<EmailInputValidation | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    setEmail(persistedEmail);
  }, [persistedEmail]);

  const trimmedEmail = email.trim();
  const hasChanged = trimmedEmail !== persistedEmail.trim();
  const isValid = validation?.isValid ?? false;
  const isDisabled = disabled || isSubmitting;

  const handleChange = (
    event: ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
  ): void => {
    setEmail(event.target.value);
  };

  const handleCancel = (): void => {
    setEmail(persistedEmail);
    setErrorMessage(null);
  };

  const handleApply = createAsyncSubmitHandler<
    UserMutationResponse,
    GraphQLCurrentUserResponse
  >()
    .when(() => !isDisabled && isValid && hasChanged)
    .onStart(() => {
      setErrorMessage(null);
      setIsSubmitting(true);
    })
    .request(() =>
      requestGraphQL<UserMutationResponse>({
        ...getGraphQLAuthContext(),
        document: buildChangeEmailMutation(trimmedEmail)
      })
    )
    .verify((response: UserMutationResponse) => {
      if (response.changeEmail === undefined) {
        throw new Error("GraphQL response did not include the updated user.");
      }

      return response.changeEmail;
    })
    .onSuccess(applyUpdatedUser)
    .onError((error: unknown) => {
      setErrorMessage(
        error instanceof Error ? error.message : "Failed to update the email."
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
            <Typography variant="h5">Email</Typography>
            <Typography color="textSecondary" variant="body2">
              Change the email address associated with your account.
            </Typography>
          </Stack>

          <EmailInput
            disabled={isDisabled}
            onChange={handleChange}
            onValidationChange={setValidation}
            required
            value={email}
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
                disabled={isDisabled || !isValid}
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
