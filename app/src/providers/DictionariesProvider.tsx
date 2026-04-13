import type { PropsWithChildren, ReactElement } from "react";

import { DictionariesContext } from "@contexts/dictionaries-context";
import { useDictionariesService } from "../domain/dictionaries/useDictionariesService";

export const DictionariesProvider = ({
  children
}: PropsWithChildren): ReactElement => {
  const dictionariesService = useDictionariesService();

  return (
    <DictionariesContext.Provider
      value={{
        dictionariesError: dictionariesService.dictionariesError,
        hasLoadedDictionaries: dictionariesService.hasLoadedDictionaries,
        isLoadingDictionaries: dictionariesService.isLoadingDictionaries,
        loadDictionaries: dictionariesService.loadDictionaries,
        roles: dictionariesService.roles,
        severities: dictionariesService.severities,
        timeTrackingActivities: dictionariesService.timeTrackingActivities
      }}
    >
      {children}
    </DictionariesContext.Provider>
  );
};
