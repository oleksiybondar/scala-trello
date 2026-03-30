import { createBrowserRouter, Navigate } from "react-router-dom";

import { HomePage } from "@pages/HomePage";
import { LoginPage } from "@pages/LoginPage";
import { UserProfileSettingsPage } from "@pages/UserProfileSettingsPage";
import { UserSecuritySettingsPage } from "@pages/UserSecuritySettingsPage";
import { UserUiPreferencesSettingsPage } from "@pages/UserUiPreferencesSettingsPage";
import { UserSettingsPage } from "@pages/UserSettingsPage";

export const router = createBrowserRouter([
  {
    path: "/",
    element: <HomePage />
  },
  {
    path: "/home",
    element: <HomePage />
  },
  {
    path: "/login",
    element: <LoginPage />
  },
  {
    path: "/settings",
    element: <UserSettingsPage />,
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
