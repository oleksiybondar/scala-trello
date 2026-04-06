import path from "node:path";

import { defineConfig, loadEnv } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), "");
  const backendTarget = env.API_URL || "http://127.0.0.1:8080";

  return {
    server: {
      proxy: {
        "/auth": {
          target: backendTarget,
          changeOrigin: true
        },
        "/graphql": {
          target: backendTarget,
          changeOrigin: true
        }
      }
    },
    resolve: {
      alias: {
        "@app": path.resolve(__dirname, "src/app"),
        "@components": path.resolve(__dirname, "src/components"),
        "@configs": path.resolve(__dirname, "src/configs"),
        "@contexts": path.resolve(__dirname, "src/contexts"),
        "@features": path.resolve(__dirname, "src/features"),
        "@helpers": path.resolve(__dirname, "src/helpers"),
        "@hooks": path.resolve(__dirname, "src/hooks"),
        "@models": path.resolve(__dirname, "src/models"),
        "@pages": path.resolve(__dirname, "src/pages"),
        "@providers": path.resolve(__dirname, "src/providers"),
        "@routes": path.resolve(__dirname, "src/routes"),
        "@tests": path.resolve(__dirname, "tests"),
        "@theme": path.resolve(__dirname, "src/theme")
      }
    },
    plugins: [react()]
  };
});
