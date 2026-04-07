import type { ReactElement } from "react";

import Box from "@mui/material/Box";
import Container from "@mui/material/Container";
import Stack from "@mui/material/Stack";

import { LandingBoardsSection } from "@components/landing/LandingBoardsSection";
import { LandingCollaborationSection } from "@components/landing/LandingCollaborationSection";
import { LandingHeroSection } from "@components/landing/LandingHeroSection";
import { AppNavBar } from "@components/navigation/AppNavBar";

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
            <LandingHeroSection />
            <LandingBoardsSection />
            <LandingCollaborationSection />
          </Stack>
        </Stack>
      </Container>
    </Box>
  );
};
