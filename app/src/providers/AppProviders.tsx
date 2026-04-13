import type { PropsWithChildren, ReactElement } from "react";

import { AppThemeProvider } from "@providers/AppThemeProvider";
import { AuthProvider } from "@providers/AuthProvider";
import { CurrentUserProvider } from "@providers/CurrentUserProvider";
import { DictionariesProvider } from "@providers/DictionariesProvider";
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
            <DictionariesProvider>
              <AppThemeProvider>{children}</AppThemeProvider>
            </DictionariesProvider>
          </CurrentUserProvider>
        </AuthProvider>
      </QueryProvider>
    </ThemeManagerProvider>
  );
};
