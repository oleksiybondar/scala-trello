import type { ReactElement } from "react";

import AppBar from "@mui/material/AppBar";
import Link from "@mui/material/Link";
import Stack from "@mui/material/Stack";
import Toolbar from "@mui/material/Toolbar";
import { Link as RouterLink } from "react-router-dom";

import { AppLogo } from "@components/branding/AppLogo";
import { AppUserMenu } from "@components/navigation/AppUserMenu";

export const AppNavBar = (): ReactElement => {

  return (
    <AppBar color="transparent" elevation={0} position="static">
      <Toolbar disableGutters>
        <Stack alignItems="center" direction="row" flexGrow={1} spacing={2}>
          <Link
            color="textPrimary"
            component={RouterLink}
            display="inline-flex"
            to="/"
            underline="none"
          >
            <AppLogo />
          </Link>

          <Stack alignItems="center" direction="row" spacing={1}>

          </Stack>
        </Stack>

        <AppUserMenu />
      </Toolbar>
    </AppBar>
  );
};
