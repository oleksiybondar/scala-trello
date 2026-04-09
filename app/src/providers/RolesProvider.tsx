import type { PropsWithChildren, ReactElement } from "react";

import { RolesContext } from "@contexts/roles-context";
import { useRolesQuery } from "@features/role/useRolesQuery";

export const RolesProvider = ({
  children
}: PropsWithChildren): ReactElement => {
  const rolesQuery = useRolesQuery();

  return (
    <RolesContext.Provider
      value={{
        isLoadingRoles: rolesQuery.isLoading,
        roles: rolesQuery.data ?? [],
        rolesError: rolesQuery.error instanceof Error ? rolesQuery.error : null
      }}
    >
      {children}
    </RolesContext.Provider>
  );
};
