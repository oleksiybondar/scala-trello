import type { ReactElement } from "react";

import AppBar from "@mui/material/AppBar";
import Button from "@mui/material/Button";
import Link from "@mui/material/Link";
import Stack from "@mui/material/Stack";
import Toolbar from "@mui/material/Toolbar";
import { Link as RouterLink } from "react-router-dom";

import { AppLogo } from "@components/branding/AppLogo";
import { AppUserMenu } from "@components/navigation/AppUserMenu";
import { useAuth } from "@hooks/useAuth";

export const AppNavBar = (): ReactElement => {
  const { isAuthenticated } = useAuth();
  const logoTarget = isAuthenticated ? "/boards" : "/";

  return (
    <AppBar color="primary" position="sticky">
      <Toolbar>
        <Stack alignItems="center" direction="row" flexGrow={1} spacing={2}>
          <Link
            color="textPrimary"
            component={RouterLink}
            display="inline-flex"
            to={logoTarget}
            underline="none"
          >
            <AppLogo />
          </Link>

          {isAuthenticated ? (
            <>
              <Button color="inherit" component={RouterLink} to="/boards">
                My boards
              </Button>
              <Button color="inherit" component={RouterLink} to="/tickets">
                My tickets
              </Button>
              <Button color="inherit" component={RouterLink} to="/time-registration">
                My time registration
              </Button>
            </>
          ) : null}
        </Stack>

        <AppUserMenu />
      </Toolbar>
    </AppBar>
  );
};
