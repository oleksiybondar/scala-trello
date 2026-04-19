import { screen } from "@testing-library/react";

import { MyTimeRegistrationPage } from "@pages/MyTimeRegistrationPage";
import { renderApp } from "@tests/setup/render";

describe("MyTimeRegistrationPage", () => {
  test("renders the my time registration base tools", () => {
    renderApp(<MyTimeRegistrationPage />);

    expect(screen.getByRole("heading", { name: "My time registration" })).toBeInTheDocument();
    expect(screen.getByLabelText("Search entries")).toBeInTheDocument();
    expect(screen.getByLabelText("Activity")).toBeInTheDocument();
    expect(screen.getByText(/^\d+\/\d+ entries$/)).toBeInTheDocument();
  });
});
