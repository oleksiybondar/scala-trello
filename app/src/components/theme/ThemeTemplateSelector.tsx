import type { ReactElement } from "react";

import FormControl from "@mui/material/FormControl";
import InputLabel from "@mui/material/InputLabel";
import MenuItem from "@mui/material/MenuItem";
import Select from "@mui/material/Select";

import { useThemeManager } from "@hooks/useThemeManager";
import type { ThemeTemplateName } from "@theme/index";

const formatThemeTemplateLabel = (templateName: ThemeTemplateName): string => {
  return templateName.slice(0, 1).toUpperCase() + templateName.slice(1);
};

export const ThemeTemplateSelector = (): ReactElement => {
  const { availableTemplates, setTemplateName, source, templateName } =
    useThemeManager();

  return (
    <FormControl disabled={source !== "user"} fullWidth>
      <InputLabel id="theme-template-label">Theme template</InputLabel>
      <Select
        label="Theme template"
        labelId="theme-template-label"
        onChange={event => {
          setTemplateName(event.target.value as ThemeTemplateName);
        }}
        value={templateName}
      >
        {availableTemplates.map(availableTemplate => {
          return (
            <MenuItem key={availableTemplate} value={availableTemplate}>
              {formatThemeTemplateLabel(availableTemplate)}
            </MenuItem>
          );
        })}
      </Select>
    </FormControl>
  );
};
