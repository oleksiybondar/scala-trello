import { passwordStrengthConfig } from "@configs/passwordStrengthConfig";
import { evaluatePasswordStrength } from "@helpers/passwordStrength";

describe("evaluatePasswordStrength", () => {
  test("returns the first unmet requirement in policy order", () => {
    const result = evaluatePasswordStrength("short", passwordStrengthConfig);

    expect(result.value).toBe("weak");
    expect(result.firstUnmetRequirement?.key).toBe("length");
    expect(result.firstUnmetRequirement?.label).toBe("At least 8 characters");
  });

  test("advances to the next unmet requirement once earlier rules are satisfied", () => {
    const result = evaluatePasswordStrength("longenough", passwordStrengthConfig);

    expect(result.firstUnmetRequirement?.key).toBe("differentCase");
    expect(result.firstUnmetRequirement?.label).toBe(
      "Includes uppercase and lowercase letters"
    );
  });

  test("marks a fully compliant password as strong with no unmet requirement", () => {
    const result = evaluatePasswordStrength("StrongPass1!", passwordStrengthConfig);

    expect(result.value).toBe("strong");
    expect(result.score).toBe(100);
    expect(result.firstUnmetRequirement).toBeNull();
  });
});
