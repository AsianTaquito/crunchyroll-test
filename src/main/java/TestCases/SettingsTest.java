package TestCases;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;


//add indents and pauses for presentation purposes

public class SettingsTest extends BaseTest {

    // Locators
    // User menu dropdown button in the nav header
    private static final By USER_MENU_BTN = By.cssSelector(
            "[aria-label='User Menu'][data-t='header-tile']");

    // Settings link inside the user-menu dropdown
    private static final By SETTINGS_LINK = By.cssSelector(
            "a[href='/account/preferences'][role='menuitem']");

    // Subtitle/CC language dropdown trigger (on preferences page)
    private static final By SUBTITLE_DROPDOWN = By.cssSelector(
            "[data-t='subtitle-language-select'] [role='button']");

    // Custom dropdown option items
    private static final By DROPDOWN_OPTIONS = By.cssSelector(
            "[role='option']");

    // Account sub-nav: Email Notifications
    private static final By NAV_NOTIFICATIONS = By.cssSelector(
            "[data-t='account-nav-notifications']");

    // All Notifications toggle checkbox input
    private static final By TOGGLE_ALL_NOTIFS = By.cssSelector(
            "[data-t='toggle-all-notifications']");

    // Account sub-nav: Email
    private static final By NAV_EMAIL = By.cssSelector(
            "[data-t='account-nav-email']");

    // "Send email change link" button
    private static final By CHANGE_EMAIL_BTN = By.cssSelector(
            "[data-t='change-email-btn']");

    // Customer Support link
    private static final By CUSTOMER_SUPPORT_LINK = By.cssSelector(
            "[data-t='customer-support-link']");


    // Tests

    @Test(description = "TC-ST-01: Settings page loads correctly")
    public void testSettingsPageLoads() {

        // Open the user menu dropdown
        clickElement(USER_MENU_BTN);
        try {
            Thread.sleep(800);
        } catch (InterruptedException ignored) {}

        // Click the Settings link inside the dropdown
        clickElement(SETTINGS_LINK);

        wait.until(ExpectedConditions.urlContains("/account/preferences"));

        Assert.assertTrue(driver.getCurrentUrl().contains("/account/preferences"),
                "Settings page should be accessible");

        System.out.println("Settings page loaded. URL: " + driver.getCurrentUrl() + "\n");

        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {}
    }

    @Test(description = "TC-ST-02: User can change subtitle language preference",
          dependsOnMethods = "testSettingsPageLoads")
    public void testSubtitleLanguage() {

        // Click the Subtitles/CC Language dropdown trigger
        clickElement(SUBTITLE_DROPDOWN);
        try {
            Thread.sleep(600);
        } catch (InterruptedException ignored) {}

        // Find the English option and click it
        List<WebElement> options = wait.until(
                ExpectedConditions.presenceOfAllElementsLocatedBy(DROPDOWN_OPTIONS));

        WebElement englishOption = options.stream()
                .filter(opt -> opt.getText().trim().equalsIgnoreCase("English"))
                .findFirst()
                .orElse(null);

        Assert.assertNotNull(englishOption, "English subtitle option not found in dropdown");
        englishOption.click();
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ignored) {}

        System.out.println("Subtitle language set to English.\n");
    }

    @Test(description = "TC-ST-03: User can toggle notification settings",
          dependsOnMethods = "testSubtitleLanguage")
    public void testNotifSettings() {

        // Click the Email Notifications sub-nav link
        clickElement(NAV_NOTIFICATIONS);
        wait.until(ExpectedConditions.urlContains("/account/notifications"));

        Assert.assertTrue(driver.getCurrentUrl().contains("/account/notifications"),
                "Should be on the notifications settings page");

        // Find the All Notifications toggle input
        WebElement toggleInput = wait.until(
                ExpectedConditions.presenceOfElementLocated(TOGGLE_ALL_NOTIFS));

        boolean wasChecked = toggleInput.isSelected();

        // trigger the toggle
        WebElement toggleLabel = toggleInput.findElement(By.xpath("./.."));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", toggleLabel);
        try {
            Thread.sleep(800);
        } catch (InterruptedException ignored) {}

        // Restore original state
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", toggleLabel);
        try {
            Thread.sleep(500);
        } catch (InterruptedException ignored) {}

        String toggleState;
        if (wasChecked) {
            toggleState = "ON";
        } else {
            toggleState = "OFF";
        }

        System.out.println("Notification toggle was " + toggleState + " - toggled and restored.\n");

        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {}
    }

    @Test(description = "TC-ST-04: User should be able to change email info if needed",
          dependsOnMethods = "testNotifSettings")
    public void testChangeAccountInfo() {

        // Click the Email sub-nav link
        clickElement(NAV_EMAIL);
        wait.until(ExpectedConditions.urlContains("/account/email"));

        Assert.assertTrue(driver.getCurrentUrl().contains("/account/email"),
                "Should be on the email settings page");

        // Click the "Send email change link" button
        clickElement(CHANGE_EMAIL_BTN);
        try {
            Thread.sleep(9000); }
        catch (InterruptedException ignored) {}

        System.out.println("Change email link sent – check email for the confirmation link.\n");
    }

    @Test(description = "TC-ST-05: User can access customer support link",
          dependsOnMethods = "testChangeAccountInfo")
    public void testCustomerSupport() {

        String mainHandle = driver.getWindowHandle();

        // Click the Customer Support link (opens in a new tab)
        clickElement(CUSTOMER_SUPPORT_LINK);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {}

        // Switch to the new tab
        List<String> handles = new ArrayList<>(driver.getWindowHandles());
        Assert.assertTrue(handles.size() > 1,
                "Customer Support link did not open a new tab");

        handles.remove(mainHandle);
        driver.switchTo().window(handles.get(0));

        wait.until(ExpectedConditions.urlContains("help.crunchyroll.com"));
        Assert.assertTrue(driver.getCurrentUrl().contains("help.crunchyroll.com"),
                "Customer Support tab should open help.crunchyroll.com, got: " + driver.getCurrentUrl());

        System.out.println("Customer Support page opened: " + driver.getCurrentUrl() + "\n");

        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {}

        // Close the support tab and return to the main tab
        driver.close();
        driver.switchTo().window(mainHandle);
    }
}
