import { useState } from "react";

import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";

import { PasswordInput } from "@components/form-elements/password/PasswordInput";

describe("PasswordInput", () => {
  test("toggles password visibility through the icon button", async () => {
    const user = userEvent.setup();

    render(<PasswordInput label="Password" value="Secret1!" />);

    const input = screen.getByLabelText(/password/i, {
      selector: "input"
    });
    const toggleButton = screen.getByRole("button", {
      name: "Show password"
    });

    expect(input).toHaveAttribute("type", "password");

    await user.click(toggleButton);

    expect(
      screen.getByLabelText(/password/i, {
        selector: "input"
      })
    ).toHaveAttribute("type", "text");
    expect(
      screen.getByRole("button", { name: "Hide password" })
    ).toBeInTheDocument();
  });

  test("updates the visible strength label as the value changes", async () => {
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

    const input = screen.getByLabelText(/password/i, {
      selector: "input"
    });

    expect(screen.getByText("Enter a password")).toBeInTheDocument();

    await user.type(input, "longenough");

    expect(screen.getByText("Weak")).toBeInTheDocument();
  });

  test("raises the first unmet requirement as the field error message", async () => {
    const user = userEvent.setup();

    const PasswordInputHarness = () => {
      const [value, setValue] = useState("");

      return (
        <PasswordInput
          label="Password"
          onChange={event => {
            setValue(event.target.value);
          }}
          required
          value={value}
        />
      );
    };

    render(<PasswordInputHarness />);

    await user.type(
      screen.getByLabelText(/password/i, {
        selector: "input"
      }),
      "abc"
    );

    expect(screen.getByText("At least 8 characters")).toBeInTheDocument();
    expect(
      screen.getByLabelText(/password/i, {
        selector: "input"
      })
    ).toHaveAttribute("aria-invalid", "true");
  });
});
