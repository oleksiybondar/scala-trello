import path from "node:path";
import { fileURLToPath } from "node:url";

import { defineConfig } from "vitest/config";

const dirname = path.dirname(fileURLToPath(import.meta.url));

export default defineConfig({
  resolve: {
    alias: {
      "@app": path.resolve(dirname, "src/app"),
      "@components": path.resolve(dirname, "src/components"),
      "@contexts": path.resolve(dirname, "src/contexts"),
      "@helpers": path.resolve(dirname, "src/helpers"),
      "@hooks": path.resolve(dirname, "src/hooks"),
      "@pages": path.resolve(dirname, "src/pages"),
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
