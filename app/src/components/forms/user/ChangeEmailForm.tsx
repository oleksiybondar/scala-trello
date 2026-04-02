import type { ChangeEvent, ReactElement } from "react";
import { useEffect, useState } from "react";

import Button from "@mui/material/Button";
import Card from "@mui/material/Card";
import CardContent from "@mui/material/CardContent";
import Stack from "@mui/material/Stack";
import Typography from "@mui/material/Typography";

import { EmailInput } from "@components/form-elements/email/EmailInput";
import type { EmailInputValidation } from "@components/form-elements/email/EmailInput";
import { useCurrentUser } from "@hooks/useCurrentUser";

interface ChangeEmailFormProps {
  disabled?: boolean;
  onSubmit?: (payload: { email: string }) => Promise<void> | void;
}

export const ChangeEmailForm = ({
  disabled = false,
  onSubmit
}: ChangeEmailFormProps): ReactElement => {
  const { currentUser } = useCurrentUser();
  const persistedEmail = currentUser?.email ?? "";
  const [email, setEmail] = useState(persistedEmail);
  const [validation, setValidation] = useState<EmailInputValidation | null>(null);

  useEffect(() => {
    setEmail(persistedEmail);
  }, [persistedEmail]);

  const trimmedEmail = email.trim();
  const hasChanged = trimmedEmail !== persistedEmail.trim();
  const isValid = validation?.isValid ?? false;

  const handleChange = (
    event: ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
  ): void => {
    setEmail(event.target.value);
  };

  const handleCancel = (): void => {
    setEmail(persistedEmail);
  };

  const handleApply = async (): Promise<void> => {
    if (disabled || !isValid || !hasChanged) {
      return;
    }

    await onSubmit?.({
      email: trimmedEmail
    });
  };

  return (
    <Card variant="outlined">
      <CardContent>
        <Stack padding={3} spacing={3}>
          <Stack spacing={1}>
            <Typography variant="h5">Email</Typography>
            <Typography color="textSecondary" variant="body2">
              Change the email address associated with your account.
            </Typography>
          </Stack>

          <EmailInput
            disabled={disabled}
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
              <Button disabled={disabled} onClick={handleCancel} variant="outlined">
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
            </Stack>
          ) : null}
        </Stack>
      </CardContent>
    </Card>
  );
};
