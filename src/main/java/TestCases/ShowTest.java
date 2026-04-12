package TestCases;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.List;

public class ShowTest extends BaseTest {

    // One Piece series page — stable Crunchyroll series ID
    private static final String ONE_PIECE_URL = BASE_URL + "/series/GRMG8ZQZR/one-piece";

    @BeforeClass
    public void navigateSeries() {
        driver.get(ONE_PIECE_URL);
        // Wait for URL and for some page content – SPA needs extra time to render
        wait.until(ExpectedConditions.urlContains("one-piece"));
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
        dismissCookieConsent();
    }

    @Test(description = "TC-VP-01: Verify series info properly displays")
    public void seriesInfo() {
        // Explicit wait for the SPA to render the series title before asserting
        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("h1, [class*='series-title'], [data-t*='title']")));

        boolean titlePresent =
                isElementPresent(By.cssSelector("h1, [class*='series-title'], [data-t*='title']"));
        boolean descPresent =
                isElementPresent(By.cssSelector("[class*='description'], [class*='synopsis'], [data-t*='description']"));
        boolean metaPresent =
                isElementPresent(By.cssSelector("[class*='genre'], [class*='rating'], [class*='metadata'], [data-t*='genre']"));

        Assert.assertTrue(titlePresent, "Series title should be displayed");
        Assert.assertTrue(descPresent,  "Series description/synopsis should be displayed");
        Assert.assertTrue(metaPresent,  "Series metadata (genre/rating) should be displayed");

        // Use the same broad selector used in the assertion above to avoid NoSuchElementException
        String title = driver.findElement(
                By.cssSelector("h1, [class*='series-title'], [data-t*='title']")).getText();
        System.out.println("Series info verified. Title: " + title);
    }

    @Test(description = "TC-VP-02: Verify episode details display correctly",
          dependsOnMethods = "seriesInfo")
    public void episodeDetails() {
        List<WebElement> episodes = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                By.cssSelector("[class*='episode-card'], [data-t*='episode'], [class*='episode-item']")));

        Assert.assertTrue(episodes.size() > 0, "Episodes should be present on the series page");

        WebElement first = episodes.get(0);
        boolean hasTitle   = !first.findElements(By.cssSelector("[class*='title'], h4, h3")).isEmpty();
        boolean hasDesc    = !first.findElements(By.cssSelector("[class*='description'], [class*='synopsis']")).isEmpty();
        boolean hasAirDate = !first.findElements(By.cssSelector("[class*='date'], [class*='air'], time")).isEmpty();

        Assert.assertTrue(hasTitle,   "Episode should display a title");
        Assert.assertTrue(hasDesc,    "Episode should display a description");
        Assert.assertTrue(hasAirDate, "Episode should display an air date");

        System.out.println("Episode details verified. Total episodes visible: " + episodes.size());
    }

    @Test(description = "TC-VP-03: Verify drop down menu for seasons",
          dependsOnMethods = "episodeDetails")
    public void seasonsMenu() {
        WebElement seasonDropdown = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("select[class*='season'], [data-t*='season-dropdown'], " +
                               "[class*='season-select'], [aria-label*='season']")));

        Assert.assertTrue(seasonDropdown.isDisplayed(), "Season dropdown should be visible");

        // If it's a native <select>, use Select; otherwise click to open
        if (seasonDropdown.getTagName().equals("select")) {
            Select dropdown = new Select(seasonDropdown);
            Assert.assertTrue(dropdown.getOptions().size() > 1,
                    "Season dropdown should contain more than one season");
            String firstSeason = dropdown.getFirstSelectedOption().getText();
            // Switch to a different season
            for (WebElement opt : dropdown.getOptions()) {
                if (!opt.getText().equals(firstSeason)) {
                    dropdown.selectByVisibleText(opt.getText());
                    break;
                }
            }
            System.out.println("Season dropdown functional. Seasons available: " + dropdown.getOptions().size());
        } else {
            seasonDropdown.click();
            List<WebElement> options = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                    By.cssSelector("[class*='season-option'], [data-t*='season-item'], [role='option']")));
            Assert.assertTrue(options.size() > 1, "Season dropdown should contain more than one season");
            options.get(options.size() - 1).click(); // select last season
            System.out.println("Season dropdown functional. Seasons available: " + options.size());
        }

        // Confirm episodes reloaded
        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("[class*='episode-card'], [data-t*='episode']")));
        System.out.println("Season switch successful – test passed.");
    }

    @Test(description = "TC-VP-04: Test playback on random episode")
    public void testPlayback() {
        driver.get(ONE_PIECE_URL);
        dismissCookieConsent();
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                By.cssSelector("[class*='episode-card'], [data-t*='episode']")));

        // Find episode 146 – use presenceOfElementLocated (not elementToBeClickable)
        // to get the element first, then scroll into view before clicking
        WebElement ep146 = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//*[contains(@class,'episode') and (.//text()[contains(.,'146')] or @data-episode-number='146')]//a" +
                         " | //a[contains(@href,'episode-146')]")));
        scrollToElement(ep146);
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}
        // Use JS click to bypass any overlay (e.g. OneTrust banner) that may intercept
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", ep146);

        // Extended wait for the video player (30 s – streaming player takes longer to load)
        WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(30));
        longWait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("video, [class*='video-player'], [data-t*='player']")));

        // Seek to 9 minutes (540 seconds) and play
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("var v = document.querySelector('video'); if(v){ v.currentTime = 540; v.play(); }");
        try { Thread.sleep(10000); } catch (InterruptedException ignored) {}

        double currentTime = ((Number) js.executeScript(
                "var v = document.querySelector('video'); return v ? v.currentTime : 0;")).doubleValue();

        Assert.assertTrue(currentTime > 540,
                "Video should have advanced past the 9-minute mark, got: " + currentTime);
        System.out.println("Playback verified at " + (int)(currentTime / 60) + "m " + (int)(currentTime % 60) + "s – test passed.");
    }

    @Test(description = "TC-VP-05: Test continue watching – assure that playback works as well")
    public void continueWatching() {
        driver.get(BASE_URL);
        dismissCookieConsent();
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}

        // Find first card in the Continue Watching section using XPath text search
        WebElement continueSection = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//*[contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'continue watching')]" +
                         "/following::*[contains(@class,'card') or contains(@data-t,'card')][1]")));
        scrollToElement(continueSection);
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", continueSection);

        // Wait for navigation away from home
        wait.until(d -> !d.getCurrentUrl().equals(BASE_URL) && !d.getCurrentUrl().equals(BASE_URL + "/"));

        // Extended wait for the video player
        WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(30));
        try {
            longWait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("video, [class*='video-player'], [data-t*='player']")));
        } catch (TimeoutException e) {
            // May have landed on a series page – try clicking the first available play button
            System.out.println("Player not immediately visible after click. Current URL: " + driver.getCurrentUrl() +
                    ". Attempting to click a play button.");
            try {
                clickElement(By.cssSelector("[data-t='play-btn'], [class*='play-button'], [aria-label*='Play']"));
                longWait.until(ExpectedConditions.presenceOfElementLocated(
                        By.cssSelector("video, [class*='video-player'], [data-t*='player']")));
            } catch (Exception ex) {
                Assert.fail("Could not reach video player from Continue Watching section. Last URL: " + driver.getCurrentUrl());
            }
        }

        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("var v = document.querySelector('video'); if(v) v.play();");
        double startTime = ((Number) js.executeScript(
                "var v = document.querySelector('video'); return v ? v.currentTime : 0;")).doubleValue();

        try { Thread.sleep(10000); } catch (InterruptedException ignored) {}

        double endTime = ((Number) js.executeScript(
                "var v = document.querySelector('video'); return v ? v.currentTime : 0;")).doubleValue();

        Assert.assertTrue(endTime > startTime,
                "Video should have advanced during playback (start: " + startTime + ", end: " + endTime + ")");
        System.out.println("Continue Watching playback verified – advanced from "
                + (int) startTime + "s to " + (int) endTime + "s – test passed.");
    }
}
