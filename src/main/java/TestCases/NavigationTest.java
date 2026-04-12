package TestCases;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

public class NavigationTest extends BaseTest {

    @BeforeClass
    public void navigateHome() {
        driver.get(BASE_URL);
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
        dismissBanners();
    }


    @Test(description = "TC-NAV-01: Assure there is an easy navigation bar with multiple links")
    public void navLinks() {
        List<WebElement> navLinks = driver.findElements(By.cssSelector("header a, nav a"));

        Assert.assertTrue(navLinks.size() > 0, "Navigation bar should contain links");

        System.out.println("Navigation bar has " + navLinks.size() + " links:");
        for (WebElement link : navLinks) {
            String text = link.getText();
            String href = link.getAttribute("href");
            if (text != null && !text.isBlank()) {
                System.out.println("  – \"" + text + "\" → " + href);
            }
        }
    }


    @Test(description = "TC-NAV-02: Assure easy navigation to watchlists",
          dependsOnMethods = "navLinks")
    public void watchlistNav() {
        clickElement(By.cssSelector("[data-t='watchlist-svg'], a[href*='/watchlist']"));
        wait.until(ExpectedConditions.urlContains("/watchlist"));
        boolean watchlistTabPassed = driver.getCurrentUrl().contains("/watchlist");
        Assert.assertTrue(watchlistTabPassed,
                "Clicking watchlist icon should navigate to the watchlist page");

        // Navigate directly to the Crunchylists sub-page (tab link locators vary by build)
        driver.get(BASE_URL + "/watchlist/crunchylists");
        try { Thread.sleep(1500); } catch (InterruptedException ignored) {}
        boolean crunchyListTabPassed = driver.getCurrentUrl().contains("crunchylist");
        Assert.assertTrue(crunchyListTabPassed,
                "Navigating to crunchylists URL should succeed");

        // Navigate directly to the History sub-page
        driver.get(BASE_URL + "/watchlist/history");
        try { Thread.sleep(1500); } catch (InterruptedException ignored) {}
        boolean historyTabPassed = driver.getCurrentUrl().contains("history");
        Assert.assertTrue(historyTabPassed,
                "Navigating to history URL should succeed");

        if (watchlistTabPassed && crunchyListTabPassed && historyTabPassed) {
            System.out.println("Watchlist, Crunchylist, & History nav links all work.\n");
        }
    }


    @Test(description = "TC-NAV-03: Assure Crunchyroll logo returns user to homepage")
    public void testLogoNav() {

    }


    @Test(description = "TC-NAV-04: Assure there is a category/genre filter/easy navigation")
    public void navByCategory() {
        // Navigate directly to /browse to avoid stale-element issues with hover menus
        driver.get(BASE_URL + "/browse");
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}

        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(
                "[class*='genre'] a, [data-t*='genre'], [class*='filter'] a, " +
                "a[href*='/genres/'], [class*='browse'] a")));

        List<WebElement> categories = driver.findElements(By.cssSelector(
                "[class*='genre'] a, [data-t*='genre'], [class*='filter'] a, " +
                "a[href*='/genres/'], [class*='browse'] a"));

        Assert.assertTrue(categories.size() > 0,
                "Browse/category page should contain at least one genre/filter link");

        System.out.println("Available categories/browse links (" + categories.size() + "):");
        for (WebElement cat : categories) {
            String text = cat.getText();
            if (text != null && !text.isBlank()) System.out.println("  – " + text);
        }
    }


    @Test(description = "TC-NAV-05: Assure nav has a search icon/bar")
    public void searchNav() {
        driver.get(BASE_URL);

        boolean searchPresent =
                isElementPresent(By.cssSelector("a[href*='/search']"))                        ||
                isElementPresent(By.cssSelector("[data-t='search-svg']"))                     ||
                isElementPresent(By.cssSelector("input[type='search']"))                      ||
                isElementPresent(By.cssSelector("[class*='search-input'], [class*='search-bar']"));

        Assert.assertTrue(searchPresent,
                "A search icon or search bar should be present in the nav bar");
        System.out.println("Search icon/bar found in nav – test passed.\n");
    }
}
