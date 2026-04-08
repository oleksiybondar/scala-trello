import type { PropsWithChildren, ReactElement } from "react";

import Box from "@mui/material/Box";

import { AppFooter } from "@components/footer/AppFooter";
import { AppContent } from "@components/layout/AppContent";
import { AppNavBar } from "@components/navigation/AppNavBar";

interface AppPageLayoutProps extends PropsWithChildren {
  containerMaxWidth?: "lg" | "md" | "sm" | "xl" | false | undefined;
}

/**
 * Shared top-level page composition.
 *
 * This component intentionally stays thin and only defines the outer shell:
 * navbar, main content region, and footer. The actual main-content container
 * is delegated to {@link AppContent}, which keeps the shell composition
 * explicit while still avoiding repeated layout scaffolding on every page.
 */
export const AppPageLayout = ({
  children,
  containerMaxWidth = "lg"
}: AppPageLayoutProps): ReactElement => {
  return (
    <Box display="flex" flexDirection="column" minHeight="100vh">
      <AppNavBar />

      <AppContent containerMaxWidth={containerMaxWidth}>
        {children}
      </AppContent>

      <AppFooter />
    </Box>
  );
};
