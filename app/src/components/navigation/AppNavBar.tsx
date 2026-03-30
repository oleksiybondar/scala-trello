import type { ReactElement } from "react";

import AppBar from "@mui/material/AppBar";
import Box from "@mui/material/Box";
import Button from "@mui/material/Button";
import Chip from "@mui/material/Chip";
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
      <Toolbar disableGutters sx={{ gap: 2, py: 2 }}>
        <Typography
          color="textPrimary"
          component={RouterLink}
          sx={{ flexGrow: 1, fontWeight: 700, textDecoration: "none" }}
          to="/"
          variant="h6"
        >
          Intro Into Scala App
        </Typography>

        <Stack alignItems="center" direction="row" spacing={1}>
          <Button color="inherit" component={RouterLink} to="/home">
            Home
          </Button>
          {!isAuthenticated ? (
            <Button color="inherit" component={RouterLink} to="/login">
              Login
            </Button>
          ) : null}
          <Chip
            color={isAuthenticated ? "success" : "default"}
            label={getStatusLabel(status)}
            size="small"
            variant={isAuthenticated ? "filled" : "outlined"}
          />
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
