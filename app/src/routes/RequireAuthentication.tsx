import type { ReactElement, ReactNode } from "react";

import { Navigate, useLocation } from "react-router-dom";

import { useAuth } from "@hooks/useAuth";

interface RequireAuthenticationProps {
  children: ReactNode;
}

/**
 * Route guard for authenticated-only application areas.
 */
export const RequireAuthentication = ({
  children
}: RequireAuthenticationProps): ReactElement => {
  const location = useLocation();
  const { isAuthenticated } = useAuth();

  if (!isAuthenticated) {
    return <Navigate replace state={{ from: location }} to="/login" />;
  }

  return <>{children}</>;
};
