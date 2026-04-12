package TestCases;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

public class SearchTest extends BaseTest {

    private static final String SEARCH_URL = BASE_URL + "/search";


    // Navigates to the search page, types the query, and submits.
    private void performSearch(String query) {
        driver.get(SEARCH_URL);
        WebElement searchBox = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("input[type='search'], input[name='q'], input[placeholder*='Search']")));
        searchBox.clear();
        searchBox.sendKeys(query);
        // Use ENTER key – the search input is not inside a <form> element so .submit() throws
        searchBox.sendKeys(Keys.ENTER);
        try { Thread.sleep(1500); } catch (InterruptedException ignored) { }
    }

    // Returns true if any result cards are visible on the current page.
    private boolean resultsPresent() {
        return isElementPresent(By.cssSelector("[class*='card']"))   ||
               isElementPresent(By.cssSelector("[class*='result']")) ||
               isElementPresent(By.cssSelector("[class*='item']"))   ||
               isElementPresent(By.cssSelector("article"));
    }

    // Returns true if at least one result title contains the keyword (case-insensitive).
    private boolean resultsTitleContains(String keyword) {
        List<WebElement> titles = driver.findElements(By.cssSelector(
                "[class*='title'], [class*='card'] h4, [class*='card'] h3, " +
                "[class*='result'] [class*='name'], [data-t*='title']"));
        for (WebElement title : titles) {
            try {
                if (title.getText().toLowerCase().contains(keyword.toLowerCase())) return true;
            } catch (NoSuchElementException ignored) { }
        }
        return false;
    }


    @Test(description = "TC-SR-01: Assure search page loads")
    public void testSearchPage() {
        driver.get(SEARCH_URL);
        String title = driver.getTitle();
        String url   = driver.getCurrentUrl();

        Assert.assertTrue(title.toLowerCase().contains("search") || url.contains("/search"),
                "Expected search page to load. Title: " + title + " | URL: " + url);
        System.out.println("Search page loaded. Title: " + title + " | URL: " + url);
    }


    @Test(description = "TC-SR-02: Empty search cannot crash site or produce error page")
    public void testEmptySearch() {
        driver.get(SEARCH_URL);
        try { Thread.sleep(1500); } catch (InterruptedException ignored) { }

        String bodyContent = driver.findElement(By.tagName("body")).getText();
        String title       = driver.getTitle();

        Assert.assertFalse(bodyContent.trim().isEmpty() && title.trim().isEmpty(),
                "Page should not be completely empty on an empty search — possible crash/error page");
        System.out.println("Empty search did not crash the site. Title: " + title);
    }


    @Test(description = "TC-SR-03: Test Search Functionality")
    public void testSearch() {
        performSearch("One Piece");

        Assert.assertTrue(resultsPresent(), "Expected search results to be present for 'One Piece'");
        Assert.assertTrue(resultsTitleContains("One Piece"),
                "Expected at least one result title to contain 'One Piece'");
        System.out.println("Search for 'One Piece' returned matching results – test passed.");
    }


    @Test(description = "TC-SR-04: Test Search by keyword")
    public void keywordSearch() {
        performSearch("ball");

        Assert.assertTrue(resultsPresent(), "Expected search results to be present for keyword 'ball'");
        Assert.assertTrue(resultsTitleContains("ball"),
                "Expected at least one result title to contain 'ball' (e.g. Dragon Ball)");
        System.out.println("Keyword search for 'ball' returned matching results – test passed.");
    }


    @Test(description = "TC-SR-05: Test search by abbreviation (known limitation – " +
                        "Crunchyroll does not support abbreviation search)")
    public void abbrevSearch() {
        performSearch("jjk");

        boolean jjkFound = resultsTitleContains("jujutsu");

        if (!jjkFound) {
            System.out.println("Abbreviation search 'jjk' did not surface Jujutsu Kaisen – " +
                    "known Crunchyroll limitation: abbreviation search is not supported.");
        } else {
            System.out.println("Abbreviation search 'jjk' returned Jujutsu Kaisen – test passed.");
        }

        Assert.assertTrue(jjkFound,
                "Abbreviation 'jjk' did not match 'Jujutsu Kaisen' – " +
                "Crunchyroll does not support abbreviation search");
    }
}
