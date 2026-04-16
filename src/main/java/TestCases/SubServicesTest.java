package TestCases;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.Test;

public class SubServicesTest extends BaseTest {

    private static final String STORE_URL = "https://store.crunchyroll.com";

    // Profile selection
    private static final By SEBI_PROFILE = By.xpath(
            "//button[@data-t='profile-button'][.//p[normalize-space()='Sebi']] | " +
            "//*[@data-profile-can-switch='true'][.//p[normalize-space()='Sebi']]");


    @Test(description = "TC-SS-01: Test manga sub service")
    public void testManga() {

        // Click Manga in the nav
        clickElement(By.cssSelector("[data-t='header-menu-manga']"));
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}

        // Select Sebi profile if the selection appears
        if (isElementPresent(SEBI_PROFILE)) {
            driver.findElement(SEBI_PROFILE).click();
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

        // Click the exact Games nav anchor from the provided HTML.
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
        //i dont think this will work because this button is a hover drop down menu- might just have to directly use link. https://store.crunchyroll.com/account?redirected=1
        // Click the account icon- <button type="button" class="chakra-button headerIcons userIcon css-14nboyj" aria-label="My Account"><svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 20 20" fill="none" role="img" aria-label="user-icon"><path fill-rule="evenodd" clip-rule="evenodd" d="M9.9945 4.53973e-07C10.9903 0.000970453 11.9631 0.299578 12.7881 0.857519C13.6131 1.41546 14.2527 2.20729 14.6248 3.13143C14.9969 4.05557 15.0847 5.06988 14.8767 6.04424C14.6687 7.0186 14.1746 7.90857 13.4576 8.6C17.3654 10.045 19.8811 13.945 19.986 18.711L20 20H0V19C0 14.072 2.52261 10.06 6.52441 8.6C5.80702 7.90816 5.31271 7.01754 5.10496 6.04253C4.89721 5.06751 4.9855 4.05262 5.35851 3.12818C5.73152 2.20375 6.3722 1.41199 7.19829 0.854584C8.02437 0.297177 8.99813 -0.000424268 9.9945 4.53973e-07ZM9.9945 10C5.56694 10 2.47564 13.272 2.04887 17.89L2.03988 18H17.9461L17.9401 17.923C17.5234 13.411 14.519 10.131 10.2514 10.004L9.9945 10ZM9.9945 2C9.40149 2 8.82178 2.17595 8.32871 2.50559C7.83563 2.83524 7.45133 3.30377 7.22439 3.85195C6.99745 4.40013 6.93807 5.00333 7.05376 5.58527C7.16946 6.16721 7.45502 6.70176 7.87435 7.12132C8.29368 7.54088 8.82793 7.8266 9.40955 7.94236C9.99118 8.05811 10.594 7.9987 11.1419 7.77164C11.6898 7.54458 12.1581 7.16006 12.4875 6.66671C12.817 6.17336 12.9929 5.59334 12.9929 5C12.9929 4.20435 12.677 3.44129 12.1147 2.87868C11.5524 2.31607 10.7897 2 9.9945 2Z" fill="white"></path></svg></button>
        clickElement(By.cssSelector("button[aria-label='My Account']"));
        try {
            Thread.sleep(800);
        } catch (InterruptedException ignored) {}

        // Click "My Account" from the dropdown
        clickElement(By.cssSelector("a[href*='account'], [data-t='my-account']"));
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {}

        // Find the email displayed on the account page- <dl>
        //                <dt>Email:</dt>
        //                <dd>mouye.martin@gmail.com</dd>
        //            </dl>
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
        } catch (InterruptedException ignored) {
        }

        // Click the News dropdown in the nav
        clickElement(By.cssSelector("[data-t='header-menu-news']"));
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {
        }

        // Click "All News" from the dropdown
        clickElement(By.cssSelector("[data-t='news-dropdown-item'] a[href='https://www.crunchyroll.com/news']"));
        wait.until(ExpectedConditions.urlContains("/news"));
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {
        }

        Assert.assertTrue(driver.getCurrentUrl().contains("/news"),
                "URL should be the news page, got: " + driver.getCurrentUrl());
        System.out.println("News page loaded successfully - test passed.\n");

        try {
            Thread.sleep(5000);
        } catch (InterruptedException ignored) {}

    }
}
