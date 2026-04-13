import { useDictionaries } from "@hooks/useDictionaries";

interface UseActivitiesResult {
  activities: ReturnType<typeof useDictionaries>["timeTrackingActivities"];
  activitiesError: ReturnType<typeof useDictionaries>["dictionariesError"];
  hasLoadedActivities: ReturnType<typeof useDictionaries>["hasLoadedDictionaries"];
  isLoadingActivities: ReturnType<typeof useDictionaries>["isLoadingDictionaries"];
  loadActivities: ReturnType<typeof useDictionaries>["loadDictionaries"];
}

export const useActivities = (): UseActivitiesResult => {
  const dictionaries = useDictionaries();

  return {
    activities: dictionaries.timeTrackingActivities,
    activitiesError: dictionaries.dictionariesError,
    hasLoadedActivities: dictionaries.hasLoadedDictionaries,
    isLoadingActivities: dictionaries.isLoadingDictionaries,
    loadActivities: dictionaries.loadDictionaries
  };
};
