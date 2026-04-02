import { render, screen } from "@testing-library/react";

import { PasswordStrengthBar } from "@components/form-elements/password/PasswordStrengthBar";
import { passwordStrengthConfig } from "@configs/passwordStrengthConfig";
import { evaluatePasswordStrength } from "@helpers/passwordStrength";

describe("PasswordStrengthBar", () => {
  test("renders the progress state and label", () => {
    const strength = evaluatePasswordStrength("longenough1", passwordStrengthConfig);

    render(<PasswordStrengthBar strength={strength} />);

    expect(screen.getByText("Fair")).toBeInTheDocument();
    expect(
      screen.getByRole("progressbar", { name: "Password strength" })
    ).toHaveAttribute("aria-valuenow", String(strength.score));
  });

  test("does not render password guidance text", () => {
    const strength = evaluatePasswordStrength("StrongPass1!", passwordStrengthConfig);

    render(<PasswordStrengthBar strength={strength} />);

    expect(screen.getByText("Strong")).toBeInTheDocument();
    expect(
      screen.queryByText("Includes uppercase and lowercase letters")
    ).not.toBeInTheDocument();
  });
});
