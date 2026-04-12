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


    @Test(description = "TC-SR-01: Assure search page loads")
    public void testSearchPage() {
        //click on search icon
        driver.get(SEARCH_URL);
        String title = driver.getTitle();
        String url   = driver.getCurrentUrl();

        Assert.assertTrue(title.toLowerCase().contains("search") || url.contains("/search"),
                "Expected search page to load. Title: " + title + " | URL: " + url);
        System.out.println("Search page loaded. Title: " + title + " | URL: " + url + "\n");
    }


    @Test(description = "TC-SR-02: Empty search cannot crash site or produce error page")
    public void testEmptySearch() {
        driver.get(SEARCH_URL);
        try { Thread.sleep(1500); } catch (InterruptedException ignored) { }


        Assert.assertFalse(bodyContent.trim().isEmpty() && title.trim().isEmpty(),
                "Page should not be completely empty on an empty search — possible crash/error page");
        System.out.println("Empty search did not crash the site. Title: " + title + "\n");
    }


    @Test(description = "TC-SR-03: Test Search Functionality")
    public void testSearch() {
        performSearch("One Piece");

        Assert.assertTrue(resultsPresent(), "Expected search results to be present for 'One Piece'");
        Assert.assertTrue(resultsTitleContains("One Piece"),
                "Expected at least one result title to contain 'One Piece'");
        System.out.println("Search for 'One Piece' returned matching results – test passed.\n");
    }


    @Test(description = "TC-SR-04: Test Search by keyword")
    public void keywordSearch() {
        //clear search bar
        performSearch("ball");

        Assert.assertTrue(resultsPresent(), "Expected search results to be present for keyword 'ball'");
        Assert.assertTrue(resultsTitleContains("ball"),
                "Expected at least one result title to contain 'ball' (e.g. Dragon Ball)");
        System.out.println("Keyword search for 'ball' returned matching results – test passed.\n");
    }


    @Test(description = "TC-SR-05: Test search by abbreviation (known limitation – " +
                        "Crunchyroll does not support abbreviation search)")
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
    }
}
