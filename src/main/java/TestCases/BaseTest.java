package TestCases;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * manages browser lifecycle for the entire suite
 * • LoginTest uses the single suite-level browser
 * • Every other test class gets its own fresh browser
 */

public class BaseTest {

    // Instance-level — each test class instance owns its own browser
    protected WebDriver driver;
    protected WebDriverWait wait;

    // Keeps the suite browser alive for LoginTest; closed by @AfterSuite
    private static WebDriver suiteDriver;
    private static WebDriverWait suiteWait;

    // Stagger parallel browser launches — each slot is 5 seconds apart
    private static final AtomicInteger launchSlot = new AtomicInteger(0);

    protected static final String BASE_URL       = "https://www.crunchyroll.com";
    protected static final String VALID_EMAIL    = "";
    protected static final String VALID_PASSWORD = "";
    private   static final String COOKIE_FILE    = "crunchyroll_session.cookies";

    // Suite-level setup (runs once — on the LoginTest instance)

    @BeforeSuite
    public synchronized void suiteSetUp() {
        if (suiteDriver != null) return;

        WebDriverManager.chromedriver().setup();

        Path profileDir = Paths.get(System.getProperty("user.dir"), ".chrome-test-profile");
        try { Files.createDirectories(profileDir); } catch (IOException ignored) {}
        cleanChromeLockFiles(profileDir);

        suiteDriver = buildDriver(profileDir);
        suiteWait   = new WebDriverWait(suiteDriver, Duration.ofSeconds(10));

        // Assign to this instance so LoginTest's tests can use driver/wait directly
        this.driver = suiteDriver;
        this.wait   = suiteWait;

        suiteDriver.get(BASE_URL);
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}

        if (new File(COOKIE_FILE).exists()) {
            injectCookiesInto(suiteDriver);
            suiteDriver.get(BASE_URL);
            try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
        } else {
            waitForCaptcha();
        }

        dismissBanners();
    }

    @AfterSuite
    public synchronized void suiteTearDown() {
        if (suiteDriver != null) {
            suiteDriver.quit();
            suiteDriver = null;
        }
    }

    // Per-class setup (runs before each test class)
    @BeforeClass
    public void classSetUp() {
        // LoginTest must always use the suite browser
        if (this.getClass().getName().equals("TestCases.LoginTest")) {
            this.driver = suiteDriver;
            this.wait   = suiteWait;
            System.out.println("[LoginTest] Using suite browser.");
            return;
        }

        // Parallel test classes: wait until LoginTest has written the cookie file, then open a fresh browser and restore the session.
        long deadline = System.currentTimeMillis() + 120_000; // up to 2 min
        while (!new File(COOKIE_FILE).exists() && System.currentTimeMillis() < deadline) {
            System.out.println("[" + this.getClass().getSimpleName() + "] Waiting for cookie file from LoginTest...");
            try { Thread.sleep(3000); } catch (InterruptedException ignored) {}
        }

        if (!new File(COOKIE_FILE).exists()) {
            System.out.println("[" + this.getClass().getSimpleName() + "] WARNING: No cookie file found — CAPTCHA may appear.");
        }

        WebDriverManager.chromedriver().setup();

        // Stagger browser launches
        int slot = launchSlot.getAndIncrement();
        if (slot > 0) {
            long delay = slot * 5000L;
            System.out.println("[" + this.getClass().getSimpleName() + "] Stagger delay: " + (delay / 1000) + "s (slot " + slot + ")");
            try { Thread.sleep(delay); } catch (InterruptedException ignored) {}
        }

        driver = buildDriver(null);
        wait   = new WebDriverWait(driver, Duration.ofSeconds(10));

        // Must navigate to the domain before cookies can be added
        driver.get(BASE_URL);
        try { Thread.sleep(3000); } catch (InterruptedException ignored) {}

        if (new File(COOKIE_FILE).exists()) {
            injectCookiesInto(driver);
            driver.get(BASE_URL); // reload so the injected session takes effect
            try { Thread.sleep(3000); } catch (InterruptedException ignored) {}
            handleErrorPage();  // redirect away from /premium/error if needed
            System.out.println("[" + this.getClass().getSimpleName() + "] Session restored via cookies.");
        }

        dismissBanners();
    }

    @AfterClass
    public void classTearDown() {
        // Don't close the suite browser — @AfterSuite handles that
        if (driver != null && driver != suiteDriver) {
            driver.quit();
            driver = null;
        }
    }

    
    // If the browser landed on the /premium/error page, redirect to BASE_URL
    protected void handleErrorPage() {
        String url = driver.getCurrentUrl();
        if (url == null || (!url.contains("/premium/error") && !url.contains("/error"))) return;

        System.out.println("[" + this.getClass().getSimpleName() + "] Error page detected (" + url + ") — redirecting to home...");
        driver.get(BASE_URL);
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
        System.out.println("[" + this.getClass().getSimpleName() + "] Redirected home. URL: " + driver.getCurrentUrl());
    }

    private static WebDriver buildDriver(Path profileDir) {
        ChromeOptions options = new ChromeOptions();
        if (profileDir != null) {
            options.addArguments("--user-data-dir=" + profileDir.toAbsolutePath());
            options.addArguments("--profile-directory=Default");
        }
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--disable-infobars");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.setExperimentalOption("excludeSwitches", List.of("enable-automation"));
        options.setExperimentalOption("useAutomationExtension", false);

        WebDriver d = new ChromeDriver(options);
        d.manage().window().maximize();
        ((JavascriptExecutor) d).executeScript(
                "Object.defineProperty(navigator, 'webdriver', {get: () => undefined})");
        return d;
    }

    // Chrome lock-file cleanup
    private static void cleanChromeLockFiles(Path userDataDir) {
        String[] rootLocks = {"SingletonLock", "SingletonCookie", "SingletonSocket", "lockfile"};
        for (String name : rootLocks) {
            try { Files.deleteIfExists(userDataDir.resolve(name)); } catch (IOException ignored) {}
        }
        Path defaultProfile = userDataDir.resolve("Default");
        for (String name : rootLocks) {
            try { Files.deleteIfExists(defaultProfile.resolve(name)); } catch (IOException ignored) {}
        }
        System.out.println("Chrome lock files cleaned from: " + userDataDir);
    }

    // Cookie helper
    private void waitForCaptcha() {
        System.out.println("══════════════════════════════════════════════════════");
        System.out.println(" No valid saved session – fresh start.");
        System.out.println(" If a CAPTCHA appears in the browser, solve it now.");
        System.out.println(" Cookies are saved automatically after the first login");
        System.out.println(" so every future run bypasses CAPTCHA completely.");
        System.out.println("══════════════════════════════════════════════════════");
        try { Thread.sleep(30_000); } catch (InterruptedException ignored) {}
    }

    private static void injectCookiesInto(WebDriver target) {
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
                    target.manage().addCookie(cb.build());
                } catch (Exception ignored) {}
            }
            System.out.println("Session cookies injected.");
        } catch (IOException e) {
            System.out.println("Could not read cookie file: " + e.getMessage());
        }
    }

    protected void saveSessionCookies() {
        String currentUrl = driver.getCurrentUrl();
        if (currentUrl.contains("/error") || currentUrl.contains("/premium")) {
            System.out.println("Skipping cookie save — current URL looks invalid: " + currentUrl);
            return;
        }
        try (PrintWriter pw = new PrintWriter(new FileWriter(COOKIE_FILE))) {
            Set<Cookie> cookies = driver.manage().getCookies();
            for (Cookie c : cookies) {
                pw.printf("%s\t%s\t%s\t%s\t%b%n",
                        c.getName(), c.getValue(),
                        c.getDomain() == null ? "" : c.getDomain(),
                        c.getPath()   == null ? "/" : c.getPath(),
                        c.isSecure());
            }
            System.out.println("Session saved (" + cookies.size() + " cookies) – future runs will skip CAPTCHA.");
        } catch (IOException e) {
            System.out.println("Could not save cookies: " + e.getMessage());
        }
    }

    // Shared helpers
    protected void dismissBanners() {
        try {
            WebElement btn = driver.findElement(
                    By.cssSelector("[data-t='grant-anonymous-consent-btn']"));
            if (btn.isDisplayed()) {
                btn.click();
                System.out.println("Dismissed consent banner.");
            }
        } catch (NoSuchElementException ignored) {}
    }

    protected boolean isElementPresent(By locator) {
        try { driver.findElement(locator); return true; }
        catch (NoSuchElementException e) { return false; }
    }

    protected void clickElement(By locator) {
        wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
    }

    protected void scrollToElement(WebElement element) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);
    }

    protected void scrollToBottom() {
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");
    }

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
        dismissBanners();
        saveSessionCookies();
    }

    // Shared locators
    protected static final By MY_PROFILE = By.xpath(
            "//button[@data-t='profile-button'][.//p[normalize-space()='Sebi']] | " +
            "//*[@data-profile-can-switch='true'][.//p[normalize-space()='Sebi']]");

    protected static final By SEARCH_ICON = By.cssSelector(
            "a[aria-label='Search'][href*='/search'], " +
            "[data-t='search-svg'], " +
            "a.erc-search-header-button-old, " +
            "a[href='/search'][aria-label='Search']");

    protected static final By LOGO = By.cssSelector(
            "a.erc-logo, a[href='/discover'][aria-label*='logo'], " +
            "[data-t='crunchyroll-horizontal-svg'], [data-t='crunchyroll-logo-only-svg']");
}
