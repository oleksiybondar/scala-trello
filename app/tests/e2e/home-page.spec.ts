import { expect, test } from "@playwright/test";

import { HomePageObject } from "./pageObjects/HomePageObject";
import { registerMockApiRoutes } from "./support/mockApi";

test.describe("home page", () => {
  test("renders the landing page", async ({ page }) => {
    await registerMockApiRoutes(page);

    const homePage = new HomePageObject(page);

    await homePage.goto();

    await expect(homePage.heading).toBeVisible();
    await expect(homePage.boardsHeading).toBeVisible();
    await expect(homePage.boardCardTitle).toBeVisible();
    await expect(homePage.boardCardDescription).toBeVisible();
    await expect(homePage.ticketSummary).toBeVisible();
    await expect(page.getByRole("link", { name: "Create account" })).toBeVisible();
  });
});
