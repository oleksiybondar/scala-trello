import path from "node:path";
import { fileURLToPath } from "node:url";

import { defineConfig } from "vitest/config";

const dirname = path.dirname(fileURLToPath(import.meta.url));

export default defineConfig({
  resolve: {
    alias: {
      "@assets": path.resolve(dirname, "src/assets"),
      "@app": path.resolve(dirname, "src/app"),
      "@components": path.resolve(dirname, "src/components"),
      "@configs": path.resolve(dirname, "src/configs"),
      "@contexts": path.resolve(dirname, "src/contexts"),
      "@features": path.resolve(dirname, "src/features"),
      "@helpers": path.resolve(dirname, "src/helpers"),
      "@hooks": path.resolve(dirname, "src/hooks"),
      "@models": path.resolve(dirname, "src/models"),
      "@pages": path.resolve(dirname, "src/pages"),
      "@providers": path.resolve(dirname, "src/providers"),
      "@routes": path.resolve(dirname, "src/routes"),
      "@tests": path.resolve(dirname, "tests"),
      "@theme": path.resolve(dirname, "src/theme")
    }
  },
  test: {
    environment: "jsdom",
    globals: true,
    include: ["tests/unit/**/*.test.ts", "tests/unit/**/*.test.tsx"],
    setupFiles: ["./tests/setup/vitest.setup.ts"]
  }
});
