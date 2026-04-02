import type { ReactElement } from "react";
import { useState } from "react";

import Button from "@mui/material/Button";
import Card from "@mui/material/Card";
import CardContent from "@mui/material/CardContent";
import Stack from "@mui/material/Stack";
import Typography from "@mui/material/Typography";

import { ThemeModeToggle } from "@components/theme/ThemeModeToggle";
import { ThemeSourceSelector } from "@components/theme/ThemeSourceSelector";
import { ThemeTemplateSelector } from "@components/theme/ThemeTemplateSelector";
import { useThemeManager } from "@hooks/useThemeManager";
import { saveThemeSettings } from "@theme/manager/storage";
import type { ThemeMode, ThemeSettings, ThemeSource, ThemeTemplateName } from "@theme/index";

export const UserUiPreferencesForm = (): ReactElement => {
  const {
    mode,
    resolvedMode,
    resolvedTemplateName,
    setSettings,
    setMode,
    setSource,
    setTemplateName,
    source,
    templateName
  } = useThemeManager();
  const [savedSettings, setSavedSettings] = useState({
    mode,
    source,
    templateName
  });

  const isChanged =
    source !== savedSettings.source ||
    mode !== savedSettings.mode ||
    templateName !== savedSettings.templateName;
  const displayedMode = source === "user" ? mode : resolvedMode;
  const displayedTemplateName =
    source === "user" ? templateName : resolvedTemplateName;

  const handleSourceChange = (nextSource: ThemeSource): void => {
    setSource(nextSource);
  };

  const handleModeChange = (nextMode: ThemeMode): void => {
    setMode(nextMode);
  };

  const handleTemplateChange = (nextTemplateName: ThemeTemplateName): void => {
    setTemplateName(nextTemplateName);
  };

  const handleCancel = (): void => {
    setSettings(savedSettings);
  };

  const handleApply = (): void => {
    if (!isChanged) {
      return;
    }

    const nextSavedSettings: ThemeSettings = {
      mode,
      source,
      templateName
    };

    saveThemeSettings(nextSavedSettings);
    setSavedSettings(nextSavedSettings);
  };

  return (
    <Card variant="outlined">
      <CardContent>
        <Stack padding={3} spacing={3}>
          <Stack spacing={1}>
            <Typography variant="h5">Theme preferences</Typography>
            <Typography color="textSecondary" variant="body2">
              Customize theme source, mode, and template.
            </Typography>
          </Stack>

          <ThemeSourceSelector onChange={handleSourceChange} value={source} />

          <ThemeTemplateSelector
            onChange={handleTemplateChange}
            source={source}
            value={displayedTemplateName}
          />

          <ThemeModeToggle
            mode={displayedMode}
            onChange={handleModeChange}
            source={source}
          />


          {isChanged ? (
            <Stack
              direction={{ xs: "column-reverse", sm: "row" }}
              justifyContent="flex-end"
              spacing={1.5}
            >
              <Button onClick={handleCancel} variant="outlined">
                Cancel
              </Button>
              <Button onClick={handleApply} variant="contained">
                Apply
              </Button>
            </Stack>
          ) : null}
        </Stack>
      </CardContent>
    </Card>
  );
};
