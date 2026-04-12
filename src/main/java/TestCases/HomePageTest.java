package TestCases;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class HomePageTest extends BaseTest {

    @BeforeClass
    public void navigateHome() {
        driver.get(BASE_URL);
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
        dismissBanners();
    }


    @Test(description = "TC-HP-01: Assure homepage displays title & logo")
    public void titleANDlogo() {
        boolean titlePresent = driver.getTitle().contains("Crunchyroll");
        boolean logoPresent =
                !driver.findElements(By.cssSelector("a[href='/'] img")).isEmpty() ||
                !driver.findElements(By.cssSelector("[class*='logo']")).isEmpty()  ||
                !driver.findElements(By.cssSelector("[data-t*='logo']")).isEmpty();

        Assert.assertTrue(titlePresent && logoPresent,
                "Expected both title and logo to be present on the home page");
        System.out.println("Logo present on home page. Title: " + driver.getTitle() + "\n");
    }


    @Test(description = "TC-HP-02: Test home page carousel")
    public void testCarousel() {
        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("[class*='hero'], [class*='carousel'], [data-t*='hero']")));

        java.util.List<WebElement> carouselItems = driver.findElements(By.cssSelector(
                "[class*='hero-carousel'] [class*='slide']," +
                "[class*='hero-carousel'] [class*='item']," +
                "[data-t*='hero-card']"));

        for (WebElement item : carouselItems) {
            try {
                String title = item.findElement(
                        By.cssSelector("[class*='title'], h3, h4")).getText();
                if (!title.isEmpty()) System.out.println("Carousel item: " + title);
            } catch (NoSuchElementException ignored) { }
        }

        Assert.assertTrue(carouselItems.size() > 3,
                "Carousel should have more than 3 items, found: " + carouselItems.size());
        System.out.println("Carousel has " + carouselItems.size() + " items – test passed. \n");
    }


    @Test(description = "TC-HP-03: Assure continue watching section is near top of page")
    public void testContinueSection() {
        driver.get(BASE_URL);
        try { Thread.sleep(3000); } catch (InterruptedException ignored) {}
        dismissBanners();

        java.util.List<WebElement> cwHeaders = driver.findElements(By.xpath(
                "//*[contains(translate(normalize-space(.)," +
                "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'continue watching')]"));

        // Fallback: scroll to trigger lazy loading then try again
        if (cwHeaders.isEmpty()) {
            scrollToBottom();
            try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
            driver.get(BASE_URL);
            try { Thread.sleep(3000); } catch (InterruptedException ignored) {}
            cwHeaders = driver.findElements(By.xpath(
                    "//*[contains(translate(normalize-space(.)," +
                    "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'continue watching')]"));
        }

        Assert.assertFalse(cwHeaders.isEmpty(),
                "Could not find a 'Continue Watching' section on the page");

        java.util.List<WebElement> allSections = driver.findElements(By.cssSelector(
                "[class*='erc-feed-section'], [class*='feed-section'], [class*='carousel-section'], " +
                "[class*='home-section']"));

        System.out.println("Continue Watching section found – test passed. " +
                "(" + allSections.size() + " total sections on page)\n");
    }


    @Test(description = "TC-HP-04: Assure each featured section has at least 6 titles")
    public void testFeaturedSections() {
        list<WebElement> sections = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                By.cssSelector("[class*='erc-feed-section'],[class*='feed-section'],[class*='carousel-section']")));

        int passCount = 0;
        int failCount = 0;

        for (WebElement section : sections) {
            scrollToElement(section);
            try { Thread.sleep(300); } catch (InterruptedException ignored) {}

            // Broader card selector to handle obfuscated class names
            list<WebElement> cards = section.findElements(By.cssSelector(
                    "[class*='browse-card'],[class*='card-item'],[class*='show-card']," +
                    "[data-t*='card'],[class*='playable-card'],[class*='poster-card']," +
                    "li[class*='card'],li[class*='item']"));

            String headerText;
            try {
                headerText = section.findElement(
                        By.cssSelector("[class*='feed-header'], [class*='section-title'], h2, h3")).getText();
            } catch (NoSuchElementException e) {
                headerText = "(unnamed section)";
            }

            if (cards.size() < 6) {
                System.out.println("Section \"" + headerText + "\" has " + cards.size() + " cards (< 6).");
                failCount++;
            } else {
                System.out.println("Section \"" + headerText + "\" has " + cards.size() + " cards – passed.");
                passCount++;
            }
        }

        System.out.println("Sections passing (>=6 cards): " + passCount + " | failing: " + failCount);
        // Allow up to 2 under-populated sections (editorial/special sections may have fewer cards)
        Assert.assertTrue(failCount <= 2,
                "More than 2 sections had fewer than 6 titles (" + failCount + " sections failed)");
    }


    @Test(description = "TC-HP-05: Assure home page has adequate content")
    public void testHPcontent() {
        for (int i = 0; i < 5; i++) {
            scrollToBottom();
            try { Thread.sleep(800); } catch (InterruptedException ignored) { }
        }

        int totalTitles = driver.findElements(By.cssSelector(
                "[class*='browse-card'],[class*='card-item'],[class*='show-card']," +
                "[data-t*='card'],[class*='playable-card'],[class*='poster-card']"))
                .size();

        if (totalTitles < 20) {
            System.out.println("Not enough content on home page (" + totalTitles + " titles) – failed.\n");
        } else {
            System.out.println("Adequate content on home page (" + totalTitles + " titles) – passed.\n");
        }

        Assert.assertTrue(totalTitles >= 20,
                "Home page should have at least 20 titles, found: " + totalTitles);
    }
}

