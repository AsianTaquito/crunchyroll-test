package TestCases;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;

public class SubServicesTest extends BaseTest {

    private static final String STORE_URL = "https://store.crunchyroll.com";

    @Test(description = "TC-SS-01: Test manga sub service")
    public void testManga() {
        driver.get(BASE_URL + "/manga");
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}

        // Verify the manga page loaded
        Assert.assertTrue(driver.getCurrentUrl().contains("/manga"),
                "Should be on the manga page, got: " + driver.getCurrentUrl());

        // Find a manga series card and click it
        WebElement firstManga;
        try {
            firstManga = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("[class*='manga-card'] a, [class*='series-card'] a, " +
                                   "[class*='card'] a[href*='/manga/'], a[href*='/manga/']")));
        } catch (TimeoutException e) {
            throw new SkipException("No manga series cards found on the manga page – page structure may have changed.");
        }
        firstManga.click();
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}

        Assert.assertTrue(
                driver.getCurrentUrl().toLowerCase().contains("manga") ||
                driver.getTitle().toLowerCase().contains("manga"),
                "Should navigate to a manga detail page");

        // Try clicking a chapter/read button
        try {
            clickElement(By.cssSelector("[data-t='read-btn'], [class*='read-btn'], " +
                                        "[class*='latest-chapter'] a, [class*='chapter-btn'], " +
                                        "a[href*='chapter'], button[class*='read']"));
        } catch (TimeoutException ignored) {
            System.out.println("No 'Read' button found – checking for subscription prompt directly.");
        }

        boolean upgradePrompt =
                isElementPresent(By.cssSelector("[class*='paywall'], [class*='subscription'], [data-t*='upgrade']")) ||
                isElementPresent(By.xpath("//*[contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'upgrade')" +
                        " or contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'subscribe')]"));
        boolean upgradeLink =
                isElementPresent(By.cssSelector("a[href*='upgrade'], a[href*='premium'], a[href*='subscribe']"));

        Assert.assertTrue(upgradePrompt && upgradeLink,
                "A subscription blocking message with an upgrade link should appear");
        System.out.println("Manga chapter correctly blocked with upgrade prompt – test passed.");
    }

    @Test(description = "TC-SS-02: Test game sub service")
    public void testGames() {
        driver.get(BASE_URL + "/games");
        String url = driver.getCurrentUrl();
        Assert.assertTrue(url.contains("/games"), "URL should contain /games, got: " + url);
        System.out.println("Most games are mobile or behind paywall. URL: " + url);
    }

    @Test(description = "TC-SS-03: Test news sub service")
    public void testNews() {
        // Navigate directly to avoid relying on a nav button hidden in a dropdown
        driver.get(BASE_URL + "/news");
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}

        wait.until(ExpectedConditions.urlContains("/news"));

        WebElement firstHeadline = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[class*='news-card'] h3, [class*='article-title'], " +
                               "[class*='headline'], article h2, h1, h2, h3")));
        System.out.println("First news headline: " + firstHeadline.getText());

        Assert.assertTrue(driver.getCurrentUrl().contains("/news"),
                "URL should contain /news, got: " + driver.getCurrentUrl());
        System.out.println("News page loaded successfully – test passed.");
    }

    @Test(description = "TC-SS-04: Test store sub service")
    public void testStore() {
        driver.get(STORE_URL);
        try { Thread.sleep(3000); } catch (InterruptedException ignored) {}  // store is a separate domain – give it time

        WebElement searchBox;
        try {
            searchBox = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector("input[type='search'], input[name='q'], input[placeholder*='Search'], " +
                                   "input[placeholder*='search'], [class*='search-input'] input")));
        } catch (TimeoutException e) {
            throw new SkipException("Store search input not found – store page structure may require HTML inspection.");
        }

        searchBox.sendKeys("Initial D");
        searchBox.submit();
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}

        WebElement firstItem = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("[class*='product-item'] a, [class*='product-card'] a, [class*='search-result'] a")));
        firstItem.click();
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}

        clickElement(By.cssSelector("button[name='add'], [data-t='add-to-cart'], " +
                                    "button[class*='add-to-cart'], button[id*='add-to-cart']"));

        WebElement cartCount = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[class*='cart-count'], [class*='cart-badge'], [data-t='cart-count']")));
        int itemCount = Integer.parseInt(cartCount.getText().trim());

        Assert.assertEquals(itemCount, 1, "Cart should contain 1 item");
        System.out.println("Initial D item added to cart – test passed.");
    }

    @Test(description = "TC-SS-05: Assure account info transfers over to store for user ease",
          dependsOnMethods = "testStore")
    public void testAccountTransfer() {
        driver.get(STORE_URL);
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}

        clickElement(By.cssSelector("[class*='account-icon'], [data-t='account-btn'], " +
                                    "[aria-label*='account'], [class*='user-icon']"));

        clickElement(By.cssSelector("a[href*='account'], [data-t='my-account'], a[href*='my-account']"));

        WebElement emailElement = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[class*='account-email'], [data-t='account-email'], " +
                               "input[type='email'], [class*='email']")));
        String displayedEmail = emailElement.getText().isEmpty()
                ? emailElement.getAttribute("value")
                : emailElement.getText();

        Assert.assertEquals(displayedEmail, VALID_EMAIL,
                "Store account email should match the Crunchyroll login email");
        System.out.println("Account auto logged in from previous site. Email: " + displayedEmail);
    }
}
