import type { ReactElement } from "react";

import Box from "@mui/material/Box";
import Typography from "@mui/material/Typography";

const LOGO_WIDTH = 120;
const LOGO_HEIGHT = 40;
const MAJOR_SIZE = 34;
const MINOR_SIZE = 8;
const MINOR_TOP = 0;
const MINOR_LEFT = 23;

/**
 * Static text-based wordmark for the application.
 *
 * The styling here is intentionally hand-tuned and sx-heavy because this
 * project does not use a separate branded logo asset, so the visual balance
 * depends on direct font sizing and positioning adjustments.
 */
export const AppLogo = (): ReactElement => {
  return (
    <Box
      aria-label="Boards"
      role="img"
      sx={{
        color: "text.primary",
        display: "inline-block",
        fontFamily: '"Arial Black", "Helvetica Neue", sans-serif',
        height: LOGO_HEIGHT,
        position: "relative",
        userSelect: "none",
        width: LOGO_WIDTH
      }}
    >
      <Box
        sx={{
          left: MINOR_LEFT,
          position: "absolute",
          top: MINOR_TOP,
          width: "max-content"
        }}
      >
        <Typography
          sx={{
            fontSize: MINOR_SIZE,
            fontWeight: 800,
            letterSpacing: "0.08em",
            lineHeight: 1,
            textTransform: "uppercase",
            whiteSpace: "nowrap"
          }}
        >
          Trello-like
        </Typography>
      </Box>

      <Box
        sx={{
          inset: 0,
          position: "absolute"
        }}
      >
        <Typography
          color="primary"
          sx={{
            alignItems: "flex-end",
            display: "flex",
            fontSize: MAJOR_SIZE,
            fontWeight: 900,
            inset: 0,
            position: "absolute",
            whiteSpace: "nowrap",
            width: "100%"
          }}
        >
          Boards
        </Typography>
      </Box>
    </Box>
  );
};
