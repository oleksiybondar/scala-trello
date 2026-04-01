import type { ReactElement } from "react";

import AppBar from "@mui/material/AppBar";
import Box from "@mui/material/Box";
import Button from "@mui/material/Button";
import Chip from "@mui/material/Chip";
import Link from "@mui/material/Link";
import Stack from "@mui/material/Stack";
import Toolbar from "@mui/material/Toolbar";
import Typography from "@mui/material/Typography";
import { Link as RouterLink } from "react-router-dom";

import { useAuth } from "@hooks/useAuth";

const getStatusLabel = (status: string): string => {
  switch (status) {
    case "authenticating":
      return "Signing in";
    case "authenticated":
      return "Authenticated";
    case "refreshing":
      return "Refreshing";
    default:
      return "Anonymous";
  }
};

export const AppNavBar = (): ReactElement => {
  const { isAuthenticated, logout, session, status } = useAuth();

  return (
    <AppBar color="transparent" elevation={0} position="static">
      <Toolbar disableGutters>
        <Stack alignItems="center" direction="row" flexGrow={1} spacing={2}>
        <Link
          color="textPrimary"
          component={RouterLink}
          to="/"
          underline="none"
          variant="h6"
        >
          Intro Into Scala App
        </Link>

        <Stack alignItems="center" direction="row" spacing={1}>
          <Button color="inherit" component={RouterLink} to="/home">
            Home
          </Button>
          <Button color="inherit" component={RouterLink} to="/settings/profile">
            Settings
          </Button>
          {!isAuthenticated ? (
            <Button color="inherit" component={RouterLink} to="/login">
              Login
            </Button>
          ) : null}
          {!isAuthenticated ? (
            <Button color="inherit" component={RouterLink} to="/register">
              Register
            </Button>
          ) : null}
          <Chip
            color={isAuthenticated ? "success" : "default"}
            label={getStatusLabel(status)}
            size="small"
            variant={isAuthenticated ? "filled" : "outlined"}
          />
        </Stack>
        </Stack>

        <Box minWidth={{ sm: 220, xs: "auto" }}>
          {isAuthenticated && session !== null ? (
            <Stack
              alignItems={{ sm: "flex-end", xs: "center" }}
              direction={{ sm: "column", xs: "row" }}
              spacing={1}
            >
              <Typography color="textSecondary" variant="body2">
                {session.tokenType} {session.accessToken.slice(0, 12)}...
              </Typography>
              <Button
                color="inherit"
                onClick={() => {
                  void logout();
                }}
                variant="outlined"
              >
                Logout
              </Button>
            </Stack>
          ) : null}
        </Box>
      </Toolbar>
    </AppBar>
  );
};
