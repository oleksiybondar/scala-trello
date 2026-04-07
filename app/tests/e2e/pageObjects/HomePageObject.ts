import type { Locator, Page } from "@playwright/test";

export class HomePageObject {
  readonly heading: Locator;
  readonly boardsHeading: Locator;
  readonly boardCardTitle: Locator;
  readonly boardCardDescription: Locator;
  readonly ticketSummary: Locator;

  public constructor(private readonly page: Page) {
    this.heading = page.getByRole("heading", {
      name: "Track sprint work without enterprise bloat."
    });
    this.boardsHeading = page.getByRole("heading", {
      name: "Tickets move through a delivery-focused lifecycle."
    });
    this.boardCardTitle = page.getByText("Backend API sprint");
    this.boardCardDescription = page.getByText(
      "A board groups the sprint scope, its members, and the tickets moving through implementation, review, and testing."
    );
    this.ticketSummary = page.getByText(
      "Each ticket keeps its own estimate, logged work, and discussion, so the board shows both delivery state and effort visibility in one place."
    );
  }

  public async goto(): Promise<void> {
    await this.page.goto("/");
  }
}
