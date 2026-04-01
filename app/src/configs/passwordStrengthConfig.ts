/**
 * Frontend password policy used for strength feedback and early validation.
 */
export interface PasswordStrengthConfig {
  /**
   * Minimum required password length.
   */
  length: number;
  /**
   * Whether both uppercase and lowercase letters are required.
   */
  requireDifferentCase: boolean;
  /**
   * Whether at least one numeric digit is required.
   */
  requireNumber: boolean;
  /**
   * Whether at least one non-alphanumeric character is required.
   */
  requireSpecialCharacter: boolean;
}

/**
 * Default password policy mirrored by password-related UI components.
 */
export const passwordStrengthConfig: PasswordStrengthConfig = {
  length: 8,
  requireDifferentCase: true,
  requireNumber: true,
  requireSpecialCharacter: true
};
