import type { PropsWithChildren, ReactElement } from "react";
import { useEffect, useState } from "react";

import { useMediaQuery } from "@mui/material";

import { ThemeManagerContext } from "@contexts/theme-manager-context";
import {
  loadThemeSettings,
  saveThemeSettings
} from "@theme/manager/storage";
import { resolveThemeSettings } from "@theme/manager/resolveThemeSettings";
import { themeRegistry } from "@theme/registry";
import type {
  ThemeMode,
  ThemeSettings,
  ThemeSource,
  ThemeTemplateName
} from "@theme/index";

export const ThemeManagerProvider = ({
  children
}: PropsWithChildren): ReactElement => {
  const prefersDarkMode = useMediaQuery("(prefers-color-scheme: dark)");
  const [settings, setSettings] = useState<ThemeSettings>(() => {
    return loadThemeSettings();
  });

  useEffect(() => {
    saveThemeSettings(settings);
  }, [settings]);

  const osMode: ThemeMode = prefersDarkMode ? "dark" : "light";
  const resolvedThemeSettings = resolveThemeSettings(settings, osMode);

  return (
    <ThemeManagerContext.Provider
      value={{
        availableTemplates: Object.keys(themeRegistry) as ThemeTemplateName[],
        mode: settings.mode,
        resolvedMode: resolvedThemeSettings.mode,
        resolvedTemplateName: resolvedThemeSettings.templateName,
        setMode: (mode: ThemeMode) => {
          setSettings(currentSettings => ({
            ...currentSettings,
            mode
          }));
        },
        setSource: (source: ThemeSource) => {
          setSettings(currentSettings => ({
            ...currentSettings,
            source
          }));
        },
        setTemplateName: (templateName: ThemeTemplateName) => {
          setSettings(currentSettings => ({
            ...currentSettings,
            templateName
          }));
        },
        source: settings.source,
        templateName: settings.templateName
      }}
    >
      {children}
    </ThemeManagerContext.Provider>
  );
};
