import { useDictionaries } from "@hooks/useDictionaries";

interface UseSeveritiesResult {
  hasLoadedSeverities: ReturnType<typeof useDictionaries>["hasLoadedDictionaries"];
  isLoadingSeverities: ReturnType<typeof useDictionaries>["isLoadingDictionaries"];
  loadSeverities: ReturnType<typeof useDictionaries>["loadDictionaries"];
  severities: ReturnType<typeof useDictionaries>["severities"];
  severitiesError: ReturnType<typeof useDictionaries>["dictionariesError"];
}

export const useSeverities = (): UseSeveritiesResult => {
  const dictionaries = useDictionaries();

  return {
    hasLoadedSeverities: dictionaries.hasLoadedDictionaries,
    isLoadingSeverities: dictionaries.isLoadingDictionaries,
    loadSeverities: dictionaries.loadDictionaries,
    severities: dictionaries.severities,
    severitiesError: dictionaries.dictionariesError
  };
};
