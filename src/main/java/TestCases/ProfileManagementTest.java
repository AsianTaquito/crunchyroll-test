package TestCases;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class    ProfileManagementTest extends BaseTest {


    @Test(description = "TC-PM-01: Assure user can log out and log back in")
    public void testLogout(){

        driver.findElement(By.cssSelector("a.logout-link")).click();
        try {
            Thread.sleep(4000);
        } catch (InterruptedException ignored) {}

        String url = driver.getCurrentUrl();
        Assert.assertTrue(url.contains("/login"),
                "Should be back at login page (or home as guest) after logout, got: " + url);
        System.out.println("Logout works. Now at: " + url);

        loginWithValidCredentials();
        System.out.println("Successfully logged back in.\n");

    }


    @Test(description = "TC-PM-02: Assure user can add new user profile to account",
        dependsOnMethods = "testLogout")
    public void addNewUser() {

        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {}
        dismissBanners();

        // Navigate to profile management page
        clickElement(By.cssSelector("[data-t='edit-profile-button']"));
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}

        // Click add new profile button
        clickElement(By.cssSelector("a[data-t='add-profile-button']"));
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {}

        // Enter profile name
        WebElement profileNameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("input[name='profile-name']")));
        profileNameInput.clear();
        profileNameInput.sendKeys("TestUser");

        // Save — returns to profile selection page
        clickElement(By.cssSelector("[data-t='save-profile-btn']"));
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ignored) {}
        System.out.println("New profile created successfully.\n");

        // Dismiss the "Cool, Thanks!" modal that appears after profile creation
        if (isElementPresent(By.cssSelector("[data-t='pin-promotion-modal-btn']"))) {
            driver.findElement(By.cssSelector("[data-t='pin-promotion-modal-btn']")).click();
            try {
                Thread.sleep(4000);
            } catch (InterruptedException ignored) {}
        }
    }

    @Test(description = "TC-PM-03: Assure user can edit existing user profile",
          dependsOnMethods = "addNewUser")
    public void editUserProfile() {

        // Navigate to a known home state first to avoid whatever was making it get stuck
        driver.get(BASE_URL + "/discover");
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
        dismissBanners();

        // Navigate to profile management page
        clickElement(By.cssSelector("[data-t='edit-profile-button']"));
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}

        // Click the TestUser profile to open its edit page
        clickElement(By.xpath(
                "//button[@data-t='profile-button']" +
                "[.//p[@data-t='profile-name' and normalize-space()='TestUser']]"));
        try {
            Thread.sleep(1500);
        } catch (InterruptedException ignored) {}

        // Click the avatar area to open the picker
        clickElement(By.cssSelector(".avatar-edit-overlay"));
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {}

        // Select the black & white Crunchyroll logo avatar (index 3)
        wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("[data-card-index='3'] .avatar-option"))).click();
        try {
            Thread.sleep(500);
        } catch (InterruptedException ignored) {}

        // Save the avatar selection
        if (isElementPresent(By.cssSelector("[data-t='save-avatar-btn']"))) {
            clickElement(By.cssSelector("[data-t='save-avatar-btn']"));
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {}
        }

        // Save profile — returns to profile selection page
        clickElement(By.cssSelector("[data-t='save-profile-btn']"));
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {}
        System.out.println("Profile pic updated and saved – back at profile selection page.\n");
    }


    @Test(description = "TC-PM-04: Assure user can delete user profile",
          dependsOnMethods = "editUserProfile")
    public void deleteUserProfile() {
        /*try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {}

        // Navigate to profile management page
        clickElement(By.cssSelector("[data-t='edit-profile-button']"));
        try { Thread.sleep(1500); } catch (InterruptedException ignored) {}*/

        // Click the TestUser profile to open its edit page
        clickElement(By.xpath(
                "//button[@data-t='profile-button']" +
                "[.//p[@data-t='profile-name' and normalize-space()='TestUser']]"));
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {}

        // Delete the profile
        clickElement(By.cssSelector("[data-t='delete-profile-btn']"));

        // Confirm deletion
        clickElement(By.cssSelector("[data-t='confirm-btn']"));
        try {
            Thread.sleep(1500);
        } catch (InterruptedException ignored) {}
        System.out.println("Test profile deleted – test passed.\n");
    }


    @Test(description = "TC-PM-05: Assure user can choose their own profile",
          dependsOnMethods = "deleteUserProfile")
    public void chooseProfile() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {}

        // Click on the Sebi profile access home page for next test
        WebElement sebiProfile = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[@data-t='profile-button']" +
                         "[.//p[@data-t='profile-name' and normalize-space()='Sebi']]")));
        sebiProfile.click();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {}
        dismissBanners();
        System.out.println("Switched to Sebi profile – test passed. URL: "
                + driver.getCurrentUrl() + "\n");
    }
}
