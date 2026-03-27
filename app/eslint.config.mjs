import js from "@eslint/js";
import globals from "globals";
import tseslint from "typescript-eslint";

const tsFiles = ["src/**/*.ts", "src/**/*.tsx"];

const withTypedTsScope = config => ({
  ...config,
  files: tsFiles,
  languageOptions: {
    ...config.languageOptions,
    ecmaVersion: "latest",
    sourceType: "module",
    globals: {
      ...globals.browser,
      ...globals.es2024,
      ...config.languageOptions?.globals
    },
    parserOptions: {
      ...config.languageOptions?.parserOptions,
      projectService: true,
      tsconfigRootDir: import.meta.dirname
    }
  }
});

export default [
  {
    ignores: ["dist/**", "coverage/**", "node_modules/**"]
  },
  {
    ...js.configs.recommended,
    files: ["**/*.js", "**/*.mjs", "**/*.cjs"],
    languageOptions: {
      ...js.configs.recommended.languageOptions,
      ecmaVersion: "latest",
      sourceType: "module",
      globals: {
        ...globals.node,
        ...globals.es2024
      }
    }
  },
  ...tseslint.configs.strictTypeChecked.map(withTypedTsScope),
  ...tseslint.configs.stylisticTypeChecked.map(withTypedTsScope),
  {
    files: tsFiles,
    languageOptions: {
      ecmaVersion: "latest",
      sourceType: "module",
      globals: {
        ...globals.browser,
        ...globals.es2024
      },
      parserOptions: {
        projectService: true,
        tsconfigRootDir: import.meta.dirname
      }
    },
    rules: {
      "@typescript-eslint/consistent-type-imports": [
        "error",
        {
          prefer: "type-imports",
          fixStyle: "separate-type-imports"
        }
      ],
      "@typescript-eslint/explicit-function-return-type": [
        "error",
        {
          allowExpressions: true,
          allowTypedFunctionExpressions: true
        }
      ],
      "@typescript-eslint/no-explicit-any": "error",
      "@typescript-eslint/no-floating-promises": "error",
      "@typescript-eslint/no-inferrable-types": "off",
      "@typescript-eslint/no-misused-promises": [
        "error",
        {
          checksVoidReturn: {
            attributes: false
          }
        }
      ],
      "@typescript-eslint/no-unused-vars": [
        "error",
        {
          argsIgnorePattern: "^_",
          caughtErrorsIgnorePattern: "^_",
          destructuredArrayIgnorePattern: "^_",
          varsIgnorePattern: "^_"
        }
      ],
      "no-console": [
        "error",
        {
          allow: ["warn", "error"]
        }
      ]
    }
  }
];
