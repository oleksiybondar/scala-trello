import type { PropsWithChildren, ReactElement } from "react";

import { AppThemeProvider } from "@providers/AppThemeProvider";
import { AuthProvider } from "@providers/AuthProvider";
import { ThemeManagerProvider } from "@providers/ThemeManagerProvider";

export const AppProviders = ({
  children
}: PropsWithChildren): ReactElement => {
  return (
    <ThemeManagerProvider>
      <AuthProvider>
        <AppThemeProvider>{children}</AppThemeProvider>
      </AuthProvider>
    </ThemeManagerProvider>
  );
};
