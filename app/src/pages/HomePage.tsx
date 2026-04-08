import type { ReactElement } from "react";

import Box from "@mui/material/Box";
import Container from "@mui/material/Container";
import Stack from "@mui/material/Stack";

import { LandingBoardsSection } from "@components/landing/LandingBoardsSection";
import { LandingCollaborationSection } from "@components/landing/LandingCollaborationSection";
import { LandingHeroSection } from "@components/landing/LandingHeroSection";
import { AppNavBar } from "@components/navigation/AppNavBar";
import Divider from "@mui/material/Divider";

/**
 * Visitor landing page with static introduction content.
 *
 * The page is intentionally composed from a few decoupled but still fairly
 * large section components. That is a deliberate tradeoff: the content is
 * mostly static, contains no meaningful interaction logic, and reads more
 * clearly as a small number of narrative blocks.
 *
 * Some parts of these sections may later become reusable components or be
 * replaced when real functionality is implemented, but the overall approach
 * should remain the same. Do not break this page into many tiny components
 * without a concrete need.
 */
export const HomePage = (): ReactElement => {
  return (
    <Box
      sx={{
        background:
          "radial-gradient(circle at top, rgba(25, 118, 210, 0.08), transparent 34%)"
      }}
    >
      <Container maxWidth="lg">
        <Stack minHeight="100vh" py={4} spacing={4}>
        <AppNavBar />

          <Stack pb={6} spacing={3}>
            <Divider />
            <LandingHeroSection />
            <Divider />
            <LandingBoardsSection />
            <Divider />
            <LandingCollaborationSection />
          </Stack>
        </Stack>
      </Container>
    </Box>
  );
};
