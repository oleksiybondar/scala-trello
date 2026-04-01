import type { ChangeEvent, ReactElement, SyntheticEvent } from "react";
import { useState } from "react";

import Alert from "@mui/material/Alert";
import Button from "@mui/material/Button";
import Stack from "@mui/material/Stack";
import TextField from "@mui/material/TextField";
import Typography from "@mui/material/Typography";
import Link from "@mui/material/Link";
import { Link as RouterLink } from "react-router-dom";

import { EmailInput } from "@components/form-elements/email/EmailInput";
import type { EmailInputValidation } from "@components/form-elements/email/EmailInput";
import { PasswordInputWithConfirmation } from "@components/form-elements/password/PasswordInputWithConfirmation";
import type { PasswordInputWithConfirmationValidation } from "@components/form-elements/password/PasswordInputWithConfirmation";
import { useAuth } from "@hooks/useAuth";

interface RegisterFormState {
  email: string;
  firstName: string;
  lastName: string;
  password: string;
  passwordConfirmation: string;
}

const INITIAL_FORM_STATE: RegisterFormState = {
  email: "",
  firstName: "",
  lastName: "",
  password: "",
  passwordConfirmation: ""
};

export const RegisterForm = (): ReactElement => {
  const { isAuthenticated, register, status } = useAuth();
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [emailValidation, setEmailValidation] =
    useState<EmailInputValidation | null>(null);
  const [passwordValidation, setPasswordValidation] =
    useState<PasswordInputWithConfirmationValidation | null>(null);
  const [formState, setFormState] = useState(INITIAL_FORM_STATE);

  const isSubmitting = status === "authenticating";
  const hasFirstName = formState.firstName.trim().length > 0;
  const hasLastName = formState.lastName.trim().length > 0;
  const isEmailValid = emailValidation?.isValid ?? false;
  const isPasswordValid = passwordValidation?.isValid ?? false;
  const isSubmitDisabled =
    isSubmitting ||
    isAuthenticated ||
    !hasFirstName ||
    !hasLastName ||
    !isEmailValid ||
    !isPasswordValid;

  const handleChange =
    (field: keyof RegisterFormState) =>
    (event: ChangeEvent<HTMLInputElement | HTMLTextAreaElement>): void => {
      setFormState(currentState => ({
        ...currentState,
        [field]: event.target.value
      }));
    };

  const handleSubmit = async (
    event: SyntheticEvent<HTMLFormElement>
  ): Promise<void> => {
    event.preventDefault();

    if (isSubmitDisabled) {
      return;
    }

    setErrorMessage(null);

    try {
      await register({
        email: formState.email.trim(),
        first_name: formState.firstName.trim(),
        last_name: formState.lastName.trim(),
        password: formState.password
      });
      setFormState(INITIAL_FORM_STATE);
    } catch (error) {
      setErrorMessage(
        error instanceof Error ? error.message : "Registration request failed."
      );
    }
  };

  return (
    <Stack component="form" onSubmit={handleSubmit} spacing={3}>
      <Stack spacing={1}>
        <Typography variant="h3">Create account</Typography>
        <Typography color="textSecondary" variant="body2">
          Registration immediately creates an authenticated session in this pet
          project. That is an intentional simplification, not a missing email
          verification flow.
        </Typography>
      </Stack>

      {errorMessage !== null ? <Alert severity="error">{errorMessage}</Alert> : null}

      <EmailInput
        label="Email"
        onChange={handleChange("email")}
        onValidationChange={setEmailValidation}
        required
        value={formState.email}
      />

      <TextField
        disabled={isSubmitting}
        fullWidth
        label="First name"
        onChange={handleChange("firstName")}
        required
        value={formState.firstName}
      />

      <TextField
        disabled={isSubmitting}
        fullWidth
        label="Last name"
        onChange={handleChange("lastName")}
        required
        value={formState.lastName}
      />

      <PasswordInputWithConfirmation
        confirmationValue={formState.passwordConfirmation}
        disabled={isSubmitting}
        onConfirmationChange={handleChange("passwordConfirmation")}
        onPasswordChange={handleChange("password")}
        onValidationChange={setPasswordValidation}
        passwordValue={formState.password}
        required
      />

      <Button
        disabled={isSubmitDisabled}
        size="large"
        type="submit"
        variant="contained"
      >
        {isSubmitting ? "Creating account..." : "Register"}
      </Button>

      <Link component={RouterLink} to="/login" variant="body2">
        Already have an account? Sign in
      </Link>
    </Stack>
  );
};
