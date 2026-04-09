import { useQuery } from "@tanstack/react-query";
import type { UseQueryResult } from "@tanstack/react-query";

import { useAuth } from "@hooks/useAuth";
import { requestGraphQL } from "@helpers/requestGraphQL";
import { buildRolesQuery, mapRoleResponseToRole } from "@models/role";
import type { Role, RolesQueryResponse } from "@models/role";

export const useRolesQuery = (): UseQueryResult<Role[]> => {
  const { accessToken, session } = useAuth();

  return useQuery({
    enabled: accessToken !== null && session !== null,
    queryFn: async () => {
      const response = await requestGraphQL<RolesQueryResponse>({
        accessToken,
        document: buildRolesQuery(),
        ...(session?.tokenType === undefined
          ? {}
          : {
              tokenType: session.tokenType
            })
      });

      return response.roles.map(mapRoleResponseToRole);
    },
    queryKey: ["roles"]
  });
};
