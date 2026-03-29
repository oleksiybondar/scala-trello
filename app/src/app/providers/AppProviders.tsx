import type { PropsWithChildren, ReactElement } from "react";

import { AppThemeProvider } from "@app/providers/AppThemeProvider";
import { ThemeManagerProvider } from "@app/providers/ThemeManagerProvider";

export const AppProviders = ({
  children
}: PropsWithChildren): ReactElement => {
  return (
    <ThemeManagerProvider>
      <AppThemeProvider>{children}</AppThemeProvider>
    </ThemeManagerProvider>
  );
};
