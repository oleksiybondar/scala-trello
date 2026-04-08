import type { ReactElement } from "react";

import { Navigate } from "react-router-dom";

import { useAuth } from "@hooks/useAuth";
import { HomePage } from "@pages/HomePage";

/**
 * Root route entry point.
 *
 * Anonymous users see the landing page, while authenticated users are sent to
 * their boards area.
 */
export const RootPage = (): ReactElement => {
  const { isAuthenticated } = useAuth();

  if (isAuthenticated) {
    return <Navigate replace to="/boards" />;
  }

  return <HomePage />;
};
