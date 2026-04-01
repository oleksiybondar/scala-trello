import type { ReactElement } from "react";
import { useEffect } from "react";

import Card from "@mui/material/Card";
import CardContent from "@mui/material/CardContent";
import Container from "@mui/material/Container";
import Stack from "@mui/material/Stack";
import { useNavigate } from "react-router-dom";

import { RegisterForm } from "@components/forms/auth/RegisterForm";
import { AppNavBar } from "@components/navigation/AppNavBar";
import { useAuth } from "@hooks/useAuth";

export const RegisterPage = (): ReactElement => {
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();

  useEffect(() => {
    if (isAuthenticated) {
      void navigate("/home", {
        replace: true
      });
    }
  }, [isAuthenticated, navigate]);

  return (
    <Container maxWidth="md">
      <Stack minHeight="100vh" py={4} spacing={4}>
        <AppNavBar />

        <Stack alignItems="center" flexGrow={1} justifyContent="center">
          <Card variant="outlined">
            <CardContent>
              <RegisterForm />
            </CardContent>
          </Card>
        </Stack>
      </Stack>
    </Container>
  );
};
