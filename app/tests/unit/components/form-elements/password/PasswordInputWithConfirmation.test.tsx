import { useState } from "react";

import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";

import { PasswordInputWithConfirmation } from "@components/form-elements/password/PasswordInputWithConfirmation";

describe("PasswordInputWithConfirmation", () => {
  test("shows a mismatch error when the confirmation does not match", async () => {
    const user = userEvent.setup();

    const PasswordInputWithConfirmationHarness = () => {
      const [passwordValue, setPasswordValue] = useState("");
      const [confirmationValue, setConfirmationValue] = useState("");

      return (
        <PasswordInputWithConfirmation
          confirmationValue={confirmationValue}
          onConfirmationChange={event => {
            setConfirmationValue(event.target.value);
          }}
          onPasswordChange={event => {
            setPasswordValue(event.target.value);
          }}
          passwordValue={passwordValue}
          required
        />
      );
    };

    render(<PasswordInputWithConfirmationHarness />);

    const [passwordInput, confirmationInput] = screen.getAllByLabelText(
      /password/i,
      {
        selector: "input"
      }
    );

    await user.type(
      passwordInput as HTMLElement,
      "StrongPass1!"
    );
    await user.type(
      confirmationInput as HTMLElement,
      "StrongPass1"
    );

    expect(screen.getByText("Passwords do not match")).toBeInTheDocument();
  });

  test("reports valid matching passwords with no confirmation error", async () => {
    const user = userEvent.setup();

    const PasswordInputWithConfirmationHarness = () => {
      const [passwordValue, setPasswordValue] = useState("");
      const [confirmationValue, setConfirmationValue] = useState("");

      return (
        <PasswordInputWithConfirmation
          confirmationValue={confirmationValue}
          onConfirmationChange={event => {
            setConfirmationValue(event.target.value);
          }}
          onPasswordChange={event => {
            setPasswordValue(event.target.value);
          }}
          passwordValue={passwordValue}
          required
        />
      );
    };

    render(<PasswordInputWithConfirmationHarness />);

    const [passwordInput, confirmationInput] = screen.getAllByLabelText(
      /password/i,
      {
        selector: "input"
      }
    );

    await user.type(
      passwordInput as HTMLElement,
      "StrongPass1!"
    );
    await user.type(
      confirmationInput as HTMLElement,
      "StrongPass1!"
    );

    expect(screen.queryByText("Passwords do not match")).not.toBeInTheDocument();
    expect(confirmationInput).toHaveAttribute("aria-invalid", "false");
  });
});
