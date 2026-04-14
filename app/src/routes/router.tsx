import { createBrowserRouter, Navigate } from "react-router-dom";

import { BoardGeneralSettingsPage } from "@pages/BoardGeneralSettingsPage";
import { BoardMembersSettingsPage } from "@pages/BoardMembersSettingsPage";
import { BoardOwnershipSettingsPage } from "@pages/BoardOwnershipSettingsPage";
import { BoardPage } from "@pages/BoardPage";
import { BoardSettingsPage } from "@pages/BoardSettingsPage";
import { LoginPage } from "@pages/LoginPage";
import { MyBoardsPage } from "@pages/MyBoardsPage";
import { MyTimeRegistrationPage } from "@pages/MyTimeRegistrationPage";
import { MyTicketsPage } from "@pages/MyTicketsPage";
import { RegisterPage } from "@pages/RegisterPage";
import { RootPage } from "@pages/RootPage";
import { TicketPage } from "@pages/TicketPage";
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
    path: "/tickets",
    element: (
      <RequireAuthentication>
        <MyTicketsPage />
      </RequireAuthentication>
    )
  },
  {
    path: "/time-registration",
    element: (
      <RequireAuthentication>
        <MyTimeRegistrationPage />
      </RequireAuthentication>
    )
  },
  {
    path: "/tickets/:ticketId",
    element: (
      <RequireAuthentication>
        <TicketPage />
      </RequireAuthentication>
    )
  },
  {
    path: "/boards/:boardId",
    element: (
      <RequireAuthentication>
        <BoardPage />
      </RequireAuthentication>
    )
  },
  {
    path: "/boards/:boardId/settings",
    element: (
      <RequireAuthentication>
        <BoardSettingsPage />
      </RequireAuthentication>
    ),
    children: [
      {
        index: true,
        element: <Navigate replace to="general" />
      },
      {
        path: "general",
        element: <BoardGeneralSettingsPage />
      },
      {
        path: "members",
        element: <BoardMembersSettingsPage />
      },
      {
        path: "ownership",
        element: <BoardOwnershipSettingsPage />
      }
    ]
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
