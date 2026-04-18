package TestCases;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.Test;

public class SubServicesTest extends BaseTest {

    private static final String STORE_URL = "https://store.crunchyroll.com";


    @Test(description = "TC-SS-01: Test manga sub service")
    public void testManga() {

        try{
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {}

        if (isElementPresent(MY_PROFILE)) {
            driver.findElement(MY_PROFILE).click();
        }

        // Click Manga in the nav
        clickElement(By.cssSelector("[data-t='header-menu-manga']"));
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {}

        // Select profile if the selection appears
        if (isElementPresent(MY_PROFILE)) {
            driver.findElement(MY_PROFILE).click();
        }

        wait.until(ExpectedConditions.urlContains("/manga"));
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {}

        Assert.assertTrue(driver.getCurrentUrl().contains("/manga"),
                "Should be on the manga page, got: " + driver.getCurrentUrl());
        System.out.println("Manga page loaded successfully - test passed.\n");

        try{
            Thread.sleep(9000);
        } catch (InterruptedException ignored) {}
    }


    @Test(description = "TC-SS-02: Test game sub service",
          dependsOnMethods = "testManga")
    public void testGames() {

        // Click the Games nav
        By gamesLocator = By.xpath("//a[@href='https://www.crunchyroll.com/games' and contains(@class,'nav-item--ImXGe')][.//span[normalize-space()='Games']]");
        WebElement gamesLink = wait.until(ExpectedConditions.visibilityOfElementLocated(gamesLocator));
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", gamesLink);

        wait.until(ExpectedConditions.urlContains("/games"));
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {}

        Assert.assertTrue(driver.getCurrentUrl().contains("/games"),
                "URL should be the games page, got: " + driver.getCurrentUrl());
        System.out.println("Games page loaded successfully - test passed.\n");

        try {
            Thread.sleep(5000);
        } catch (InterruptedException ignored) {}
    }


    @Test(description = "TC-SS-03: Test store sub service",
          dependsOnMethods = "testGames")
    public void testStore() {

        // Open the games page menu
        clickElement(By.cssSelector("div.menu-tab[tabindex='0'], div.css-10tn139 span.menu-title"));
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {}

        // Click Store from the menu
        clickElement(By.cssSelector("li.menu-item a.menu-item-title[href='https://store.crunchyroll.com/']"));
        wait.until(ExpectedConditions.urlContains("store.crunchyroll.com"));
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {}

        Assert.assertTrue(driver.getCurrentUrl().contains(STORE_URL),
                "URL should be at store.crunchyroll.com, got: " + driver.getCurrentUrl());
        System.out.println("Store page loaded successfully - test passed.\n");

        try {
            Thread.sleep(5000);
        } catch (InterruptedException ignored) {}
    }


    @Test(description = "TC-SS-04: Assure account info transfers over to store for user ease",
          dependsOnMethods = "testStore")
    public void testAccountTransfer() {

        // Click the account icon
        clickElement(By.cssSelector("button[aria-label='My Account']"));
        try {
            Thread.sleep(800);
        } catch (InterruptedException ignored) {}

        // Click "My Account" from the dropdown
        clickElement(By.cssSelector("a[href*='account'], [data-t='my-account']"));
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {}

        // Find the email displayed on the account page
        WebElement emailElement = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//dt[normalize-space(text())='Email:']/following-sibling::dd[1]")));
        String displayedEmail = emailElement.getText().trim();

        Assert.assertEquals(displayedEmail, VALID_EMAIL,
                "Store account email should match the Crunchyroll login email");
        System.out.println("Account info transferred successfully - test passed.\n");

        try {
            Thread.sleep(5000);
        } catch (InterruptedException ignored) {}

    }


    @Test(description = "TC-SS-05: Test news sub service",
          dependsOnMethods = "testAccountTransfer")
    public void testNews() {

        driver.get(BASE_URL);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {}

        // Click the News dropdown in the nav
        clickElement(By.cssSelector("[data-t='header-menu-news']"));
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {}

        // Click "all News" from the dropdown
        clickElement(By.cssSelector("[data-t='news-dropdown-item'] a[href='https://www.crunchyroll.com/news']"));
        wait.until(ExpectedConditions.urlContains("/news"));
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {}

        Assert.assertTrue(driver.getCurrentUrl().contains("/news"),
                "URL should be the news page, got: " + driver.getCurrentUrl());
        System.out.println("News page loaded successfully - test passed.\n");

        try {
            Thread.sleep(5000);
        } catch (InterruptedException ignored) {}

    }
}
