package TestCases;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

public class NavigationTest extends BaseTest {

    // Locators

    // Crunchyroll logo link
    private static final By LOGO = By.cssSelector(
            "a.erc-logo, a[href='/discover']");

    // Watchlist icon in the header
    private static final By WATCHLIST_ICON = By.cssSelector(
            "a[aria-label='Watchlist'], a[href='/watchlist'].erc-watchlist-header-button-old");

    // Crunchylists tab on the watchlist page
    private static final By CRUNCHYLISTS_TAB = By.cssSelector(
            "a[href='/crunchylists'][role='tab']");

    // History tab on the watchlist page
    private static final By HISTORY_TAB = By.cssSelector(
            "a[href='/history'][role='tab']");

    // Categories dropdown button in the header
    private static final By CATEGORIES_BTN = By.cssSelector(
            "[data-t='menu-browse']");

    // Genre links inside the open Categories dropdown
    private static final By GENRE_LINKS = By.cssSelector(
            "[data-t='browse-submenu-item'] a");

    // Search icon/link in the header
    private static final By SEARCH_ICON = By.cssSelector(
            "a[aria-label='Search'][href*='/search'], [data-t='search-svg']");


    // ── Tests ─────────────────────────────────────────────────────────────────

    @Test(description = "TC-NAV-01: Assure Crunchyroll logo returns user to homepage")
    public void testLogoNav() {

        // Click the logo
        clickElement(LOGO);

        wait.until(ExpectedConditions.urlContains("/discover"));
        Assert.assertTrue(driver.getCurrentUrl().contains("/discover"),
                "Clicking the logo should navigate back to the homepage (/discover)");

        System.out.println("Logo navigation worked. URL: " + driver.getCurrentUrl());
    }


    @Test(description = "TC-NAV-02: Assure there is an easy navigation bar with multiple links",
          dependsOnMethods = "testLogoNav")
    public void navLinks() {

        List<WebElement> links = driver.findElements(By.cssSelector("header a, nav a"));

        Assert.assertTrue(links.size() > 0, "Navigation bar should contain links");

        System.out.println("Navigation bar has " + links.size() + " links:");
        for (WebElement link : links) {
            String text = link.getText();
            String href = link.getAttribute("href");
            if (text != null && !text.isBlank()) {
                System.out.println("  - \"" + text + "\" -> " + href);
            }
        }
    }


    @Test(description = "TC-NAV-03: Assure easy navigation to watchlists",
          dependsOnMethods = "navLinks")
    public void watchlistNav() {

        // Click the watchlist icon in the header
        clickElement(WATCHLIST_ICON);
        wait.until(ExpectedConditions.urlContains("/watchlist"));
        Assert.assertTrue(driver.getCurrentUrl().contains("/watchlist"),
                "Clicking watchlist icon should navigate to the watchlist page");

        // Click the Crunchylists tab
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {}
        clickElement(CRUNCHYLISTS_TAB);
        wait.until(ExpectedConditions.urlContains("crunchylist"));
        Assert.assertTrue(driver.getCurrentUrl().contains("crunchylist"),
                "Clicking Crunchylists tab should navigate to /crunchylists");

        // Click the History tab
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {}
        clickElement(HISTORY_TAB);
        wait.until(ExpectedConditions.urlContains("/history"));
        Assert.assertTrue(driver.getCurrentUrl().contains("history"),
                "Clicking History tab should navigate to /history");

        System.out.println("Watchlist, Crunchylist, and History nav links all work.");
    }


    @Test(description = "TC-NAV-04: Assure there is a category/genre filter/easy navigation",
          dependsOnMethods = "watchlistNav")
    public void navByCategory() {

        // Open the Categories dropdown
        clickElement(CATEGORIES_BTN);
        try {
            Thread.sleep(800);
        } catch (InterruptedException ignored) {}

        // Wait for and collect genre links inside the open dropdown
        List<WebElement> genreLinks = wait.until(
                ExpectedConditions.presenceOfAllElementsLocatedBy(GENRE_LINKS));

        Assert.assertTrue(genreLinks.size() > 0,
                "Categories dropdown should contain at least one genre link");

        System.out.println("Available genre links (" + genreLinks.size() + "):");
        for (WebElement genre : genreLinks) {
            String text = genre.getText();
            if (text != null && !text.isBlank()) System.out.println("  - " + text);
        }
    }


    @Test(description = "TC-NAV-05: Assure nav has a search icon/bar",
          dependsOnMethods = "navByCategory")
    public void searchNav() {

        boolean searchPresent =
                isElementPresent(SEARCH_ICON)                                                   ||
                isElementPresent(By.cssSelector("input[type='search']"))                        ||
                isElementPresent(By.cssSelector("[class*='search-input'], [class*='search-bar']"));

        Assert.assertTrue(searchPresent,
                "A search icon or search bar should be present in the nav bar");
        System.out.println("Search icon/bar found in nav - test passed.");
    }
}
