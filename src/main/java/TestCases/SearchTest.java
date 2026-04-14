package TestCases;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;


public class SearchTest extends BaseTest {


    private static final By SEARCH_ICON = By.cssSelector(
            "a.erc-search-header-button-old, a[href='/search'][aria-label='Search']");

    // Search input: <input aria-label="Search" placeholder="Search..." class="search-input--4gZGM" type="text">
    private static final By SEARCH_INPUT = By.cssSelector(
            "input[class*='search-input'], input[placeholder*='Search'], input[aria-label='Search']");

    // Search result cards / titles
    private static final By RESULT_TITLES = By.cssSelector(
            "[data-t='search-series-card'] .search-show-card__title--kGOEF a, " +
            "[data-t='search-series-card'] .search-show-card-hover__title--9ZRTG a, " +
            "[data-t='search-episode-card'] [data-t='episode-title'] a, " +
            "[data-t='search-episode-card'] [data-t='series-title']");


    private void performSearch(String query) {
        clickElement(SEARCH_ICON);
        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(SEARCH_INPUT));
        input.clear();
        input.sendKeys(query);
        input.sendKeys(Keys.ENTER);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {}
    }

    //Returns true if any search result cards are visible on the page.
    private boolean resultsPresent() {
        try {
            List<WebElement> cards = driver.findElements(By.cssSelector(
                    "[data-t='search-series-card'], [data-t='search-episode-card'], " +
                    "[data-t='single-song-card']"));
            return !cards.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    //Returns true if at least one visible result title contains the given text
    private boolean resultsTitleContains(String text) {
        try {
            List<WebElement> titles = driver.findElements(RESULT_TITLES);
            String lower = text.toLowerCase();
            for (WebElement t : titles) {
                if (t.getText().toLowerCase().contains(lower)) return true;
            }
        } catch (Exception ignored) {}
        return false;
    }

    // Test cases
    @Test(description = "TC-SR-01: Assure search page loads")
    public void testSearchPage() {

        // Click the header search icon to navigate to the search page
        clickElement(SEARCH_ICON);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {}

        String title = driver.getTitle();
        String url   = driver.getCurrentUrl();

        Assert.assertTrue(title.toLowerCase().contains("search") || url.contains("/search"),
                "Expected search page to load. Title: " + title + " | URL: " + url);
        System.out.println("Search page loaded. Title: " + title + " | URL: " + url + "\n");
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ignored) {}
    }


    @Test(description = "TC-SR-02: Empty search cannot crash site or produce error page",
          dependsOnMethods = "testSearchPage")
    public void testEmptySearch() {

        // Already on /search — submit an empty query
        try {
            WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(SEARCH_INPUT));
            input.clear();
            input.sendKeys(Keys.ENTER);
        } catch (Exception ignored) {}

        try {
            Thread.sleep(1500);
        } catch (InterruptedException ignored) {}

        String bodyContent = driver.findElement(By.tagName("body")).getText();
        String title       = driver.getTitle();

        Assert.assertFalse(bodyContent.trim().isEmpty() && title.trim().isEmpty(),
                "Page should not be completely empty on an empty search — possible crash/error page");
        System.out.println("Empty search did not crash the site. Title: " + title + "\n");

        try {
            Thread.sleep(3000);
        } catch (InterruptedException ignored) {}
    }


    @Test(description = "TC-SR-03: Test Search Functionality",
          dependsOnMethods = "testEmptySearch")
    public void testSearch() {

        performSearch("One Piece");

        Assert.assertTrue(resultsPresent(), "Expected search results to be present for 'One Piece'");
        Assert.assertTrue(resultsTitleContains("One Piece"),
                "Expected at least one result title to contain 'One Piece'");
        System.out.println("Search for 'One Piece' returned matching results – test passed.\n");

        try {
            Thread.sleep(5000);
        } catch (InterruptedException ignored) {}
    }


    @Test(description = "TC-SR-04: Test Search by keyword",
          dependsOnMethods = "testSearch")
    public void keywordSearch() {

        performSearch("ball");

        Assert.assertTrue(resultsPresent(), "Expected search results to be present for keyword 'ball'");
        Assert.assertTrue(resultsTitleContains("ball"),
                "Expected at least one result title to contain 'ball' (e.g. Dragon Ball)");
        System.out.println("Keyword search for 'ball' returned matching results – test passed.\n");

        try {
            Thread.sleep(5000);
        } catch (InterruptedException ignored) {}
    }


    @Test(description = "TC-SR-05: Test search by abbreviation (known limitation – " +
                        "Crunchyroll does not support abbreviation search)",
          dependsOnMethods = "keywordSearch")
    public void abbrevSearch() {

        performSearch("jjk");

        boolean jjkFound = resultsTitleContains("jujutsu");

        if (!jjkFound) {
            System.out.println("Abbreviation search 'jjk' did not surface Jujutsu Kaisen – " +
                    "abbreviation search is not supported.\n");
        } else {
            System.out.println("Abbreviation search 'jjk' returned Jujutsu Kaisen – test passed.\n");
        }

        Assert.assertTrue(jjkFound,
                "Abbreviation 'jjk' did not match 'Jujutsu Kaisen' – " +
                "Crunchyroll does not support abbreviation search");

        try {
            Thread.sleep(5000);
        } catch (InterruptedException ignored) {}
    }
}
