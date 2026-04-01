import { render, screen } from "@testing-library/react";

import { PasswordStrengthBar } from "@components/forms/form-elements/PasswordStrengthBar";
import { passwordStrengthConfig } from "@configs/passwordStrengthConfig";
import { evaluatePasswordStrength } from "@helpers/passwordStrength";

describe("PasswordStrengthBar", () => {
  test("renders the progress state and the first unmet requirement hint", () => {
    const strength = evaluatePasswordStrength("longenough1", passwordStrengthConfig);

    render(<PasswordStrengthBar strength={strength} />);

    expect(screen.getByText("Fair")).toBeInTheDocument();
    expect(
      screen.getByText("Includes uppercase and lowercase letters")
    ).toBeInTheDocument();
    expect(
      screen.getByRole("progressbar", { name: "Password strength" })
    ).toHaveAttribute("aria-valuenow", String(strength.score));
  });

  test("hides the hint when all password requirements are satisfied", () => {
    const strength = evaluatePasswordStrength("StrongPass1!", passwordStrengthConfig);

    render(<PasswordStrengthBar strength={strength} />);

    expect(screen.getByText("Strong")).toBeInTheDocument();
    expect(
      screen.queryByText("Includes at least one special character")
    ).not.toBeInTheDocument();
    expect(
      screen.queryByText("All password requirements are satisfied")
    ).not.toBeInTheDocument();
  });
});
