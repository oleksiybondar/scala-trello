import { useQueryClient } from "@tanstack/react-query";

import { useAuth } from "@hooks/useAuth";
import { useCurrentUser } from "@hooks/useCurrentUser";
import { mapGraphQLCurrentUserResponseToCurrentUser } from "@models/user";
import type { GraphQLCurrentUserResponse } from "@models/user";

interface GraphQLAuthContext {
  accessToken: string | null;
  tokenType?: string;
}

interface UserSettingsMutationHelpers {
  applyUpdatedUser: (response: GraphQLCurrentUserResponse) => Promise<void>;
  getGraphQLAuthContext: () => GraphQLAuthContext;
  refreshUserState: () => Promise<void>;
}

export const useUserSettingsMutation = (): UserSettingsMutationHelpers => {
  const queryClient = useQueryClient();
  const { accessToken, session } = useAuth();
  const { refreshCurrentUser, setCurrentUser } = useCurrentUser();

  const getGraphQLAuthContext = (): GraphQLAuthContext => {
    return {
      accessToken,
      ...(session?.tokenType === undefined
        ? {}
        : {
            tokenType: session.tokenType
          })
    };
  };

  const applyUpdatedUser = async (
    response: GraphQLCurrentUserResponse
  ): Promise<void> => {
    setCurrentUser(mapGraphQLCurrentUserResponseToCurrentUser(response));
    await queryClient.invalidateQueries({
      queryKey: ["current-user-profile"]
    });
  };

  const refreshUserState = async (): Promise<void> => {
    await refreshCurrentUser();
    await queryClient.invalidateQueries({
      queryKey: ["current-user-profile"]
    });
  };

  return {
    applyUpdatedUser,
    getGraphQLAuthContext,
    refreshUserState
  };
};
