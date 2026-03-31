import type { PropsWithChildren, ReactElement } from "react";

import { CurrentUserContext } from "@contexts/current-user-context";
import { useAuth } from "@hooks/useAuth";

const normalizeBase64Url = (value: string): string => {
  const normalizedValue = value.replace(/-/g, "+").replace(/_/g, "/");
  const requiredPadding = (4 - (normalizedValue.length % 4)) % 4;

  return normalizedValue.padEnd(normalizedValue.length + requiredPadding, "=");
};

interface JwtPayload {
  sub?: unknown;
}

const getUserIdFromAccessToken = (accessToken: string | null): string | null => {
  if (accessToken === null) {
    return null;
  }

  const tokenParts = accessToken.split(".");

  if (tokenParts.length !== 3) {
    return null;
  }

  const [, payload] = tokenParts;

  if (payload === undefined || payload.length === 0) {
    return null;
  }

  try {
    const decodedPayload = window.atob(normalizeBase64Url(payload));
    const parsedPayload = JSON.parse(decodedPayload) as JwtPayload;

    return typeof parsedPayload.sub === "string" && parsedPayload.sub.length > 0
      ? parsedPayload.sub
      : null;
  } catch {
    return null;
  }
};

export const CurrentUserProvider = ({
  children
}: PropsWithChildren): ReactElement => {
  const { accessToken, isAuthenticated } = useAuth();

  const userId = isAuthenticated ? getUserIdFromAccessToken(accessToken) : null;
  const currentUser = userId === null ? null : { userId };

  return (
    <CurrentUserContext.Provider
      value={{
        currentUser,
        userId
      }}
    >
      {children}
    </CurrentUserContext.Provider>
  );
};
