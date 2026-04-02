import type { PropsWithChildren, ReactElement } from "react";
import { useCallback, useEffect, useState } from "react";

import { CurrentUserContext } from "@contexts/current-user-context";
import type { CurrentUserContextValue } from "@contexts/current-user-context";
import { meRequest } from "@features/auth/authApi";
import { createAsyncSubmitHandler } from "@helpers/createAsyncActionBuilder";
import { useAuth } from "@hooks/useAuth";
import {
  mapAuthCurrentUserResponseToCurrentUser
} from "@models/user";
import type { AuthCurrentUserResponse, CurrentUser } from "@models/user";

export const CurrentUserProvider = ({
  children
}: PropsWithChildren): ReactElement => {
  const { accessToken, session } = useAuth();
  const [currentUser, setCurrentUser] = useState<CurrentUserContextValue["currentUser"]>(
    null
  );

  const loadCurrentUserHandler = useCallback(
    async (): Promise<void> => {
      await createAsyncSubmitHandler<AuthCurrentUserResponse, CurrentUser>()
        .when(() => {
          return accessToken !== null && session !== null;
        })
        .request(() => {
          if (accessToken === null || session === null) {
            throw new Error("Authentication context is missing.");
          }

          return meRequest(accessToken, session.tokenType);
        })
        .verify(mapAuthCurrentUserResponseToCurrentUser)
        .onSuccess(user => {
          setCurrentUser(user);
        })
        .onError(() => {
          setCurrentUser(null);
        })
        .handle();
    },
    [accessToken, session]
  );

  useEffect(() => {
    if (accessToken === null || session === null) {
      setCurrentUser(null);

      return;
    }

    let isActive = true;

    void createAsyncSubmitHandler<AuthCurrentUserResponse, CurrentUser>()
      .request(() => {
        return meRequest(accessToken, session.tokenType);
      })
      .verify(mapAuthCurrentUserResponseToCurrentUser)
      .onSuccess(user => {
        if (!isActive) {
          return;
        }

        setCurrentUser(user);
      })
      .onError(() => {
        if (!isActive) {
          return;
        }

        setCurrentUser(null);
      })
      .handle();

    return () => {
      isActive = false;
    };
  }, [accessToken, loadCurrentUserHandler, session]);

  const userId = currentUser?.userId ?? null;

  return (
    <CurrentUserContext.Provider
      value={{
        currentUser,
        refreshCurrentUser: loadCurrentUserHandler,
        setCurrentUser,
        userId
      }}
    >
      {children}
    </CurrentUserContext.Provider>
  );
};
