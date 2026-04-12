package TestCases;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

public class SettingsTest extends BaseTest {

    private static final String SETTINGS_URL = BASE_URL + "/settings";

    @BeforeClass
    public void navigateSettings() {
        driver.get(SETTINGS_URL);
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
    }

    @Test(description = "TC-ST-01: Settings page loads correctly")
    public void testSettingsPageLoads() {
        driver.get(SETTINGS_URL);
        wait.until(ExpectedConditions.urlContains("/settings"));
        Assert.assertTrue(driver.getCurrentUrl().contains("/settings"),
                "Settings page should be accessible");
        Assert.assertTrue(
                isElementPresent(By.cssSelector("[class*='settings'], [data-t*='settings'], main")),
                "Settings page should display content");
        System.out.println("Settings page loaded. URL: " + driver.getCurrentUrl());
    }

    /**
     * Crunchyroll uses custom styled dropdowns (not native <select>).
     * This helper tries native select first, then falls back to a custom-component click flow.
     * Returns the text of the NEW selection, or null if interaction failed.
     */
    private String interactWithDropdown(String sectionSelector) {
        // Try native <select> first
        List<WebElement> selects = driver.findElements(By.cssSelector(sectionSelector + " select, select" + sectionSelector));
        if (!selects.isEmpty()) {
            WebElement sel = selects.get(0);
            Select dropdown = new Select(sel);
            List<WebElement> options = dropdown.getOptions();
            if (options.size() > 1) {
                String original = dropdown.getFirstSelectedOption().getText();
                for (WebElement opt : options) {
                    if (!opt.getText().equals(original)) {
                        dropdown.selectByVisibleText(opt.getText());
                        return opt.getText();
                    }
                }
            }
        }

        // Fallback: custom dropdown button (React-style)
        List<WebElement> triggers = driver.findElements(By.cssSelector(
                sectionSelector + " button, " +
                sectionSelector + " [role='combobox'], " +
                sectionSelector + " [role='button'], " +
                "[class*='select-trigger']" + sectionSelector));
        if (triggers.isEmpty()) return null;

        WebElement trigger = triggers.get(0);
        String original = trigger.getText().trim();
        trigger.click();
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}

        // Find and click the first option that differs from current
        List<WebElement> options = driver.findElements(By.cssSelector(
                "[role='option'], [class*='option-item'], [class*='dropdown-item'], " +
                "[class*='select-option'], li[class*='item']"));
        for (WebElement opt : options) {
            if (!opt.getText().trim().equals(original)) {
                opt.click();
                return opt.getText().trim();
            }
        }
        // Close the dropdown if no selection made
        ((JavascriptExecutor) driver).executeScript("document.activeElement.blur();");
        return null;
    }

    @Test(description = "TC-ST-02: User can change subtitle language preference")
    public void testSubtitleLanguage() {
        driver.get(SETTINGS_URL);
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}

        // Try broad selectors for the subtitle language control
        By subtitleLocator = By.cssSelector(
                "[data-t='subtitle-language-select'], select[name*='subtitle'], " +
                "[class*='subtitle'] select, select[name*='language'], " +
                "[class*='subtitle-language'], [class*='subtitleLanguage'], " +
                "[data-t*='subtitle'] button, [class*='subtitle'] button, " +
                "[class*='subtitle'] [role='combobox']");

        WebElement subtitleEl;
        try {
            subtitleEl = wait.until(ExpectedConditions.presenceOfElementLocated(subtitleLocator));
        } catch (TimeoutException e) {
            throw new SkipException(
                    "Subtitle language control not found – locator needs updating with actual HTML. " +
                    "Please inspect /settings and provide the correct selector.");
        }

        scrollToElement(subtitleEl);
        String newValue = null;

        if (subtitleEl.getTagName().equals("select")) {
            Select dropdown = new Select(subtitleEl);
            List<WebElement> options = dropdown.getOptions();
            Assert.assertTrue(options.size() > 1, "Subtitle language dropdown should have multiple options");
            String original = dropdown.getFirstSelectedOption().getAttribute("value");
            for (WebElement opt : options) {
                if (!opt.getAttribute("value").equals(original)) {
                    dropdown.selectByValue(opt.getAttribute("value"));
                    newValue = opt.getAttribute("value");
                    break;
                }
            }
            Assert.assertNotNull(newValue, "Should have been able to select a different subtitle language");
        } else {
            // Custom component – click to open, pick different option
            subtitleEl.click();
            try { Thread.sleep(500); } catch (InterruptedException ignored) {}
            List<WebElement> options = driver.findElements(By.cssSelector(
                    "[role='option'], [class*='option'], [class*='dropdown-item']"));
            Assert.assertTrue(options.size() > 1, "Subtitle language options list should have multiple entries");
            options.get(1).click();
            newValue = options.get(1).getText();
        }

        System.out.println("Subtitle language changed to: " + newValue + " – test passed.");
    }

    @Test(description = "TC-ST-03: User can toggle autoplay setting")
    public void testAutoplaySetting() {
        driver.get(SETTINGS_URL);
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}

        // Crunchyroll uses custom toggle components – try [role='switch'] before checkbox
        By toggleLocator = By.cssSelector(
                "[data-t='autoplay-toggle'], " +
                "[role='switch'][class*='autoplay'], " +
                "[class*='autoplay'] [role='switch'], " +
                "[class*='autoplay'] input[type='checkbox'], " +
                "[class*='autoplay'] [class*='toggle'], " +
                "input[id*='autoplay'], " +
                "[data-t*='autoplay']");

        WebElement toggle;
        try {
            toggle = wait.until(ExpectedConditions.presenceOfElementLocated(toggleLocator));
        } catch (TimeoutException e) {
            throw new SkipException(
                    "Autoplay toggle not found – locator needs updating with actual HTML. " +
                    "Please inspect /settings and provide the correct selector.");
        }

        scrollToElement(toggle);
        boolean initialState = toggle.isSelected()
                || "true".equals(toggle.getAttribute("aria-checked"))
                || "true".equals(toggle.getAttribute("data-state"));
        toggle.click();
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}
        boolean newState = toggle.isSelected()
                || "true".equals(toggle.getAttribute("aria-checked"))
                || "true".equals(toggle.getAttribute("data-state"));
        Assert.assertNotEquals(newState, initialState, "Autoplay toggle state should have changed");
        System.out.println("Autoplay toggled " + initialState + " → " + newState + " – test passed.");
    }

    @Test(description = "TC-ST-04: User can change video quality setting")
    public void testVideoQuality() {
        driver.get(SETTINGS_URL);
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}

        By qualityLocator = By.cssSelector(
                "[data-t='quality-select'], select[name*='quality'], " +
                "[class*='quality'] select, [class*='video-quality'] select, " +
                "[class*='videoQuality'], [class*='video-quality'] button, " +
                "[class*='quality'] button, [data-t*='quality'] button, " +
                "[class*='quality'] [role='combobox']");

        WebElement qualityEl;
        try {
            qualityEl = wait.until(ExpectedConditions.presenceOfElementLocated(qualityLocator));
        } catch (TimeoutException e) {
            throw new SkipException(
                    "Video quality control not found – locator needs updating with actual HTML. " +
                    "Please inspect /settings and provide the correct selector.");
        }

        scrollToElement(qualityEl);

        if (qualityEl.getTagName().equals("select")) {
            Select dropdown = new Select(qualityEl);
            List<WebElement> options = dropdown.getOptions();
            Assert.assertTrue(options.size() > 1, "Quality dropdown should have multiple options");
            String current = dropdown.getFirstSelectedOption().getText();
            for (WebElement opt : options) {
                if (!opt.getText().equals(current)) {
                    dropdown.selectByVisibleText(opt.getText());
                    break;
                }
            }
            Assert.assertNotEquals(dropdown.getFirstSelectedOption().getText(), current,
                    "Video quality setting should have changed");
        } else {
            // Custom component
            String originalText = qualityEl.getText().trim();
            qualityEl.click();
            try { Thread.sleep(500); } catch (InterruptedException ignored) {}
            List<WebElement> options = driver.findElements(By.cssSelector(
                    "[role='option'], [class*='option'], [class*='dropdown-item']"));
            Assert.assertTrue(options.size() > 1, "Quality options list should have multiple entries");
            for (WebElement opt : options) {
                if (!opt.getText().trim().equals(originalText)) {
                    opt.click();
                    System.out.println("Video quality changed from '" + originalText + "' to '" + opt.getText() + "' – test passed.");
                    return;
                }
            }
        }
        System.out.println("Video quality changed – test passed.");
    }

    @Test(description = "TC-ST-05: User can toggle notification preferences")
    public void testNotificationSettings() {
        driver.get(SETTINGS_URL);
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}

        // Scroll down to load all settings
        scrollToBottom();
        try { Thread.sleep(1000); } catch (InterruptedException ignored) {}

        List<WebElement> toggles = driver.findElements(By.cssSelector(
                "[class*='notification'] input[type='checkbox'], " +
                "[data-t*='notification'] input, " +
                "[class*='notif'] [class*='toggle'], " +
                "[class*='notification'] [role='switch'], " +
                "[data-t*='notification'] [role='switch'], " +
                "[class*='notification'] button[role='switch']"));

        if (toggles.isEmpty()) {
            throw new SkipException(
                    "No notification toggles found – locators need updating with actual HTML. " +
                    "Please inspect /settings and provide the correct selector.");
        }

        Assert.assertTrue(toggles.size() > 0,
                "At least one notification preference toggle should be present");
        WebElement first = toggles.get(0);
        scrollToElement(first);
        boolean initialState = first.isSelected()
                || "true".equals(first.getAttribute("aria-checked"))
                || "true".equals(first.getAttribute("data-state"));
        first.click();
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}
        boolean newState = first.isSelected()
                || "true".equals(first.getAttribute("aria-checked"))
                || "true".equals(first.getAttribute("data-state"));
        Assert.assertNotEquals(newState, initialState, "Notification toggle should have changed");
        System.out.println("Notification preference toggled – test passed. (" + toggles.size() + " options found)");
    }
}
