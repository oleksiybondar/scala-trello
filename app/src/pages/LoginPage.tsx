import type { ChangeEvent, ReactElement, SyntheticEvent } from "react";
import { useEffect, useState } from "react";

import Alert from "@mui/material/Alert";
import Box from "@mui/material/Box";
import Button from "@mui/material/Button";
import Container from "@mui/material/Container";
import Paper from "@mui/material/Paper";
import Stack from "@mui/material/Stack";
import TextField from "@mui/material/TextField";
import Typography from "@mui/material/Typography";
import { useNavigate } from "react-router-dom";

import { AppNavBar } from "@components/navigation/AppNavBar";
import { useAuth } from "@hooks/useAuth";

interface LoginFormState {
  login: string;
  password: string;
}

const INITIAL_FORM_STATE: LoginFormState = {
  login: "",
  password: ""
};

export const LoginPage = (): ReactElement => {
  const navigate = useNavigate();
  const { isAuthenticated, login, status } = useAuth();
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [formState, setFormState] = useState(INITIAL_FORM_STATE);

  useEffect(() => {
    if (isAuthenticated) {
      void navigate("/home", {
        replace: true
      });
    }
  }, [isAuthenticated, navigate]);

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
    <Container maxWidth="md">
      <Stack minHeight="100vh" py={4} spacing={4}>
        <AppNavBar />

        <Box display="grid" flexGrow={1} sx={{ placeItems: "center" }}>
          <Paper sx={{ p: 4, width: "100%" }}>
            <Stack component="form" onSubmit={handleSubmit} spacing={3}>
              <Stack spacing={1}>
                <Typography color="primary" variant="overline">
                  Authentication
                </Typography>
                <Typography variant="h3">Sign in</Typography>
                <Typography color="textSecondary" variant="body1">
                  The form only allows one in-flight login request at a time.
                </Typography>
              </Stack>

              {errorMessage !== null ? (
                <Alert severity="error">{errorMessage}</Alert>
              ) : null}

              <TextField
                autoComplete="username"
                disabled={isSubmitting}
                label="Login"
                onChange={handleChange("login")}
                required
                value={formState.login}
              />

              <TextField
                autoComplete="current-password"
                disabled={isSubmitting}
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
            </Stack>
          </Paper>
        </Box>
      </Stack>
    </Container>
  );
};
