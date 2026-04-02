import { createContext } from "react";

import type { ThemeManagerContextValue } from "@theme/index";

const missingThemeManagerProvider = (): never => {
  throw new Error("ThemeManagerContext is missing its provider.");
};

export const ThemeManagerContext = createContext<ThemeManagerContextValue>({
  availableTemplates: ["default", "Material-UI"],
  mode: "light",
  resolvedMode: "light",
  resolvedTemplateName: "default",
  setMode: missingThemeManagerProvider,
  setSettings: missingThemeManagerProvider,
  setSource: missingThemeManagerProvider,
  setTemplateName: missingThemeManagerProvider,
  source: "default",
  templateName: "default"
});
