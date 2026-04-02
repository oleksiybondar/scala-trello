import type { PropsWithChildren, ReactElement } from "react";
import { useCallback, useEffect, useState } from "react";

import { CurrentUserContext } from "@contexts/current-user-context";
import type { CurrentUserContextValue } from "@contexts/current-user-context";
import { meRequest } from "@features/auth/authApi";
import { useAuth } from "@hooks/useAuth";
import { mapAuthCurrentUserResponseToCurrentUser } from "@models/user";

export const CurrentUserProvider = ({
  children
}: PropsWithChildren): ReactElement => {
  const { accessToken, session } = useAuth();
  const [currentUser, setCurrentUser] = useState<CurrentUserContextValue["currentUser"]>(
    null
  );

  const refreshCurrentUser = useCallback(async (): Promise<void> => {
    if (accessToken === null || session === null) {
      setCurrentUser(null);

      return;
    }

    const response = await meRequest(accessToken, session.tokenType);
    setCurrentUser(mapAuthCurrentUserResponseToCurrentUser(response));
  }, [accessToken, session]);

  useEffect(() => {
    if (accessToken === null || session === null) {
      setCurrentUser(null);

      return;
    }

    let isActive = true;

    void meRequest(accessToken, session.tokenType)
      .then(response => {
        if (!isActive) {
          return;
        }

        setCurrentUser(mapAuthCurrentUserResponseToCurrentUser(response));
      })
      .catch(() => {
        if (!isActive) {
          return;
        }

        setCurrentUser(null);
      });

    return () => {
      isActive = false;
    };
  }, [accessToken, refreshCurrentUser, session]);

  const userId = currentUser?.userId ?? null;

  return (
    <CurrentUserContext.Provider
      value={{
        currentUser,
        refreshCurrentUser,
        setCurrentUser,
        userId
      }}
    >
      {children}
    </CurrentUserContext.Provider>
  );
};
