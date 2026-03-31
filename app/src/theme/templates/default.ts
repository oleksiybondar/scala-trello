import type { ThemeTemplate } from "@theme/types";

export const defaultTheme: ThemeTemplate = {
  dark: {
    palette: {
      mode: "dark",
      background: {
        default: "#111827",
        paper: "#1f2937"
      },
      primary: {
        main: "#90caf9"
      },
      secondary: {
        main: "#f48fb1"
      },
      text: {
        primary: "#f9fafb",
        secondary: "#cbd5e1"
      }
    },
    shape: {
      borderRadius: 20
    },
    typography: {
      fontFamily: [
        "Inter",
        "-apple-system",
        "BlinkMacSystemFont",
        "\"Segoe UI\"",
        "sans-serif"
      ].join(","),
      h1: {
        fontSize: "3rem",
        fontWeight: 700,
        lineHeight: 1
      },
      overline: {
        fontWeight: 700,
        letterSpacing: "0.12em"
      }
    }
  },
  light: {
    palette: {
      mode: "light",
      background: {
        default: "#f7f8fc",
        paper: "#ffffff"
      },
      primary: {
        main: "#1d4ed8"
      },
      secondary: {
        main: "#7c3aed"
      },
      text: {
        primary: "#111827",
        secondary: "#475569"
      }
    },
    shape: {
      borderRadius: 20
    },
    typography: {
      fontFamily: [
        "Inter",
        "-apple-system",
        "BlinkMacSystemFont",
        "\"Segoe UI\"",
        "sans-serif"
      ].join(","),
      h1: {
        fontSize: "3rem",
        fontWeight: 700,
        lineHeight: 1
      },
      overline: {
        fontWeight: 700,
        letterSpacing: "0.12em"
      }
    }
  }
};
