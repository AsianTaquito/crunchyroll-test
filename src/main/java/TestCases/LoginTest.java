package TestCases;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.Test;

public class LoginTest extends BaseTest {

    // Invalid credentials
    private static final String INVALID_EMAIL    = "invalid_user@example.com";
    private static final String INVALID_PASSWORD = "WrongPassword!";


    @Test(description = "TC-LGN-01: Login or signup buttons should be present")
    public void testButtons() {
        driver.get(BASE_URL);
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
        dismissBanners();

        boolean testLoginButton =
                isElementPresent(By.cssSelector("a[href*='login']")) ||
                isElementPresent(By.cssSelector("button[data-t='header-sign-in-btn']"));

        boolean testSignUpButton =
                isElementPresent(By.cssSelector("a[href*='signup']")) ||
                isElementPresent(By.cssSelector("a[href*='welcome']")) ||
                isElementPresent(By.cssSelector("button[data-t='header-start-trial-btn']")) ||
                isElementPresent(By.xpath("//*[contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'start free trial')]"));

        Assert.assertTrue(testLoginButton && testSignUpButton,
                "Login & sign up buttons should both be present");

        if (testLoginButton && testSignUpButton) {
            driver.findElement(By.cssSelector("a[href*='login']")).click();
            System.out.println("Log in & sign up buttons both present. Proceeding to login page.\n");
        }
    }


    @Test(description = "TC-LGN-02: Assure email/username & password input fields are present",
          dependsOnMethods = "testButtons")
    public void testInputFields() {
        boolean emailPresent =
                isElementPresent(By.cssSelector("input[type='email']")) ||
                isElementPresent(By.cssSelector("input[name='username']")) ||
                isElementPresent(By.cssSelector("input[name='email']")) ||
                isElementPresent(By.cssSelector("input[id*='email']")) ||
                isElementPresent(By.cssSelector("input[id*='username']"));

        boolean passwordPresent =
                isElementPresent(By.cssSelector("input[type='password']")) ||
                isElementPresent(By.cssSelector("input[name='password']")) ||
                isElementPresent(By.cssSelector("input[id*='password']"));

        Assert.assertTrue(emailPresent && passwordPresent,
                "Both username/email & password input fields should be present");
    }


    @Test(description = "TC-LGN-03: Assure forgot password link works",
          dependsOnMethods = "testButtons")
    public void testForgotPassword() {
        boolean forgotPasswordLinkPresent =
                isElementPresent(By.cssSelector("a[href*='forgot']")) ||
                isElementPresent(By.cssSelector("a[href*='reset']")) ||
                isElementPresent(By.cssSelector("a[href*='password']")) ||
                isElementPresent(By.xpath(
                    "//*[contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'forgot')" +
                    " or contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'reset')]"));

        Assert.assertTrue(forgotPasswordLinkPresent, "There should be a forgot password link");

        if (forgotPasswordLinkPresent) {
            driver.findElement(By.cssSelector("a[href*='forgot'], a[href*='reset']")).click();
            System.out.println("Forgot Password link present and clicked. Check email for reset instructions.\n");
        }
    }


    @Test(description = "TC-LGN-04: Assure system rejects wrong credentials")
    public void testInvalidLogin() {
        driver.get(BASE_URL + "/login");

        WebElement passwordField = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("input[type='password'], input[name='password']")));
        WebElement submitButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("button[type='submit']")));

        emailField.clear();
        emailField.sendKeys(INVALID_EMAIL);
        passwordField.clear();
        passwordField.sendKeys(INVALID_PASSWORD);
        submitButton.click();

        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}

        boolean errorDisplayed =
                isElementPresent(By.cssSelector(".error-message, [class*='error'], [data-t='error']")) ||
                isElementPresent(By.xpath(
                    "//*[contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'invalid')" +
                    " or contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'incorrect')" +
                    " or contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'wrong')]"));

        Assert.assertTrue(errorDisplayed,
                "An error message should be displayed for invalid credentials");
        System.out.println("Invalid credentials correctly rejected.\n");
    }


    @Test(description = "TC-LGN-05: Assure user can login with valid credentials")
    public void testValidLogin() {
        loginWithValidCredentials();

        boolean loginSuccessful =
                isElementPresent(By.cssSelector("[data-t='user-avatar'], [class*='user-menu'], [aria-label*='account']"));

        Assert.assertTrue(loginSuccessful, "User should be successfully logged in with valid credentials");
        System.out.println("Valid login successful. Current URL: " + driver.getCurrentUrl() + "\n");
    }
}