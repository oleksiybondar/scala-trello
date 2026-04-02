import type { PropsWithChildren, ReactElement } from "react";
import { useEffect, useState } from "react";

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
  }, [accessToken, session]);

  const userId = currentUser?.userId ?? null;

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
