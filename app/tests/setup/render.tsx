import type { PropsWithChildren, ReactElement } from "react";

import { render } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";

import { AppProviders } from "@providers/AppProviders";

const TestProviders = ({ children }: PropsWithChildren): ReactElement => {
  return (
    <MemoryRouter>
      <AppProviders>{children}</AppProviders>
    </MemoryRouter>
  );
};

export const renderApp = (ui: ReactElement) => {
  return render(ui, {
    wrapper: TestProviders
  });
};
