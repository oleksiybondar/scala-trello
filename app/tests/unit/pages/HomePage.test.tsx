import { screen } from "@testing-library/react";

import { HomePage } from "@pages/HomePage";
import { renderApp } from "@tests/setup/render";

describe("HomePage", () => {
  test("renders the landing page sections and auth entry points", () => {
    renderApp(<HomePage />);

    expect(
      screen.getByRole("heading", {
        name: "Track sprint work without enterprise bloat."
      })
    ).toBeInTheDocument();
    expect(
      screen.getAllByRole("img", { name: "Boards" })
    ).toHaveLength(1);
    expect(screen.getByRole("link", { name: "Sign in" })).toBeInTheDocument();
    expect(
      screen.getByRole("heading", {
        name: "Tickets move through a delivery-focused lifecycle."
      })
    ).toBeInTheDocument();
    expect(
      screen.getByRole("heading", {
        name: "Tickets carry effort tracking and discussion with them."
      })
    ).toBeInTheDocument();
    expect(screen.getByRole("heading", { name: "Platform Sprint" })).toBeInTheDocument();
    expect(
      screen.getByText(
        "Each ticket keeps its own estimate, logged work, and discussion, so the board shows both delivery state and effort visibility in one place."
      )
    ).toBeInTheDocument();
  });
});
