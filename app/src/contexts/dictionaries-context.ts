import { createContext } from "react";

import type {
  DictionarySeverity,
  DictionaryTimeTrackingActivity
} from "../domain/dictionaries/graphql";
import type { Role } from "../domain/role/graphql";

export interface DictionariesContextValue {
  dictionariesError: Error | null;
  hasLoadedDictionaries: boolean;
  isLoadingDictionaries: boolean;
  loadDictionaries: () => Promise<void>;
  roles: Role[];
  severities: DictionarySeverity[];
  timeTrackingActivities: DictionaryTimeTrackingActivity[];
}

const missingDictionariesProvider = (): never => {
  throw new Error("DictionariesContext is missing its provider.");
};

export const DictionariesContext = createContext<DictionariesContextValue>({
  dictionariesError: null,
  hasLoadedDictionaries: false,
  isLoadingDictionaries: false,
  loadDictionaries: missingDictionariesProvider,
  roles: [],
  severities: [],
  timeTrackingActivities: []
});
