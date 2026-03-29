import { screen } from "@testing-library/react";

import { HomePage } from "@pages/HomePage";
import { renderApp } from "@tests/setup/render";

describe("HomePage", () => {
  test("renders the bootstrap heading and theme widget", () => {
    renderApp(<HomePage />);

    expect(
      screen.getByRole("heading", { name: "Theme system is in place." })
    ).toBeInTheDocument();
    expect(
      screen.getByRole("heading", { name: "Theme widget" })
    ).toBeInTheDocument();
    expect(screen.getByText("Source: default")).toBeInTheDocument();
  });
});
