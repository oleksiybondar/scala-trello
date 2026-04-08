import type { MouseEvent, ReactElement } from "react";
import { useState } from "react";

import LoginRoundedIcon from "@mui/icons-material/LoginRounded";
import LogoutRoundedIcon from "@mui/icons-material/LogoutRounded";
import SettingsRoundedIcon from "@mui/icons-material/SettingsRounded";
import ListItemIcon from "@mui/material/ListItemIcon";
import Menu from "@mui/material/Menu";
import MenuItem from "@mui/material/MenuItem";
import { useNavigate } from "react-router-dom";

import { AppUserInfo } from "@components/navigation/AppUserInfo";
import { useAuth } from "@hooks/useAuth";
import { useCurrentUser } from "@hooks/useCurrentUser";

const getAnonymousLabel = (): string => "Sign in";
const getAnonymousSecondaryLabel = (status: string): string | undefined => {
  if (status === "authenticating") {
    return "Signing in";
  }

  if (status === "refreshing") {
    return "Refreshing session";
  }

  return undefined;
};

const getFullName = (
  currentUser: ReturnType<typeof useCurrentUser>["currentUser"],
  status: string
): string => {
  if (currentUser === null) {
    return getAnonymousLabel();
  }

  const name = `${currentUser.firstName} ${currentUser.lastName}`.trim();

  if (name.length > 0) {
    return name;
  }

  if (currentUser.username !== null && currentUser.username.length > 0) {
    return currentUser.username;
  }

  if (currentUser.email !== null && currentUser.email.length > 0) {
    return currentUser.email;
  }

  if (status === "refreshing") {
    return "Refreshing";
  }

  return "User";
};

export const AppUserMenu = (): ReactElement => {
  const navigate = useNavigate();
  const { isAuthenticated, logout, status } = useAuth();
  const { currentUser } = useCurrentUser();
  const [anchorEl, setAnchorEl] = useState<HTMLElement | null>(null);

  const isOpen = anchorEl !== null;
  const fullName = getFullName(currentUser, status);
  const secondaryLabel = isAuthenticated
    ? currentUser?.email ?? currentUser?.username ?? undefined
    : getAnonymousSecondaryLabel(status);

  const handleOpen = (event: MouseEvent<HTMLElement>): void => {
    setAnchorEl(event.currentTarget);
  };

  const handleClose = (): void => {
    setAnchorEl(null);
  };

  const handleNavigate = (path: string): void => {
    handleClose();
    void navigate(path);
  };

  const handleLogout = (): void => {
    handleClose();
    void logout();
  };

  return (
    <>
      <AppUserInfo
        ariaControls={isOpen ? "app-user-menu" : undefined}
        ariaExpanded={isOpen ? "true" : undefined}
        avatarUrl={currentUser?.avatarUrl ?? undefined}
        firstName={currentUser?.firstName}
        fullName={fullName}
        isAuthenticated={isAuthenticated}
        lastName={currentUser?.lastName}
        onClick={handleOpen}
        secondaryLabel={secondaryLabel}
      />

      <Menu
        anchorOrigin={{ horizontal: "right", vertical: "bottom" }}
        anchorEl={anchorEl}
        id="app-user-menu"
        onClose={handleClose}
        open={isOpen}
        slotProps={{
          paper: {
            elevation: 4,
            variant: "outlined"
          }
        }}
        transformOrigin={{ horizontal: "right", vertical: "top" }}
      >
        {isAuthenticated ? (
          [
            <MenuItem
              key="settings"
              onClick={() => {
                handleNavigate("/settings/profile");
              }}
            >
              <ListItemIcon>
                <SettingsRoundedIcon fontSize="small" />
              </ListItemIcon>
              Settings
            </MenuItem>,
            <MenuItem
              key="logout"
              onClick={handleLogout}
            >
              <ListItemIcon>
                <LogoutRoundedIcon fontSize="small" />
              </ListItemIcon>
              Logout
            </MenuItem>
          ]
        ) : (
          <MenuItem
            onClick={() => {
              handleNavigate("/login");
            }}
          >
            <ListItemIcon>
              <LoginRoundedIcon fontSize="small" />
            </ListItemIcon>
            Login
          </MenuItem>
        )}
      </Menu>
    </>
  );
};
