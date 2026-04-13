package TestCases;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.Set;

/**
 * BaseTest — owns the single shared browser session for the entire suite.
 *
 * Every test class extends this so that:
 *  • one browser opens at suite start and closes at suite end
 *  • driver / wait / helpers / credentials are available to all tests
 *  • no test class needs its own WebDriver setup or teardown
 */
public class BaseTest {

    protected static WebDriver driver;
    protected static WebDriverWait wait;

    protected static final String BASE_URL       = "https://www.crunchyroll.com";

    // Shared credentials
    // Replace with real values before running the suite
    protected static final String VALID_EMAIL    = "mouye.martin@gmail.com";
    protected static final String VALID_PASSWORD = "Alfonse-6952!";

    // Saved between runs so the CAPTCHA only ever has to be solved once
    private static final String COOKIE_FILE = "crunchyroll_session.cookies";


    @BeforeSuite
    public synchronized void suiteSetUp() {
        if (driver != null) return;  // guard against multiple calls from inherited subclasses

        WebDriverManager.chromedriver().setup();

        // Dedicated persistent profile for automation (regular tab, not incognito)
        // This keeps site/session state between runs and reduces CAPTCHA churn.
        Path profileDir = Paths.get(System.getProperty("user.dir"), ".chrome-test-profile");
        try { Files.createDirectories(profileDir); } catch (IOException ignored) {}

        // Remove Chrome lock files left over from a previous crashed/killed session.
        // Without this, ChromeDriver fails with "Chrome instance exited" when the
        // user-data-dir is still marked as in-use.
        cleanChromeLockFiles(profileDir);

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--user-data-dir=" + profileDir.toAbsolutePath());
        options.addArguments("--profile-directory=Default");
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--disable-infobars");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.setExperimentalOption("excludeSwitches", List.of("enable-automation"));
        options.setExperimentalOption("useAutomationExtension", false);

        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        ((JavascriptExecutor) driver).executeScript(
                "Object.defineProperty(navigator, 'webdriver', {get: () => undefined})");

        wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // Navigate to the domain first — required before cookies can be added
        driver.get(BASE_URL);
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}

        if (new File(COOKIE_FILE).exists()) {
            injectSavedCookies();
            driver.get(BASE_URL);                         // explicit nav — discards any /premium redirect
            try { Thread.sleep(2000); } catch (InterruptedException ignored) {}

            String landedUrl = driver.getCurrentUrl();

            if (landedUrl.contains("/premium/error") || landedUrl.contains("/error")) {
                // Known redirect — click the "Return Home" button Crunchyroll provides
                System.out.println("Landed on error page (" + landedUrl + ") – clicking Return Home...");
                try {
                    driver.findElement(By.cssSelector("[data-t='return-home-button']")).click();
                    try { Thread.sleep(3000); } catch (InterruptedException ignored) {}
                    System.out.println("Now at: " + driver.getCurrentUrl());
                } catch (NoSuchElementException e) {
                    // Button not found — fall back to direct navigation
                    System.out.println("Return Home button not found – navigating directly.");
                    driver.get(BASE_URL);
                }
                // A CAPTCHA may appear after returning home — give a short window to solve it
                System.out.println("If a CAPTCHA appears, solve it now (15 seconds)...");
                try { Thread.sleep(15_000); } catch (InterruptedException ignored) {}
            } else {
                System.out.println("Saved session restored – CAPTCHA bypassed.");
            }
        } else {
            waitForCaptcha();
        }

        dismissBanners();   // clear the phishing-awareness banner before any test runs
    }

    @AfterSuite
    public synchronized void suiteTearDown() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }

    // Chrome lock-file cleanup

    /**
     * Deletes stale Chrome lock files from the given user-data-dir.
     * Chrome writes these files when a session starts and removes them on clean exit.
     * If the JVM (or Chrome) is killed without a clean shutdown the files remain and
     * prevent the next ChromeDriver session from starting ("Chrome instance exited").
     */
    private static void cleanChromeLockFiles(Path userDataDir) {
        // Lock files that live directly in the user-data-dir
        String[] rootLocks = {"SingletonLock", "SingletonCookie", "SingletonSocket", "lockfile"};
        for (String name : rootLocks) {
            Path f = userDataDir.resolve(name);
            try { Files.deleteIfExists(f); } catch (IOException ignored) {}
        }
        // Also check inside the Default profile sub-directory
        Path defaultProfile = userDataDir.resolve("Default");
        for (String name : rootLocks) {
            Path f = defaultProfile.resolve(name);
            try { Files.deleteIfExists(f); } catch (IOException ignored) {}
        }
        System.out.println("Chrome lock files cleaned from: " + userDataDir);
    }

    // ── Cookie helpers ────────────────────────────────────────────────────────

    /** Prints CAPTCHA instructions and waits 30 s for manual solving on first/fresh runs. */
    private void waitForCaptcha() {
        System.out.println("══════════════════════════════════════════════════════");
        System.out.println(" No valid saved session – fresh start.");
        System.out.println(" If a CAPTCHA appears in the browser, solve it now.");
        System.out.println(" Cookies are saved automatically after the first login");
        System.out.println(" so every future run bypasses CAPTCHA completely.");
        System.out.println("══════════════════════════════════════════════════════");
        try { Thread.sleep(30_000); } catch (InterruptedException ignored) {}
    }

    /** Reads name/value/domain/path/secure from the cookie file and injects them. */
    private void injectSavedCookies() {
        try (BufferedReader br = new BufferedReader(new FileReader(COOKIE_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split("\t", 5);
                if (p.length < 2) continue;
                try {
                    Cookie.Builder cb = new Cookie.Builder(p[0], p[1]);
                    if (p.length > 2 && !p[2].isEmpty()) cb.domain(p[2]);
                    if (p.length > 3 && !p[3].isEmpty()) cb.path(p[3]);
                    if (p.length > 4) cb.isSecure(Boolean.parseBoolean(p[4]));
                    driver.manage().addCookie(cb.build());
                } catch (Exception ignored) {}
            }
            System.out.println("Session cookies injected.");
        } catch (IOException e) {
            System.out.println("Could not read cookie file: " + e.getMessage());
        }
    }

    /**
     * Saves all current Crunchyroll cookies to disk.
     * Called automatically after every successful login so future runs
     * can skip CAPTCHA by injecting the saved session.
     */
    protected static void saveSessionCookies() {
        // Don't save if we're on an error or premium page — those cookies cause the redirect loop
        String currentUrl = driver.getCurrentUrl();
        if (currentUrl.contains("/error") || currentUrl.contains("/premium")) {
            System.out.println("Skipping cookie save — current URL looks invalid: " + currentUrl);
            return;
        }
        try (PrintWriter pw = new PrintWriter(new FileWriter(COOKIE_FILE))) {
            Set<Cookie> cookies = driver.manage().getCookies();
            for (Cookie c : cookies) {
                pw.printf("%s\t%s\t%s\t%s\t%b%n",
                        c.getName(),
                        c.getValue(),
                        c.getDomain()  == null ? "" : c.getDomain(),
                        c.getPath()    == null ? "/" : c.getPath(),
                        c.isSecure());
            }
            System.out.println("Session saved (" + cookies.size() + " cookies) – future runs will skip CAPTCHA.");
        } catch (IOException e) {
            System.out.println("Could not save cookies: " + e.getMessage());
        }
    }

    // ── Shared helpers ────────────────────────────────────────────────────────

    //Dismisses the phishing-awareness consent banner ("Continue" button) if present.
    protected void dismissBanners() {

        try {
            WebElement btn = driver.findElement(
                    By.cssSelector("[data-t='grant-anonymous-consent-btn']"));
            if (btn.isDisplayed()) {
                btn.click();
                System.out.println("Dismissed consent banner.");
            }
        } catch (NoSuchElementException ignored) { }
    }

    /**
     * Dismisses the OneTrust cookie-consent popup if present.
     * This banner can block clicks on page elements underneath it.
     */
    protected void dismissCookieConsent() {
        try {
            WebElement btn = driver.findElement(By.cssSelector(
                    "#onetrust-accept-btn-handler, " +
                    "[class*='onetrust-accept'], " +
                    "button[id*='accept-recommended'], " +
                    "#accept-recommended-btn-handler"));
            if (btn.isDisplayed()) {
                btn.click();
                System.out.println("Dismissed OneTrust cookie consent.");
                try { Thread.sleep(500); } catch (InterruptedException ignored) {}
            }
        } catch (NoSuchElementException ignored) { }
    }

    /** Returns true if at least one element matching the locator exists in the DOM. */
    protected boolean isElementPresent(By locator) {
        try {
            driver.findElement(locator);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    /* Waits for an element to be clickable, then clicks it. */
    protected void clickElement(By locator) {
        wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
    }

    /* Scrolls an element into view via JavaScript. */
    protected void scrollToElement(WebElement element) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);
    }

    /* Scrolls to the very bottom of the page. */
    protected void scrollToBottom() {
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");
    }

    /**
     * Logs in and saves session cookies.
     * Called by LoginTest.testValidLogin (establishes the suite session)
     * and ProfileManagementTest.testLogout (re-establishes after logout).
     */
    protected void loginWithValidCredentials() {
        driver.get(BASE_URL + "/login");
        WebElement email = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("input[type='email'], input[name='username'], input[name='email']")));
        email.clear();
        email.sendKeys(VALID_EMAIL);
        driver.findElement(By.cssSelector("input[type='password'], input[name='password']"))
              .sendKeys(VALID_PASSWORD);
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("/login")));
        dismissBanners();   // banner can reappear after login

        // Persist cookies so the next run can skip CAPTCHA
        saveSessionCookies();
    }
}
