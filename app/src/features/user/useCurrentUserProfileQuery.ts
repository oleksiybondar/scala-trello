import { useQuery } from "@tanstack/react-query";
import type { UseQueryResult } from "@tanstack/react-query";

import { useAuth } from "@hooks/useAuth";
import { useCurrentUser } from "@hooks/useCurrentUser";
import { requestGraphQL } from "@helpers/requestGraphQL";
import { buildCurrentUserProfileQuery, getCurrentUserDisplayName } from "@models/user";
import type {
  CurrentUserProfileResponse,
  CurrentUserProfileViewModel
} from "@models/user";

export const useCurrentUserProfileQuery = (): UseQueryResult<
  CurrentUserProfileViewModel
> => {
  const { accessToken, session } = useAuth();
  const { userId } = useCurrentUser();

  return useQuery({
    enabled: userId !== null && accessToken !== null,
    queryFn: async () => {
      if (userId === null) {
        throw new Error("Current user id is required to load the profile.");
      }

      const response = await requestGraphQL<CurrentUserProfileResponse>({
        accessToken,
        document: buildCurrentUserProfileQuery(userId),
        ...(session?.tokenType === undefined
          ? {}
          : {
              tokenType: session.tokenType
            })
      });

      return {
        displayName: getCurrentUserDisplayName(
          response.user?.firstName ?? null,
          response.user?.lastName ?? null
        )
      };
    },
    queryKey: ["current-user-profile", userId]
  });
};
