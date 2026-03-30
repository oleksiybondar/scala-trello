import { useQuery } from "@tanstack/react-query";
import type { UseQueryResult } from "@tanstack/react-query";

import { buildCurrentUserProfileQuery } from "@features/user/userQueries";
import { useAuth } from "@hooks/useAuth";
import { useCurrentUser } from "@hooks/useCurrentUser";
import { requestGraphQL } from "@helpers/requestGraphQL";

interface CurrentUserProfileResponse {
  user: {
    firstName: string | null;
    lastName: string | null;
  } | null;
}

interface CurrentUserProfileViewModel {
  displayName: string;
}

const getDisplayName = (
  firstName: string | null,
  lastName: string | null
): string => {
  const fullName = [firstName, lastName]
    .filter((part): part is string => part !== null && part.trim().length > 0)
    .join(" ")
    .trim();

  return fullName.length > 0 ? fullName : "Unnamed user";
};

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
        displayName: getDisplayName(
          response.user?.firstName ?? null,
          response.user?.lastName ?? null
        )
      };
    },
    queryKey: ["current-user-profile", userId]
  });
};
