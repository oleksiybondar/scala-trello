import { useDictionaries } from "@hooks/useDictionaries";

import type { RolesContextValue } from "@contexts/roles-context";

export const useRoles = (): RolesContextValue => {
  const dictionaries = useDictionaries();

  return {
    isLoadingRoles: dictionaries.isLoadingDictionaries,
    roles: dictionaries.roles,
    rolesError: dictionaries.dictionariesError
  };
};
