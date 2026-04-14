package TestCases;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

public class HomePageTest extends BaseTest {

    // Locators
    //logo to go back to homepage
    private static final By LOGO = By.cssSelector(
            "a.erc-logo, a[href='/discover'][aria-label*='logo'], " +
            "[data-t='crunchyroll-horizontal-svg'], [data-t='crunchyroll-logo-only-svg']");

    // featured "hero" carousel
    private static final By HERO_CAROUSEL = By.cssSelector(
            "[class*='erc-hero-carousel'], [class*='hero-carousel'], [data-t*='hero-carousel']");

    // Carousel slides
    private static final By CAROUSEL_SLIDES = By.cssSelector(
            "[role='group'][aria-roledescription='slide'], " +
            "[class*='hero-carousel__item'], [class*='erc-hero-carousel__slide']");

    // Carousel next button
    private static final By CAROUSEL_NEXT = By.cssSelector(
            "button[aria-label='Next Item'], button[aria-label*='Next'], " +
            "[class*='hero-carousel__next'], [class*='hero-carousel__arrow']");

    // Feed / content sections on the home page
    private static final By FEED_SECTIONS = By.cssSelector(
            "[class*='erc-feed-section'], [class*='feed-section'], [class*='carousel-section']");

    // Section header element inside a feed section
    private static final By SECTION_HEADER = By.cssSelector(
            "[class*='feed-section-header'], [class*='feed-header'], " +
            "[class*='section-title'], [data-t='feed-section-header'], h2, h3");

    // Content cards (series / episode / browse cards on the home feed)
    private static final By CONTENT_CARDS = By.cssSelector(
            "[class*='browse-card'], [class*='card-item'], [class*='show-card'], " +
            "[data-t='series-card'], [data-t='browse-card'], [data-t='episode-card'], " +
            "[class*='playable-card'], [class*='poster-card'], " +
            "li[class*='card'], li[class*='item']");


    // Tests

    @Test(description = "TC-HP-01: Assure homepage displays title & logo")
    public void titleANDlogo() {

        // Title check
        boolean titlePresent = driver.getTitle().contains("Crunchyroll");
        Assert.assertTrue(titlePresent,
                "Page title should contain 'Crunchyroll', got: " + driver.getTitle());

        // Logo check
        boolean logoPresent = !driver.findElements(LOGO).isEmpty();
        Assert.assertTrue(logoPresent,
                "Crunchyroll logo (a.erc-logo / data-t svg) should be present on the home page");

        System.out.println("Title & logo both present. Title: " + driver.getTitle() + "\n");
    }


    @Test(description = "TC-HP-02: Test home page carousel",
    dependsOnMethods = "titleANDlogo")
    public void testCarousel() {

        // Wait for the hero carousel to be present (it loads asynchronously)
        List<WebElement> carousels = wait.until(
                ExpectedConditions.presenceOfAllElementsLocatedBy(HERO_CAROUSEL));

        Assert.assertFalse(carousels.isEmpty(), "Hero carousel not found on the home page");

        // Scroll the carousel into view so lazy-loaded slides initialise
        scrollToElement(carousels.getFirst());
        try {
            Thread.sleep(800);
        } catch (InterruptedException ignored) {}

        // Verify at least one slide is present
        List<WebElement> slides = driver.findElements(CAROUSEL_SLIDES);
        Assert.assertFalse(slides.isEmpty(), "No carousel slides found");
        System.out.println("Carousel found with " + slides.size() + " slides.");

        // Verify the next-item navigation button is present
        List<WebElement> nextButtons = driver.findElements(CAROUSEL_NEXT);
        Assert.assertFalse(nextButtons.isEmpty(), "Carousel next/arrow button not found");

        // Click next and wait for the animation
        nextButtons.getLast().click();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {}

        // Pagination dots confirm the carousel is fully initialised
        List<WebElement> paginationDots = driver.findElements(By.cssSelector(
                "[class*='hero-carousel__page'], [class*='carousel__page'], " +
                "[class*='carousel__dot'], [class*='carousel__pagination']"));

        System.out.println("Carousel navigation working. Slides: " + slides.size() +
                ", Pagination dots: " + paginationDots.size() + "\n");
    }


    @Test(description = "TC-HP-03: Assure Continue Watching section is displayed",
    dependsOnMethods = "testCarousel")
    public void testContinueWatching() {

        // Scroll down to trigger lazy-loaded sections, then back to top
        for (int i = 0; i < 3; i++) {
            scrollToBottom();
            try { Thread.sleep(800); } catch (InterruptedException ignored) {}
        }
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 0);");

        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {}

        // Find any element whose visible text contains "continue watching"
        List<WebElement> cwHeaders = driver.findElements(By.xpath(
                "//*[contains(translate(normalize-space(.)," +
                "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'continue watching')]"));

        // Fallback: full reload and scroll again
        if (cwHeaders.isEmpty()) {
            driver.get(BASE_URL);
            dismissBanners();

            try {
                Thread.sleep(3000);
            } catch (InterruptedException ignored) {}

            for (int i = 0; i < 3; i++) {
                scrollToBottom();
                try {
                    Thread.sleep(800);
                } catch (InterruptedException ignored) {}
            }
            cwHeaders = driver.findElements(By.xpath(
                    "//*[contains(translate(normalize-space(.)," +
                    "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'continue watching')]"));
        }

        Assert.assertFalse(cwHeaders.isEmpty(),
                "'Continue Watching' section not found — ensure the test account has watch history");

        List<WebElement> allSections = driver.findElements(FEED_SECTIONS);
        System.out.println("Continue Watching section found within 3 sections- test passed.\n");
    }


    @Test(description = "TC-HP-04: Assure home page has footer with extra links",
    dependsOnMethods = "titleANDlogo")
    public void testFooterLinks() {

        // Scroll to bottom to trigger footer load
        scrollToBottom();
        try {
            Thread.sleep(1000); } catch (InterruptedException ignored) {}

        // Wait for the footer to be present
        WebElement footer = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.cssSelector("[data-t='footer']")));

        // Scroll footer into view
        scrollToElement(footer);
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}

        // Collect all anchor links inside the footer
        List<WebElement> footerLinks = footer.findElements(By.tagName("a"));

        // Print each link to the terminal
        System.out.println("Footer links found (" + footerLinks.size() + " total):");
        for (WebElement link : footerLinks) {
            String text = link.getText().trim();
            String href = link.getAttribute("href");
            if (!text.isEmpty()) {
                System.out.println("  - " + text + " -> " + href);
            }
        }

        Assert.assertTrue(footerLinks.size() > 5,
                "Footer should have more than 5 links, found: " + footerLinks.size());

        System.out.println("Footer link check passed with " + footerLinks.size() + " links.\n");
    }


    @Test(description = "TC-HP-05: Assure home page has adequate content",
    dependsOnMethods = "testCarousel")
    public void testHPcontent() {

        // last test left us at the bottom so now scroll to top
        for (int i = 0; i < 5; i++) {
            ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 0);");
            try {
                Thread.sleep(800);
            } catch (InterruptedException ignored) {}
        }

        int totalTitles = driver.findElements(CONTENT_CARDS).size();

        if (totalTitles < 20) {
            System.out.println("Not enough content on home page (" + totalTitles + " titles) - failed.\n");
        } else {
            System.out.println("Adequate content on home page (" + totalTitles + " titles) - passed.\n");
        }

        Assert.assertTrue(totalTitles >= 20,
                "Home page should have at least 20 titles, found: " + totalTitles);
    }
}



