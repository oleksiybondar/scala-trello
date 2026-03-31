import type {
  ThemeSettings,
  ThemeSource,
  ThemeTemplateName
} from "@theme/index";

const themeSettingsStorageKey = "app.theme.settings";

const isThemeSource = (value: string): value is ThemeSource => {
  return value === "default" || value === "os" || value === "user";
};

const isThemeMode = (value: string): value is ThemeSettings["mode"] => {
  return value === "light" || value === "dark";
};

const isThemeTemplateName = (value: string): value is ThemeTemplateName => {
  return value === "default";
};

export const defaultThemeSettings: ThemeSettings = {
  mode: "light",
  source: "default",
  templateName: "default"
};

export const loadThemeSettings = (): ThemeSettings => {
  if (typeof window === "undefined") {
    return defaultThemeSettings;
  }

  const rawSettings = window.localStorage.getItem(themeSettingsStorageKey);

  if (rawSettings === null) {
    return defaultThemeSettings;
  }

  try {
    const parsedSettings: unknown = JSON.parse(rawSettings);

    if (
      typeof parsedSettings !== "object" ||
      parsedSettings === null ||
      !("source" in parsedSettings) ||
      !("mode" in parsedSettings) ||
      !("templateName" in parsedSettings)
    ) {
      return defaultThemeSettings;
    }

    const { mode, source, templateName } = parsedSettings;

    if (
      typeof source !== "string" ||
      typeof mode !== "string" ||
      typeof templateName !== "string" ||
      !isThemeSource(source) ||
      !isThemeMode(mode) ||
      !isThemeTemplateName(templateName)
    ) {
      return defaultThemeSettings;
    }

    return {
      mode,
      source,
      templateName
    };
  } catch {
    return defaultThemeSettings;
  }
};

export const saveThemeSettings = (settings: ThemeSettings): void => {
  if (typeof window === "undefined") {
    return;
  }

  window.localStorage.setItem(
    themeSettingsStorageKey,
    JSON.stringify(settings)
  );
};
