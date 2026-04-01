/**
 * Returns whether the given value matches a lightweight email format check.
 *
 * @param value Raw input value.
 * @returns `true` when the value looks like an email address.
 */
export const isEmailAddress = (value: string): boolean => {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/u.test(value.trim());
};
