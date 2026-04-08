import type { PropsWithChildren, ReactElement } from "react";

import Container from "@mui/material/Container";
import Stack from "@mui/material/Stack";

interface AppContentProps extends PropsWithChildren {
  containerMaxWidth?: "lg" | "md" | "sm" | "xl" | false | undefined;
}

/**
 * Shared main-content wrapper used by {@link AppPageLayout}.
 *
 * This component owns the constrained page container and the inner vertical
 * stack for page content. It stays separate from `AppPageLayout` so the shell
 * composition remains explicit: navbar, content, footer.
 */
export const AppContent = ({
  children,
  containerMaxWidth = "lg"
}: AppContentProps): ReactElement => {
  return (
    <Container component="main" maxWidth={containerMaxWidth} sx={{ flexGrow: 1 }}>
      <Stack py={4} spacing={3}>
        {children}
      </Stack>
    </Container>
  );
};
