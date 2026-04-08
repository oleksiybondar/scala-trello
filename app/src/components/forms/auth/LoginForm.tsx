import type { ChangeEvent, ReactElement, SyntheticEvent } from "react";
import { useState } from "react";

import Alert from "@mui/material/Alert";
import Button from "@mui/material/Button";
import Link from "@mui/material/Link";
import Stack from "@mui/material/Stack";
import TextField from "@mui/material/TextField";
import Typography from "@mui/material/Typography";
import { Link as RouterLink } from "react-router-dom";

import { useAuth } from "@hooks/useAuth";

interface LoginFormState {
  login: string;
  password: string;
}

const INITIAL_FORM_STATE: LoginFormState = {
  login: "",
  password: ""
};

/**
 * Sign-in form for the visitor auth flow.
 */
export const LoginForm = (): ReactElement => {
  const { isAuthenticated, login, status } = useAuth();
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [formState, setFormState] = useState(INITIAL_FORM_STATE);

  const isSubmitting = status === "authenticating";

  const handleChange =
    (field: keyof LoginFormState) =>
    (event: ChangeEvent<HTMLInputElement>): void => {
      setFormState(currentState => ({
        ...currentState,
        [field]: event.target.value
      }));
    };

  const handleSubmit = async (
    event: SyntheticEvent<HTMLFormElement>
  ): Promise<void> => {
    event.preventDefault();

    if (isSubmitting || isAuthenticated) {
      return;
    }

    setErrorMessage(null);

    try {
      await login(formState);
      setFormState(INITIAL_FORM_STATE);
    } catch (error) {
      setErrorMessage(
        error instanceof Error ? error.message : "Login request failed."
      );
    }
  };

  return (
    <Stack component="form" onSubmit={handleSubmit} spacing={3}>
      <Stack spacing={1}>
        <Typography variant="h3">Sign in</Typography>
        <Typography color="text.secondary" variant="body2">
          Access your boards, ticket workflow, and tracked sprint work from a
          single account session.
        </Typography>
      </Stack>

      {errorMessage !== null ? <Alert severity="error">{errorMessage}</Alert> : null}

      <TextField
        autoComplete="username"
        disabled={isSubmitting}
        fullWidth
        label="Login"
        onChange={handleChange("login")}
        required
        value={formState.login}
      />

      <TextField
        autoComplete="current-password"
        disabled={isSubmitting}
        fullWidth
        label="Password"
        onChange={handleChange("password")}
        required
        type="password"
        value={formState.password}
      />

      <Button
        disabled={isSubmitting}
        size="large"
        type="submit"
        variant="contained"
      >
        {isSubmitting ? "Signing in..." : "Sign in"}
      </Button>

      <Link component={RouterLink} to="/register" variant="body2">
        Need an account? Register
      </Link>
    </Stack>
  );
};
