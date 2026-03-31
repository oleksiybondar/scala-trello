import { expect, test } from "@playwright/test";

import { HomePageObject } from "./pageObjects/HomePageObject";
import { registerMockApiRoutes } from "./support/mockApi";

test.describe("home page", () => {
  test("renders the bootstrap shell", async ({ page }) => {
    await registerMockApiRoutes(page);

    const homePage = new HomePageObject(page);

    await homePage.goto();

    await expect(homePage.heading).toBeVisible();
    await expect(homePage.themeWidgetHeading).toBeVisible();
    await expect(page.getByText("Source: default")).toBeVisible();
  });
});
