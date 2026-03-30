import type { PropsWithChildren, ReactElement } from "react";

import CssBaseline from "@mui/material/CssBaseline";
import { ThemeProvider } from "@mui/material";

import { useThemeManager } from "@hooks/useThemeManager";
import { createAppTheme } from "@theme/index";

export const AppThemeProvider = ({
  children
}: PropsWithChildren): ReactElement => {
  const { resolvedMode, resolvedTemplateName } = useThemeManager();
  const theme = createAppTheme(resolvedTemplateName, resolvedMode);

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      {children}
    </ThemeProvider>
  );
};
