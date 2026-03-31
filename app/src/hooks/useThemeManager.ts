import { useContext } from "react";

import { ThemeManagerContext } from "@contexts/theme-manager-context";
import type { ThemeManagerContextValue } from "@theme/index";

export const useThemeManager = (): ThemeManagerContextValue => {
  return useContext(ThemeManagerContext);
};
