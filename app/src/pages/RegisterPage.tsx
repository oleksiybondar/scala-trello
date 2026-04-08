import type { ReactElement } from "react";
import { useEffect } from "react";

import Card from "@mui/material/Card";
import CardContent from "@mui/material/CardContent";
import Stack from "@mui/material/Stack";
import { useNavigate } from "react-router-dom";

import { RegisterForm } from "@components/forms/auth/RegisterForm";
import { AppPageLayout } from "@components/layout/AppPageLayout";
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
    <AppPageLayout containerMaxWidth="md">
      <Stack alignItems="center" flexGrow={1} justifyContent="center">
        <Card sx={{ maxWidth: 560, width: "100%" }}  variant="outlined">
          <CardContent>
            <RegisterForm />
          </CardContent>
        </Card>
      </Stack>
    </AppPageLayout>
  );
};
