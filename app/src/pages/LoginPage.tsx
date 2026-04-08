import type { ReactElement } from "react";
import { useEffect } from "react";

import Card from "@mui/material/Card";
import CardContent from "@mui/material/CardContent";
import Stack from "@mui/material/Stack";
import { useNavigate } from "react-router-dom";

import { LoginForm } from "@components/forms/auth/LoginForm";
import { AppPageLayout } from "@components/layout/AppPageLayout";
import { useAuth } from "@hooks/useAuth";

export const LoginPage = (): ReactElement => {
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
    <AppPageLayout containerMaxWidth="md">
      <Stack alignItems="center" flexGrow={1} justifyContent="center" width="100%">
        <Card sx={{ maxWidth: 460, width: "100%" }} variant="outlined">
          <CardContent>
            <LoginForm />
          </CardContent>
        </Card>
      </Stack>
    </AppPageLayout>
  );
};
