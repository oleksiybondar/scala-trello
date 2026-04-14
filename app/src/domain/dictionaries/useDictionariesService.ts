import { useRef, useState } from "react";

import { requestGraphQL } from "@helpers/requestGraphQL";
import { useAuth } from "@hooks/useAuth";
import { buildRolesQuery, mapRoleResponseToRole } from "../role/graphql";
import type { Role, RolesQueryResponse } from "../role/graphql";
import {
  buildDictionariesQuery,
  mapDictionarySeverityResponse,
  mapDictionaryTimeTrackingActivityResponse
} from "./graphql";
import type {
  DictionariesQueryResponse,
  DictionarySeverity,
  DictionaryTimeTrackingActivity
} from "./graphql";

export interface UseDictionariesService {
  dictionariesError: Error | null;
  hasLoadedDictionaries: boolean;
  isLoadingDictionaries: boolean;
  loadDictionaries: () => Promise<void>;
  roles: Role[];
  severities: DictionarySeverity[];
  timeTrackingActivities: DictionaryTimeTrackingActivity[];
}

export const useDictionariesService = (): UseDictionariesService => {
  const { accessToken, session } = useAuth();
  const [roles, setRoles] = useState<Role[]>([]);
  const [severities, setSeverities] = useState<DictionarySeverity[]>([]);
  const [timeTrackingActivities, setTimeTrackingActivities] = useState<
    DictionaryTimeTrackingActivity[]
  >([]);
  const [isLoadingDictionaries, setIsLoadingDictionaries] = useState(false);
  const [hasLoadedDictionaries, setHasLoadedDictionaries] = useState(false);
  const [dictionariesError, setDictionariesError] = useState<Error | null>(null);
  const loadPromiseRef = useRef<Promise<void> | null>(null);

  const loadDictionaries = async (): Promise<void> => {
    if (hasLoadedDictionaries) {
      return;
    }

    if (loadPromiseRef.current !== null) {
      return loadPromiseRef.current;
    }

    setIsLoadingDictionaries(true);
    setDictionariesError(null);

    const loadPromise = Promise.all([
      requestGraphQL<DictionariesQueryResponse>({
        accessToken,
        document: buildDictionariesQuery(),
        ...(session?.tokenType === undefined
          ? {}
          : {
              tokenType: session.tokenType
            })
      }),
      requestGraphQL<RolesQueryResponse>({
        accessToken,
        document: buildRolesQuery(),
        ...(session?.tokenType === undefined
          ? {}
          : {
              tokenType: session.tokenType
            })
      })
    ])
      .then(([dictionariesResponse, rolesResponse]) => {
        setRoles(rolesResponse.roles.map(mapRoleResponseToRole));
        setSeverities(dictionariesResponse.ticketSeverities.map(mapDictionarySeverityResponse));
        setTimeTrackingActivities(
          dictionariesResponse.timeTrackingActivities.map(mapDictionaryTimeTrackingActivityResponse)
        );
        setHasLoadedDictionaries(true);
      })
      .catch((error: unknown) => {
        setDictionariesError(
          error instanceof Error ? error : new Error("Failed to load dictionaries.")
        );
        throw error;
      })
      .finally(() => {
        setIsLoadingDictionaries(false);
        loadPromiseRef.current = null;
      });

    loadPromiseRef.current = loadPromise;

    return loadPromise;
  };

  return {
    dictionariesError,
    hasLoadedDictionaries,
    isLoadingDictionaries,
    loadDictionaries,
    roles,
    severities,
    timeTrackingActivities
  };
};
