import type { Locator, Page } from "@playwright/test";

export class HomePageObject {
  readonly heading: Locator;
  readonly themeWidgetHeading: Locator;

  public constructor(private readonly page: Page) {
    this.heading = page.getByRole("heading", {
      name: "Theme system is in place."
    });
    this.themeWidgetHeading = page.getByRole("heading", {
      name: "Theme widget"
    });
  }

  public async goto(): Promise<void> {
    await this.page.goto("/");
  }
}
