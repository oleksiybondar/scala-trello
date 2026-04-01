import { useState } from "react";

import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";

import { PasswordInput } from "@components/forms/form-elements/PasswordInput";

describe("PasswordInput", () => {
  test("toggles password visibility through the icon button", async () => {
    const user = userEvent.setup();

    render(<PasswordInput label="Password" value="Secret1!" />);

    const input = screen.getByLabelText("Password");
    const toggleButton = screen.getByRole("button", {
      name: "Show password"
    });

    expect(input).toHaveAttribute("type", "password");

    await user.click(toggleButton);

    expect(screen.getByLabelText("Password")).toHaveAttribute("type", "text");
    expect(
      screen.getByRole("button", { name: "Hide password" })
    ).toBeInTheDocument();
  });

  test("updates the visible strength hint as the value changes", async () => {
    const user = userEvent.setup();

    const PasswordInputHarness = () => {
      const [value, setValue] = useState("");

      return (
        <PasswordInput
          label="Password"
          onChange={event => {
            setValue(event.target.value);
          }}
          value={value}
        />
      );
    };

    render(<PasswordInputHarness />);

    const input = screen.getByLabelText("Password");

    expect(screen.getByText("At least 8 characters")).toBeInTheDocument();

    await user.type(input, "longenough");

    expect(
      screen.getByText("Includes uppercase and lowercase letters")
    ).toBeInTheDocument();
  });
});
