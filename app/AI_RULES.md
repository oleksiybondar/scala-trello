# App AI Rules

These rules apply to UI work in the `app` subproject.

## MUI Usage

- Prefer MUI's explicit component APIs over custom CSS when they cover the need.
- Use dedicated MUI props such as `variant`, `color`, `size`, `margin`, and layout props before introducing custom styling.
- Do not use the `sx` prop for routine component styling.

## Custom Styling

- When MUI's built-in API is not sufficient, add component-specific CSS and semantic class names instead of inline styles.
- Use inline styles only when the value is truly dynamic and cannot be expressed cleanly through CSS classes.
