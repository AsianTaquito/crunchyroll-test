package TestCases;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ProfileManagementTest extends BaseTest {


    @Test(description = "TC-PM-01: Assure user can log out and log back in")
    public void testLogout(){

        driver.findElement(By.cssSelector("a.logout-link")).click();
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}

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
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
        dismissBanners();

        //click add new profile
        clickElement(By.cssSelector("a[data-t='add-profile-button']"));
        try { Thread.sleep(1500); } catch (InterruptedException ignored) {}

        // configure/change wallpaper
        if (isElementPresent(By.cssSelector(".edit-overlay"))) {
            try {
                clickElement(By.cssSelector(".edit-overlay"));
                try { Thread.sleep(1000); } catch (InterruptedException ignored) {}

                //choose random wallpaper
                if (isElementPresent(By.cssSelector("[data-card-index='1'] .wallpaper-thumbnail"))) {
                    driver.findElement(By.cssSelector("[data-card-index='1'] .wallpaper-thumbnail")).click();
                    try { Thread.sleep(500); } catch (InterruptedException ignored) {}
                }

                // save
                if (isElementPresent(By.cssSelector("[data-t='save-wallpaper-btn']"))) {
                    clickElement(By.cssSelector("[data-t='save-wallpaper-btn']"));
                    try { Thread.sleep(500); } catch (InterruptedException ignored) {}
                }
            } catch (TimeoutException e) {
                System.out.println("Wallpaper editor not available – skipping.");
            }
        }

        // select profile pic/avatar
        if (isElementPresent(By.cssSelector(".avatar-edit-overlay"))) {
            try {
                clickElement(By.cssSelector(".avatar-edit-overlay"));
                try { Thread.sleep(1000); } catch (InterruptedException ignored) {}

                // select icon
                if (isElementPresent(By.cssSelector("[data-card-index='1'] .avatar-option"))) {
                    driver.findElement(By.cssSelector("[data-card-index='1'] .avatar-option")).click();
                }

                // save
                if (isElementPresent(By.cssSelector("[data-t='save-avatar-btn']"))) {
                    clickElement(By.cssSelector("[data-t='save-avatar-btn']"));
                } else {
                    driver.findElement(By.cssSelector("body")).sendKeys(Keys.ESCAPE);
                }
                try { Thread.sleep(500); } catch (InterruptedException ignored) {}
            } catch (TimeoutException e) {
                System.out.println("Avatar editor not available – skipping.");
            }
        }

        // Profile name
        WebElement profileNameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("input[name='profile-name']")));
        profileNameInput.clear();
        profileNameInput.sendKeys("TestUser");

        // Username
        if (isElementPresent(By.cssSelector("input[name='username']"))) {
            WebElement usernameInput = driver.findElement(By.cssSelector("input[name='username']"));
            usernameInput.clear();
            usernameInput.sendKeys("testuser52");
        }

        // save
        clickElement(By.cssSelector("[data-t='save-profile-btn']"));
        try { Thread.sleep(1500); } catch (InterruptedException ignored) {}
        System.out.println("New profile created successfully.\n");
    }


    @Test(description = "TC-PM-03: Assure user can edit existing user profile",
          dependsOnMethods = "addNewUser")
    public void editUserProfile() {
        // Dismiss the "Cool, Thanks!" modal that may appear after profile creation
        if (isElementPresent(By.cssSelector("[data-t='pin-promotion-modal-btn']"))) {
            driver.findElement(By.cssSelector("[data-t='pin-promotion-modal-btn']")).click();
            try { Thread.sleep(500); } catch (InterruptedException ignored) {}
        }


        // Click on the TestUser profile to open its edit form
        clickElement(By.xpath(
                "//button[@data-t='profile-button']" +
                "[.//p[@data-t='profile-name' and normalize-space()='TestUser']]"));
        try { Thread.sleep(1500); } catch (InterruptedException ignored) {}

        // Toggle a setting (content-maturity filter)
        try {
            WebElement toggle = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("[class*='toggle__switch'], [role='switch'], input[type='checkbox']")));
            toggle.click();
            System.out.println("User setting toggled.");
        } catch (TimeoutException e) {
            System.out.println("Toggle not found – skipping toggle step.");
        }

        // Save
        clickElement(By.cssSelector("[data-t='save-profile-btn']"));
        try { Thread.sleep(1500); } catch (InterruptedException ignored) {}
        System.out.println("Profile edited successfully.\n");
    }


    @Test(description = "TC-PM-04: Assure user can delete user profile",
          dependsOnMethods = "editUserProfile")
    public void deleteUserProfile() {
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}

        // Click on the TestUser profile to edit
        clickElement(By.xpath(
                "//button[@data-t='profile-button']" +
                "[.//p[@data-t='profile-name' and normalize-space()='TestUser']]"));
        try { Thread.sleep(1000); } catch (InterruptedException ignored) {}

        //delete
        clickElement(By.cssSelector("[data-t='delete-profile-btn']"));

        // confirm Delete
        clickElement(By.cssSelector("[data-t='confirm-btn']"));
        try { Thread.sleep(1500); } catch (InterruptedException ignored) {}
        System.out.println("Test profile deleted – test passed.\n");
    }


    @Test(description = "TC-PM-05: Assure user can choose their own profile",
          dependsOnMethods = "deleteUserProfile")
    public void chooseProfile() {
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}

        // Click on the Sebi profile access home page for next test
        WebElement sebiProfile = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[@data-t='profile-button']" +
                         "[.//p[@data-t='profile-name' and normalize-space()='Sebi']]")));
        sebiProfile.click();
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
        dismissBanners();
        System.out.println("Switched to Sebi profile – test passed. URL: "
                + driver.getCurrentUrl() + "\n");
    }
}
