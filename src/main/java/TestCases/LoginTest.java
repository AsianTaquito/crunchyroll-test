package TestCases;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.Test;

public class LoginTest extends BaseTest {

    private static final String INVALID_PASSWORD = "WrongPassword!";


    @Test(description = "TC-LGN-01: Login or signup buttons should be present")
    public void testButtons() {
        driver.get(BASE_URL);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {}
        dismissBanners();

        boolean testLoginButton = isElementPresent(By.cssSelector("button[data-t='button-login']"));

        boolean testSignUpButton = isElementPresent(By.cssSelector("button[data-t='button-premium']"));

        Assert.assertTrue(testLoginButton && testSignUpButton,
                "Login & sign up buttons should both be present");

        driver.findElement(By.cssSelector("a.cr-login-button")).click();
        System.out.println("Log in & sign up buttons both present. Proceeding to login page.\n");
    }


    @Test(description = "TC-LGN-02: Assure email/username & password input fields are present",
          dependsOnMethods = "testButtons")
    public void testInputFields() {

        boolean emailPresent = isElementPresent(By.cssSelector("input[name='email']"));


        boolean passwordPresent = isElementPresent(By.cssSelector("input[name='password']"));

        Assert.assertTrue(emailPresent && passwordPresent,
                "Both email & password input fields should be present");
    }


    @Test(description = "TC-LGN-03: Assure forgot password link works",
          dependsOnMethods = "testButtons")
    public void testForgotPassword() {

        boolean forgotPasswordLinkPresent = isElementPresent(By.cssSelector("a[data-t='forgot-password-link']"));

        Assert.assertTrue(forgotPasswordLinkPresent, "There should be a forgot password link");

        driver.findElement(By.cssSelector("a[data-t='forgot-password-link']")).click();
        System.out.println("Forgot Password link present and clicked. Check email for reset instructions.\n");
    }


    @Test(description = "TC-LGN-04: Assure system rejects wrong credentials")
    public void testInvalidLogin() {

        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("input[name='email']")));


        WebElement passwordField = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("input[name='password']")));

        emailField.clear();
        emailField.sendKeys(VALID_EMAIL);
        passwordField.clear();
        passwordField.sendKeys(INVALID_PASSWORD);



        WebElement submitButton = driver.findElement(By.cssSelector("button[data-t='login-button']"));
        wait.until(ExpectedConditions.attributeToBe(submitButton, "aria-disabled", "false"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", submitButton);

        WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[data-t='flash-message']")));

        Assert.assertTrue(errorMsg.isDisplayed(),
                "An error message should be displayed for invalid credentials");
        System.out.println("Invalid credentials correctly rejected.\n");
    }


    @Test(description = "TC-LGN-05: Assure user can login with valid credentials")
    public void testValidLogin() {
        loginWithValidCredentials();

        // After login, confirm profile selection page appears
        boolean profilePageShown = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-t='profile-button']"))
        ).isDisplayed();

        Assert.assertTrue(profilePageShown,
                "Profile selection page should appear after a successful login");
        System.out.println("Valid login successful — profile selection page reached. URL: "
                + driver.getCurrentUrl() + "\n");
    }
}