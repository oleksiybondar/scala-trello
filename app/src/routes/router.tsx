import { createBrowserRouter, Navigate } from "react-router-dom";

import { LoginPage } from "@pages/LoginPage";
import { MyBoardsPage } from "@pages/MyBoardsPage";
import { RegisterPage } from "@pages/RegisterPage";
import { RootPage } from "@pages/RootPage";
import { UserProfileSettingsPage } from "@pages/UserProfileSettingsPage";
import { UserSecuritySettingsPage } from "@pages/UserSecuritySettingsPage";
import { UserUiPreferencesSettingsPage } from "@pages/UserUiPreferencesSettingsPage";
import { UserSettingsPage } from "@pages/UserSettingsPage";
import { RequireAuthentication } from "@routes/RequireAuthentication";

export const router = createBrowserRouter([
  {
    path: "/",
    element: <RootPage />
  },
  {
    path: "/home",
    element: <RootPage />
  },
  {
    path: "/login",
    element: <LoginPage />
  },
  {
    path: "/register",
    element: <RegisterPage />
  },
  {
    path: "/boards",
    element: (
      <RequireAuthentication>
        <MyBoardsPage />
      </RequireAuthentication>
    )
  },
  {
    path: "/settings",
    element: (
      <RequireAuthentication>
        <UserSettingsPage />
      </RequireAuthentication>
    ),
    children: [
      {
        index: true,
        element: <Navigate replace to="/settings/profile" />
      },
      {
        path: "profile",
        element: <UserProfileSettingsPage />
      },
      {
        path: "security",
        element: <UserSecuritySettingsPage />
      },
      {
        path: "ui-preferences",
        element: <UserUiPreferencesSettingsPage />
      }
    ]
  }
]);
