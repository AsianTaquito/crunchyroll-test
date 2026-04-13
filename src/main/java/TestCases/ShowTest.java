package TestCases;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.List;

public class ShowTest extends BaseTest {


    // Series hero logo / title area
    // <div class="hero-logo---Zh2A" data-t="series-hero-logo">
    private static final By SERIES_TITLE = By.cssSelector(
            "[data-t='series-hero-logo'], h1, [class*='hero-title'], [class*='series-hero__title']");

    // Synopsis / description
    // <div class="erc-series-description details-section-item">
    private static final By SERIES_DESCRIPTION = By.cssSelector(
            "[class*='erc-series-description'], [class*='series-description'], " +
            "[data-t='series-description']");

    // Metadata detail rows: audio, subtitles, genres, content advisory
    // <dl data-t="detail-row-audio-language">, <dl data-t="detail-row-genres">
    private static final By SERIES_METADATA = By.cssSelector(
            "[data-t='detail-row-audio-language'], [data-t='detail-row-genres'], " +
            "[data-t='detail-row-subtitles-language'], [class*='details-item'][data-t]");

    // Episode cards  <div class="playable-card--GnRbX" data-t="episode-card ">
    // data-t has a trailing space so use ^= (starts-with) instead of =
    private static final By EPISODE_CARDS = By.cssSelector(
            "[data-t^='episode-card'], [class*='playable-card--']");

    // Season dropdown trigger (custom component – NOT a native <select>)
    // <div role="button" aria-label="Seasons" class="dropdown-trigger--P--FX …">
    private static final By SEASON_DROPDOWN = By.cssSelector(
            "[aria-label='Seasons'][role='button'], " +
            "[class*='erc-seasons-select'] [role='button'], " +
            ".seasons-select [role='button']");

    // Season option items revealed after opening the dropdown
    private static final By SEASON_OPTIONS = By.cssSelector(
            "[role='option'], [class*='season-list-item'], [class*='dropdown-item']");

    // Video player
    // <div id="player-container" aria-label="Video Player">  /  <video id="bitmovinplayer-video-null">
    // <div data-testid="player-controls-root">
    private static final By VIDEO_PLAYER = By.cssSelector(
            "video, [id='player-container'], [data-testid='player-controls-root'], " +
            "[class*='video-player'], [class*='player-container']");



    @Test(description = "TC-VP-01: Verify series info properly displays")
    public void seriesInfo() {
        // Hero logo: <div class="hero-logo---Zh2A" data-t="series-hero-logo">
        boolean titlePresent = isElementPresent(SERIES_TITLE);

        // Description: <div class="erc-series-description details-section-item"><p>…</p></div>
        boolean descPresent = isElementPresent(SERIES_DESCRIPTION);

        // Metadata rows: <dl data-t="detail-row-audio-language">, <dl data-t="detail-row-genres">, …
        boolean metaPresent = isElementPresent(SERIES_METADATA);

        Assert.assertTrue(titlePresent, "Series title/logo should be displayed");
        Assert.assertTrue(descPresent,  "Series description/synopsis should be displayed");
        Assert.assertTrue(metaPresent,  "Series metadata (audio/subtitles/genres) should be displayed");

        System.out.println("Series info verified. Page title: " + driver.getTitle() + "\n");
    }


    @Test(description = "TC-VP-03: Verify episode details display correctly",
          dependsOnMethods = "seriesInfo")
    public void episodeDetails() {
        // Wait for episode cards to load
        // <div class="playable-card--GnRbX" data-t="episode-card ">
        List<WebElement> episodes = wait.until(
                ExpectedConditions.presenceOfAllElementsLocatedBy(EPISODE_CARDS));

        Assert.assertFalse(episodes.isEmpty(), "Episode list should not be empty");

        WebElement first = episodes.getFirst();
        scrollToElement(first);
        try { Thread.sleep(300); } catch (InterruptedException ignored) {}

        // Title — available in regular card body:
        // <h3 class="playable-card__title--rgmp7"><a>E146 - Quit Dreaming!…</a></h3>
        // Also present inside the hover DOM (hidden until hover):
        // <h3 data-t="episode-title">…</h3>
        boolean hasTitle = !first.findElements(By.cssSelector(
                "h3[class*='playable-card__title'], [data-t='episode-title']")).isEmpty();

        // Sub/Dub availability — in the regular card footer:
        // <div class="meta-tags--o8OYw" data-t="meta-tags"><span>Dub | Sub</span></div>
        boolean hasSubDub = !first.findElements(By.cssSelector(
                "[data-t='meta-tags'], [class*='meta-tags']")).isEmpty();

        // Air date — inside the hover component DOM (exists in DOM even without visual hover):
        // <span class="text--…" data-t="meta-info">07/05/2023</span>
        boolean hasAirDate = !first.findElements(By.cssSelector(
                "[data-t='meta-info'], [class*='playable-card-hover__release']")).isEmpty();

        Assert.assertTrue(hasTitle,   "Episode card should contain a title");
        Assert.assertTrue(hasSubDub,  "Episode card should show Sub/Dub availability");
        Assert.assertTrue(hasAirDate, "Episode card hover DOM should include a release/air date");

        System.out.println("Episode details verified. Total episode cards visible: " + episodes.size() + "\n");
    }


    @Test(description = "TC-VP-02: Verify drop down menu for seasons",
          dependsOnMethods = "episodeDetails")
    public void seasonsMenu() {
        // The season selector is a custom dropdown — NOT a native <select>.
        // Trigger: <div role="button" aria-label="Seasons" class="dropdown-trigger--P--FX …">
        //          <div class="season-info"><span>Skypiea (144-206)</span></div>
        WebElement seasonTrigger = wait.until(
                ExpectedConditions.elementToBeClickable(SEASON_DROPDOWN));

        Assert.assertTrue(seasonTrigger.isDisplayed(), "Season dropdown trigger should be visible");
        String currentSeason = seasonTrigger.getText().trim();
        System.out.println("Current season shown in trigger: " + currentSeason);

        // Open the dropdown
        seasonTrigger.click();
        try { Thread.sleep(600); } catch (InterruptedException ignored) {}

        // Collect revealed options (e.g. <li role="option"> or <div class="dropdown-item">)
        List<WebElement> options = wait.until(
                ExpectedConditions.presenceOfAllElementsLocatedBy(SEASON_OPTIONS));

        Assert.assertTrue(options.size() > 1,
                "Season dropdown should expose more than one season, found: " + options.size());

        // Select a season that differs from the currently displayed one
        for (WebElement opt : options) {
            if (!opt.getText().trim().equals(currentSeason)) {
                opt.click();
                break;
            }
        }

        try { Thread.sleep(1000); } catch (InterruptedException ignored) {}

        // Confirm episode cards reloaded after the season switch
        wait.until(ExpectedConditions.presenceOfElementLocated(EPISODE_CARDS));
        System.out.println("Season dropdown functional. Seasons available: " + options.size() + "\n");
    }


    @Test(description = "TC-VP-04: Test playback on episode 146",
          dependsOnMethods = "seasonsMenu")
    public void testPlayback() {
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}

        // Try to select the Skypiea (144–206) season so ep 146's card is in the list
        try {
            WebElement trigger = wait.until(ExpectedConditions.elementToBeClickable(SEASON_DROPDOWN));
            trigger.click();
            try { Thread.sleep(600); } catch (InterruptedException ignored) {}
            for (WebElement opt : driver.findElements(SEASON_OPTIONS)) {
                String text = opt.getText();
                if (text.contains("144") || text.toLowerCase().contains("skypiea")) {
                    opt.click();
                    try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
                    break;
                }
            }
        } catch (Exception ignored) {}

        // Episode 146 card link — known href from HTML comment:
        // <a href="/watch/GEVUZMQ3X/quit-dreaming-mock-town-the-town-of-ridicule">
        WebElement ep146 = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("a[href*='GEVUZMQ3X']")));

        scrollToElement(ep146);
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}
        // JS click bypasses any overlay that may intercept
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", ep146);

        // Extended wait — streaming player (Bitmovin) takes up to 30 s to initialise
        // <div id="player-container" aria-label="Video Player">
        // <video id="bitmovinplayer-video-null" src="blob:…">
        // <div data-testid="player-controls-root">
        WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(30));
        longWait.until(ExpectedConditions.presenceOfElementLocated(VIDEO_PLAYER));

        // Seek to 9 minutes (540 s) and play
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("var v = document.querySelector('video'); if(v){ v.currentTime = 540; v.play(); }");
        try { Thread.sleep(10000); } catch (InterruptedException ignored) {}

        double currentTime = ((Number) js.executeScript(
                "var v = document.querySelector('video'); return v ? v.currentTime : 0;")).doubleValue();

        Assert.assertTrue(currentTime > 540,
                "Video should have advanced past the 9-minute mark, got: " + currentTime + "s");
        System.out.println("Playback verified at " + (int)(currentTime / 60) + "m " +
                (int)(currentTime % 60) + "s – test passed.\n");
    }


    @Test(description = "TC-VP-05: Test continue watching – assure playback works from home feed")
    public void continueWatching() {
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}

        // Scroll to trigger lazy loading of the history / continue-watching section
        for (int i = 0; i < 3; i++) {
            scrollToBottom();
            try { Thread.sleep(800); } catch (InterruptedException ignored) {}
        }
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 0);");
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}

        // History section container: <div class="erc-history-collection" data-t="history">
        // Cards inside: <div class="collection-item"> > <div data-t="episode-card ">
        // Thumbnail link: <a class="playable-card__thumbnail-wrapper--BkWZo" href="/watch/…">
        List<WebElement> cwLinks = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                By.cssSelector("[data-t='history'] a[class*='playable-card__thumbnail-wrapper']")));

        Assert.assertFalse(cwLinks.isEmpty(),
                "Continue Watching section should have at least one episode card");

        WebElement firstCard = cwLinks.getFirst();
        scrollToElement(firstCard);
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}
        // JS click avoids any overlay/banner blocking the element
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", firstCard);

        // Wait for navigation away from the home page
        wait.until(d -> !d.getCurrentUrl().equals(BASE_URL) && !d.getCurrentUrl().equals(BASE_URL + "/"));

        // Wait for the Bitmovin video player to initialise
        // <div id="player-container" aria-label="Video Player">
        // <video id="bitmovinplayer-video-null" src="blob:…">
        // <div data-testid="player-controls-root">
        WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(30));
        longWait.until(ExpectedConditions.presenceOfElementLocated(VIDEO_PLAYER));
        try { Thread.sleep(3000); } catch (InterruptedException ignored) {} // allow buffering

        JavascriptExecutor js = (JavascriptExecutor) driver;
        double startTime = ((Number) js.executeScript(
                "var v = document.querySelector('video'); return v ? v.currentTime : 0;")).doubleValue();

        try { Thread.sleep(10000); } catch (InterruptedException ignored) {}

        double endTime = ((Number) js.executeScript(
                "var v = document.querySelector('video'); return v ? v.currentTime : 0;")).doubleValue();

        Assert.assertTrue(endTime > startTime,
                "Video should have advanced during playback (start: " + startTime + "s, end: " + endTime + "s)");
        System.out.println("Continue Watching playback verified – advanced from " +
                (int) startTime + "s to " + (int) endTime + "s – test passed.\n");
    }
}
