import type { PasswordStrengthConfig } from "@configs/passwordStrengthConfig";

/**
 * Single password rule evaluation result used by the UI.
 */
export interface PasswordRequirementCheck {
  isMet: boolean;
  key: "length" | "differentCase" | "number" | "specialCharacter";
  label: string;
}

/**
 * Aggregated password strength state derived from the configured policy.
 */
export interface PasswordStrengthResult {
  checks: PasswordRequirementCheck[];
  firstUnmetRequirement: PasswordRequirementCheck | null;
  metRequirements: number;
  score: number;
  totalRequirements: number;
  value: "empty" | "weak" | "fair" | "good" | "strong";
}

/**
 * Returns whether the password contains at least one lowercase letter.
 *
 * @param value Password value being evaluated.
 * @returns `true` when the password includes a lowercase letter.
 */
const hasLowercaseCharacter = (value: string): boolean => {
  return /[a-z]/.test(value);
};

/**
 * Returns whether the password contains at least one uppercase letter.
 *
 * @param value Password value being evaluated.
 * @returns `true` when the password includes an uppercase letter.
 */
const hasUppercaseCharacter = (value: string): boolean => {
  return /[A-Z]/.test(value);
};

/**
 * Returns whether the password contains at least one numeric digit.
 *
 * @param value Password value being evaluated.
 * @returns `true` when the password includes a numeric digit.
 */
const hasNumberCharacter = (value: string): boolean => {
  return /\d/.test(value);
};

/**
 * Returns whether the password contains at least one special character.
 *
 * @param value Password value being evaluated.
 * @returns `true` when the password includes a special character.
 */
const hasSpecialCharacter = (value: string): boolean => {
  return /[^A-Za-z0-9]/.test(value);
};

/**
 * Evaluates a password against the configured policy and returns UI-ready strength data.
 *
 * @param value Password value being evaluated.
 * @param config Password strength rules applied by the UI.
 * @returns Aggregated password strength data for rendering and validation.
 */
export const evaluatePasswordStrength = (
  value: string,
  config: PasswordStrengthConfig
): PasswordStrengthResult => {
  const candidateChecks = [
    {
      isMet: value.length >= config.length,
      key: "length",
      label: `At least ${String(config.length)} characters`
    },
    {
      isMet:
        !config.requireDifferentCase ||
        (hasLowercaseCharacter(value) && hasUppercaseCharacter(value)),
      key: "differentCase",
      label: "Includes uppercase and lowercase letters"
    },
    {
      isMet: !config.requireNumber || hasNumberCharacter(value),
      key: "number",
      label: "Includes at least one number"
    },
    {
      isMet: !config.requireSpecialCharacter || hasSpecialCharacter(value),
      key: "specialCharacter",
      label: "Includes at least one special character"
    }
  ] satisfies PasswordRequirementCheck[];

  const checks = candidateChecks.filter(check => {
    if (check.key === "differentCase") {
      return config.requireDifferentCase;
    }

    if (check.key === "number") {
      return config.requireNumber;
    }

    if (check.key === "specialCharacter") {
      return config.requireSpecialCharacter;
    }

    return true;
  });

  const metRequirements = checks.filter(check => check.isMet).length;
  const firstUnmetRequirement = checks.find(check => !check.isMet) ?? null;
  const totalRequirements = checks.length;
  const score =
    totalRequirements === 0
      ? 0
      : Math.round((metRequirements / totalRequirements) * 100);

  const result: PasswordStrengthResult = {
    checks,
    firstUnmetRequirement,
    metRequirements,
    score: score,
    totalRequirements,
    value: "empty"
  };

  if (value.length === 0) {
    result.score = 0;
    result.value = "empty";
    return result;
  }

  if (metRequirements <= 1) {
    result.value = "weak";
    return result;
  }

  if (metRequirements === 2) {
    result.value = "fair";
    return result;
  }

  if (metRequirements < totalRequirements) {
    result.value = "good";
    return result;
  }

  result.score = 100;
  result.value = "strong";

  return result;
};
