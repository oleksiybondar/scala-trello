import { screen } from "@testing-library/react";

import { HomePage } from "@pages/HomePage";
import { renderApp } from "@tests/setup/render";

describe("HomePage", () => {
  test("renders the bootstrap heading, navbar, and login entry point", () => {
    renderApp(<HomePage />);

    expect(
      screen.getByRole("heading", { name: "Theme system is in place." })
    ).toBeInTheDocument();
    expect(screen.getByRole("link", { name: "Intro Into Scala App" })).toBeInTheDocument();
    expect(screen.getByRole("link", { name: "Login" })).toBeInTheDocument();
    expect(
      screen.getByRole("heading", { name: "Current theme state" })
    ).toBeInTheDocument();
    expect(screen.getByText("Source: default")).toBeInTheDocument();
  });
});
