package TestCases;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.List;

public class ShowTest extends BaseTest {

    //Locators/ Selectors
    // Series hero logo / title area
    private static final By SERIES_TITLE = By.cssSelector(
            "[data-t='series-hero-logo'], h1, [class*='hero-title'], [class*='series-hero__title']");

    // Synopsis / description
    private static final By SERIES_DESCRIPTION = By.cssSelector(
            "[class*='erc-series-description'], [class*='series-description'], " +
            "[data-t='series-description']");

    // Metadata detail rows: audio, subtitles, genres, content advisory
    private static final By SERIES_METADATA = By.cssSelector(
            "[data-t='detail-row-audio-language'], [data-t='detail-row-genres'], " +
            "[data-t='detail-row-subtitles-language'], [class*='details-item'][data-t]");

    // Episode cards
    private static final By EPISODE_CARDS = By.cssSelector(
            "[data-t^='episode-card'], [class*='playable-card--']");

    // Season dropdown trigger
    private static final By SEASON_DROPDOWN = By.xpath(
            "//span[contains(@class,'select-trigger__title-truncated-text')]/ancestor::div[contains(@class,'select-trigger') and @role='button'][1]");
    // Season option items revealed after opening the dropdown
    private static final By SEASON_OPTIONS = By.cssSelector(
            "div[role='option'][class*='extended-option'], div[role='option']");

    // Specific season option for Skypiea from the provided dropdown HTML.
    private static final By SKYPIEA_OPTION = By.xpath(
            "//div[@role='option' and contains(@class,'extended-option')][.//span[contains(@class,'extended-option__text') and contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'skypiea')]]");
    // Video player
    private static final By VIDEO_PLAYER = By.cssSelector(
            "video, [id='player-container'], [data-testid='player-controls-root'], " +
            "[class*='video-player'], [class*='player-container']");



    @Test(description = "TC-VP-01: Verify series info properly displays")
    public void seriesInfo() {

        try{
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {}

        if (isElementPresent(MY_PROFILE)) {
            driver.findElement(MY_PROFILE).click();
        }

        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {}


        // Click on One Piece from the home page feed
        By onePieceCard = By.cssSelector("a[href='/series/GRMG8ZQZR/one-piece']");
        WebElement onePieceLink = wait.until(ExpectedConditions.presenceOfElementLocated(onePieceCard));

        scrollToElement(onePieceLink);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", onePieceLink);
        wait.until(ExpectedConditions.urlContains("one-piece"));
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {}

        // check info
        // series logo/title
        boolean titlePresent = isElementPresent(SERIES_TITLE);

        // Description
        boolean descPresent = isElementPresent(SERIES_DESCRIPTION);

        // Metadata
        boolean metaPresent = isElementPresent(SERIES_METADATA);

        Assert.assertTrue(titlePresent, "Series title/logo should be displayed");
        Assert.assertTrue(descPresent,  "Series description/synopsis should be displayed");
        Assert.assertTrue(metaPresent,  "Series metadata (audio/subtitles/genres) should be displayed");

        System.out.println("Series info verified. Page title: " + driver.getTitle() + "\n");

        try {
            Thread.sleep(9000);
        } catch (InterruptedException ignored) {}
    }


    @Test(description = "TC-VP-02: Verify episode details display correctly",
          dependsOnMethods = "seriesInfo")
    public void episodeDetails() {

        //still on one piece page from previous test
        // Wait for episode cards to load
        List<WebElement> episodes = wait.until(
                ExpectedConditions.presenceOfAllElementsLocatedBy(EPISODE_CARDS));

        Assert.assertFalse(episodes.isEmpty(), "Episode list should not be empty");

        WebElement first = episodes.getFirst();
        scrollToElement(first);
        try {
            Thread.sleep(300);
        } catch (InterruptedException ignored) {}

        // Title
        boolean hasTitle = !first.findElements(By.cssSelector(
                "h3[class*='playable-card__title'], [data-t='episode-title']")).isEmpty();

        // Sub/Dub availability
        boolean hasSubDub = !first.findElements(By.cssSelector(
                "[data-t='meta-tags'], [class*='meta-tags']")).isEmpty();

        // Air date
        boolean hasAirDate = !first.findElements(By.cssSelector(
                "[data-t='meta-info'], [class*='playable-card-hover__release']")).isEmpty();

        Assert.assertTrue(hasTitle,   "Episode card should contain a title");
        Assert.assertTrue(hasSubDub,  "Episode card should show Sub/Dub availability");
        Assert.assertTrue(hasAirDate, "Episode card hover DOM should include a release/air date");

        System.out.println("Episode details verified. Total episode cards visible: " + episodes.size() + "\n");

        try {
            Thread.sleep(9000);
        } catch (InterruptedException ignored) {}
    }


    @Test(description = "TC-VP-03: Verify drop down menu for seasons",
          dependsOnMethods = "episodeDetails")
    public void seasonsMenu() {

        WebElement seasonTrigger = wait.until(
                ExpectedConditions.elementToBeClickable(SEASON_DROPDOWN));

        Assert.assertTrue(seasonTrigger.isDisplayed(), "Season dropdown trigger should be visible");
        String currentSeason = seasonTrigger.getText().trim();
        System.out.println("Current season shown in trigger: " + currentSeason);

        // Open dropdown
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", seasonTrigger);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", seasonTrigger);
        try {
            Thread.sleep(600);
        } catch (InterruptedException ignored) {}

        // Collect revealed options
        List<WebElement> options = wait.until(
                ExpectedConditions.presenceOfAllElementsLocatedBy(SEASON_OPTIONS));

        Assert.assertTrue(options.size() > 1,
                "Season dropdown should expose more than one season, found: " + options.size());

        // Specifically select the Skypiea season from option rows.
        if (isElementPresent(SKYPIEA_OPTION)) {
            WebElement skypiea = wait.until(ExpectedConditions.elementToBeClickable(SKYPIEA_OPTION));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", skypiea);
            System.out.println("Selected Skypiea season.");
        } else {
            System.out.println("Skypiea season not found - skipping selection.");
        }


        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {}

        // Confirm episode cards reloaded after the season switch
        wait.until(ExpectedConditions.presenceOfElementLocated(EPISODE_CARDS));
        System.out.println("Season dropdown functional. Seasons available: " + options.size() + "\n");

        try {
            Thread.sleep(9000);
        } catch (InterruptedException ignored) {}
    }


    @Test(description = "TC-VP-04: Test playback on episode 146",
          dependsOnMethods = "seasonsMenu")
    public void testPlayback() {

        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {}

        // should already be on the Skypiea season- select episode 146
        WebElement ep146 = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("a[href*='GEVUZMQ3X']")));

        scrollToElement(ep146);

        try {
            Thread.sleep(500);
        } catch (InterruptedException ignored) {}
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", ep146);

        // Extended wait — streaming player
        WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(30));
        longWait.until(ExpectedConditions.presenceOfElementLocated(VIDEO_PLAYER));

        // go to 9 minutes (540 s) and play
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("var v = document.querySelector('video'); if(v){ v.currentTime = 540; v.play(); } else { return; }");
        try {
            Thread.sleep(11000);
        } catch (InterruptedException ignored) {}

        double currentTime = ((Number) js.executeScript(
                "var v = document.querySelector('video'); if(v){ return v.currentTime; } return 0;")).doubleValue();

        Assert.assertTrue(currentTime > 540,
                "Video should have advanced past the 9-minute mark, got: " + currentTime + "s");
        System.out.println("Playback verified at " + (int)(currentTime / 60) + "m " +
                (int)(currentTime % 60) + "s – test passed.\n");

    }


    @Test(description = "TC-VP-05: Test continue watching – assure playback works from home feed",
        dependsOnMethods = "testPlayback")
    public void continueWatching() {

        try {
            Thread.sleep(4000);
        } catch (InterruptedException ignored) {}

        // Click logo to go back to home page
        clickElement(By.cssSelector("a.erc-logo, a[href='/discover']"));
        wait.until(ExpectedConditions.urlContains("/discover"));
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {}

        // Scroll to continue watching section
        for (int i = 0; i < 3; i++) {
            scrollToBottom();
            try { Thread.sleep(800); } catch (InterruptedException ignored) {}
        }

        By historySection = By.cssSelector("[data-t='history']");
        WebElement continueWatchingSection = wait.until(
                ExpectedConditions.visibilityOfElementLocated(historySection));
        scrollToElement(continueWatchingSection);
        try {
            Thread.sleep(500);
        } catch (InterruptedException ignored) {}

        // Then click the first episode or title within that section
        List<WebElement> cwLinks = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                By.cssSelector("[data-t='history'] a[href*='/watch/']")));

        Assert.assertFalse(cwLinks.isEmpty(),
                "Continue Watching section should have at least one episode card");

        WebElement firstCard = cwLinks.stream()
                .filter(el -> {
                    try {
                        return el.isDisplayed();
                    } catch (Exception ignored) {
                        return false;
                    }
                })
                .findFirst()
                .orElse(cwLinks.getFirst());

        scrollToElement(firstCard);
        try {
            Thread.sleep(500);
        } catch (InterruptedException ignored) {}
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", firstCard);

        // Wait for navigation to the selected watch page.
        wait.until(ExpectedConditions.urlContains("/watch/"));


        WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(30));
        longWait.until(ExpectedConditions.presenceOfElementLocated(VIDEO_PLAYER));
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ignored) {}

        JavascriptExecutor js = (JavascriptExecutor) driver;
        double startTime = ((Number) js.executeScript(
                "var v = document.querySelector('video'); if(v){ return v.currentTime; } return 0;")).doubleValue();

        try {
            Thread.sleep(9000);
        } catch (InterruptedException ignored) {}

        double endTime = ((Number) js.executeScript(
                "var v = document.querySelector('video'); if(v){ return v.currentTime; } return 0;")).doubleValue();

        Assert.assertTrue(endTime > startTime,
                "Video should have advanced during playback (start: " + startTime + "s, end: " + endTime + "s)");
        System.out.println("Continue Watching playback verified – advanced from " +
                (int) startTime + "s to " + (int) endTime + "s – test passed.\n");

        try {
            Thread.sleep(9000);
        } catch (InterruptedException ignored) {}
    }
}
