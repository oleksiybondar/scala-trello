import type { PropsWithChildren, ReactElement } from "react";

import { AppThemeProvider } from "@providers/AppThemeProvider";
import { AuthProvider } from "@providers/AuthProvider";
import { CurrentUserProvider } from "@providers/CurrentUserProvider";
import { QueryProvider } from "@providers/QueryProvider";
import { ThemeManagerProvider } from "@providers/ThemeManagerProvider";

export const AppProviders = ({
  children
}: PropsWithChildren): ReactElement => {
  return (
    <ThemeManagerProvider>
      <QueryProvider>
        <AuthProvider>
          <CurrentUserProvider>
            <AppThemeProvider>{children}</AppThemeProvider>
          </CurrentUserProvider>
        </AuthProvider>
      </QueryProvider>
    </ThemeManagerProvider>
  );
};
