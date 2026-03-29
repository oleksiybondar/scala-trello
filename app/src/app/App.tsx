import type { ReactElement } from "react";

import { AppProviders } from "@app/providers/AppProviders";
import { AppRouter } from "@routes/AppRouter";

export const App = (): ReactElement => {
  return (
    <AppProviders>
      <AppRouter />
    </AppProviders>
  );
};
