import type { ChangeEvent, ReactElement } from "react";
import { useEffect, useState } from "react";

import Alert from "@mui/material/Alert";
import Card from "@mui/material/Card";
import CardContent from "@mui/material/CardContent";
import Stack from "@mui/material/Stack";
import Typography from "@mui/material/Typography";

import { EmailInput } from "@components/form-elements/email/EmailInput";
import type { EmailInputValidation } from "@components/form-elements/email/EmailInput";
import { FormActionButtons } from "@components/forms/user/FormActionButtons";
import { createAsyncSubmitHandler } from "@helpers/createAsyncActionBuilder";
import { useCurrentUser } from "@hooks/useCurrentUser";

interface ChangeEmailFormProps {
  disabled?: boolean;
}

export const ChangeEmailForm = ({
  disabled = false
}: ChangeEmailFormProps): ReactElement => {
  const { changeEmail, currentUser } = useCurrentUser();
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

  const handleApply = createAsyncSubmitHandler()
    .when(() => !isDisabled && isValid && hasChanged)
    .onStart(() => {
      setErrorMessage(null);
      setIsSubmitting(true);
    })
    .request(() => changeEmail(trimmedEmail))
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
