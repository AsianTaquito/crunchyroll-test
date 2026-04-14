package TestCases;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;



//more waits for presentation

//



public class SubServicesTest extends BaseTest {

    private static final String STORE_URL = "https://store.crunchyroll.com";

    // Saved so testAccountTransfer can switch back after the store tab closes
    private String mainWindowHandle;

    // ── Tests ─────────────────────────────────────────────────────────────────

    @Test(description = "TC-SS-01: Test manga sub service")
    public void testManga() {

        // Click Manga in the nav
        clickElement(By.cssSelector("[data-t='header-menu-manga']"));
        wait.until(ExpectedConditions.urlContains("/manga"));


        try{
            Thread.sleep(9000);
        } catch (InterruptedException e) {}

        Assert.assertTrue(driver.getCurrentUrl().contains("/manga"),
                "Should be on the manga page, got: " + driver.getCurrentUrl());
        System.out.println("Manga page loaded successfully - test passed.\n");


    }


    @Test(description = "TC-SS-02: Test game sub service",
    dependsOnMethods = "testManga")
    public void testGames() {

        // Return to home so the nav is accessible
        driver.get(BASE_URL);
        try {
            Thread.sleep(1500);
        } catch (InterruptedException ignored) {}

        //make sure to select profile so it doesnt get stuck
        WebElement sebiProfile = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[@data-t='profile-button']" +
                        "[.//p[@data-t='profile-name' and normalize-space()='Sebi']]")));
        sebiProfile.click();
        try{
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {}

        // Click Games in the nav - opens a new tab
        String beforeHandle = driver.getWindowHandle();
        clickElement(By.cssSelector("[data-t='header-menu-games']"));

        // Switch to the new tab
        List<String> handles = new ArrayList<>(driver.getWindowHandles());
        handles.remove(beforeHandle);
        driver.switchTo().window(handles.getFirst());

        try {
            Thread.sleep(9000);
        } catch (InterruptedException ignored) {}

        wait.until(ExpectedConditions.urlContains("games"));
        String url = driver.getCurrentUrl();
        Assert.assertTrue(url.contains("/games/"), "URL should contain /games, got: " + url);
        System.out.println("Games page loaded. URL: " + url + "\n");

        // Close the games tab and return to the main CR tab
        driver.close();
        driver.switchTo().window(beforeHandle);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ignored) {}
    }


    @Test(description = "TC-SS-03: Test news sub service",
    dependsOnMethods = "testGames")
    public void testNews() {

        // Return to home so the nav is accessible
        driver.get(BASE_URL);
        try {
            Thread.sleep(1500);
        } catch (InterruptedException ignored) {}

        //make sure to select profile so it doesnt get stuck
        WebElement sebiProfile = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[@data-t='profile-button']" +
                        "[.//p[@data-t='profile-name' and normalize-space()='Sebi']]")));
        sebiProfile.click();
        try{
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {}

        // Click the News dropdown in the nav
        clickElement(By.cssSelector("[data-t='header-menu-news']"));
        try {
            Thread.sleep(600);
        } catch (InterruptedException ignored) {}

        // Click "All News" from the dropdown
        clickElement(By.cssSelector("[data-t='news-dropdown-item'] a[href*='/news']"));
        wait.until(ExpectedConditions.urlContains("/news"));

        Assert.assertTrue(driver.getCurrentUrl().contains("/news"),
                "URL should contain /news, got: " + driver.getCurrentUrl());
        System.out.println("News page loaded successfully - test passed.\n");

        try {
            Thread.sleep(5000);
        } catch (InterruptedException ignored) {}
    }


    @Test(description = "TC-SS-04: Test store sub service",
    dependsOnMethods = "testNews")
    public void testStore() {

        driver.get(BASE_URL);
        try {
            Thread.sleep(1500);
        } catch (InterruptedException ignored) {}

        //make sure to select profile so it doesnt get stuck
        WebElement sebiProfile = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[@data-t='profile-button']" +
                        "[.//p[@data-t='profile-name' and normalize-space()='Sebi']]")));
        sebiProfile.click();
        try{
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {}

        // Save main handle, then click Store (opens new tab)
        mainWindowHandle = driver.getWindowHandle();
        clickElement(By.cssSelector("[data-t='header-menu-store']"));
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ignored) {}

        // Switch to the store tab
        List<String> handles = new ArrayList<>(driver.getWindowHandles());
        handles.remove(mainWindowHandle);
        driver.switchTo().window(handles.getFirst());

        wait.until(ExpectedConditions.urlContains("store.crunchyroll.com"));
        Assert.assertTrue(driver.getCurrentUrl().contains(STORE_URL),
                "URL should be at store.crunchyroll.com, got: " + driver.getCurrentUrl());
        System.out.println("Store page loaded. URL: " + driver.getCurrentUrl() + "\n");

         try {
            Thread.sleep(5000);
        } catch (InterruptedException ignored) {}
    }


    @Test(description = "TC-SS-05: Assure account info transfers over to store for user ease",
          dependsOnMethods = "testStore")
    public void testAccountTransfer() {

        // Still on the store tab from testStore
        // Click the My Account icon in the store header
        clickElement(By.cssSelector("button[aria-label='My Account']"));
        try {
            Thread.sleep(800);
        } catch (InterruptedException ignored) {}

        // Click "My Account" link in the dropdown that appears
        clickElement(By.cssSelector("a[href*='account'], [data-t='my-account']"));
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}

        // Find the email
        WebElement emailElement = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//dt[normalize-space(text())='Email:']/following-sibling::dd[1]")));

        String displayedEmail = emailElement.getText().trim();

        Assert.assertEquals(displayedEmail, VALID_EMAIL,
                "Store account email should match the Crunchyroll login email");
        System.out.println("Account auto logged in from previous site. Email: " + displayedEmail);

        try {
            Thread.sleep(5000);
        } catch (InterruptedException ignored) {}

        // Close the store tab and return to the main CR tab
        driver.close();
        driver.switchTo().window(mainWindowHandle);
    }
}
