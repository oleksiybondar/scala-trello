import { useState } from "react";

import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";

import { EmailInput } from "@components/form-elements/email/EmailInput";

describe("EmailInput", () => {
  test("shows an email format error for invalid values", async () => {
    const user = userEvent.setup();

    const EmailInputHarness = () => {
      const [value, setValue] = useState("");

      return (
        <EmailInput
          label="Email"
          onChange={event => {
            setValue(event.target.value);
          }}
          value={value}
        />
      );
    };

    render(<EmailInputHarness />);

    await user.type(screen.getByLabelText(/email/i), "invalid-email");

    expect(screen.getByText("Enter a valid email address")).toBeInTheDocument();
  });

  test("shows the required message after blur when empty", async () => {
    const user = userEvent.setup();

    render(<EmailInput label="Email" required value="" />);

    await user.click(screen.getByLabelText(/email/i));
    await user.tab();

    expect(screen.getByText("Email is required")).toBeInTheDocument();
  });
});
