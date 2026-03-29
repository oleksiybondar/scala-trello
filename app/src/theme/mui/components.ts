import type { ThemeOptions } from "@mui/material/styles";

export const getComponentOverrides = (): ThemeOptions["components"] => {
  return {
    MuiButton: {
      defaultProps: {
        disableElevation: true
      }
    },
    MuiContainer: {
      defaultProps: {
        maxWidth: "md"
      }
    },
    MuiCssBaseline: {
      styleOverrides: theme => ({
        body: {
          backgroundColor: theme.palette.background.default
        }
      })
    },
    MuiLink: {
      defaultProps: {
        underline: "hover"
      }
    },
    MuiPaper: {
      defaultProps: {
        variant: "outlined"
      }
    }
  };
};
