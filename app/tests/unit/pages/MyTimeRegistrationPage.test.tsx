import { screen } from "@testing-library/react";

import { MyTimeRegistrationPage } from "@pages/MyTimeRegistrationPage";
import { renderApp } from "@tests/setup/render";

describe("MyTimeRegistrationPage", () => {
  test("renders the my time registration stub content", () => {
    renderApp(<MyTimeRegistrationPage />);

    expect(screen.getByRole("heading", { name: "My time registration" })).toBeInTheDocument();
    expect(
      screen.getByText("This page is a stub. Personal time registration tools are coming next.")
    ).toBeInTheDocument();
  });
});
