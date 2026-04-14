import { useContext } from "react";

import { DictionariesContext } from "@contexts/dictionaries-context";
import type { DictionariesContextValue } from "@contexts/dictionaries-context";

export const useDictionaries = (): DictionariesContextValue => {
  return useContext(DictionariesContext);
};
