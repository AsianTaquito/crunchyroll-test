package TestCases;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.Test;

public class LoginTest extends BaseTest {

    private static final String INVALID_EMAIL    = "fake.user.test123@nowhere-invalid.com";
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

        System.out.println("Log in & sign up buttons both present. Proceeding to login page.\n");
    }


    @Test(description = "TC-LGN-02: Assure email/username & password input fields are present",
          dependsOnMethods = "testButtons")
    public void testInputFields() {
        driver.findElement(By.cssSelector("a.cr-login-button")).click();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {}

        boolean emailPresent = isElementPresent(By.cssSelector("input[name='email']"));
        boolean passwordPresent = isElementPresent(By.cssSelector("input[name='password']"));

        Assert.assertTrue(emailPresent && passwordPresent,
                "Both email & password input fields should be present");
        if(emailPresent && passwordPresent) {
            System.out.println("Email & password input fields are present\n");
        }else{
            System.out.println("either email or password not detected\n");
        }
    }


    @Test(description = "TC-LGN-03: Assure forgot password link is present",
          dependsOnMethods = "testInputFields")
    public void testForgotPassword() {

        boolean forgotPasswordLinkPresent = isElementPresent(By.cssSelector("a[data-t='forgot-password-link']"));

        Assert.assertTrue(forgotPasswordLinkPresent, "Forgot password link should be visible on the login page");

        System.out.println("Forgot password link is present on the login page.\n");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {}
    }


    @Test(description = "TC-LGN-04: Assure system rejects wrong credentials",
    dependsOnMethods = "testForgotPassword")
    public void testInvalidLogin() {

        //enter email
        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("input[type='email'], input[name='username'], input[name='email']")));
        emailField.clear();
        emailField.sendKeys(INVALID_EMAIL);

        //enter purposely incorrect password
        WebElement passwordField = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("input[name='password'], input[type='password']")));
        passwordField.clear();
        passwordField.sendKeys(INVALID_PASSWORD);


        driver.findElement(By.cssSelector("button[type='submit']")).click();

        try {
            Thread.sleep(4000);
        } catch (InterruptedException ignored) {}

        Assert.assertTrue(driver.getCurrentUrl().contains("/login"),
                "Should remain on the login page after submitting invalid credentials");

        System.out.println("Invalid credentials correctly rejected — still on login page.\n");
    }


    @Test(description = "TC-LGN-05: Assure user can login with valid credentials",
    dependsOnMethods = "testInvalidLogin")
    public void testValidLogin() {
        loginWithValidCredentials();


        // After login, confirm profile selection page appears
        boolean profilePageShown = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-t='profile-button']"))
        ).isDisplayed();

        if(!profilePageShown){
            System.out.println("login failed still on login page");
        }

        Assert.assertTrue(profilePageShown,
                "Profile selection page should appear after a successful login");
        System.out.println("Valid login successful — profile selection page reached. URL: "
                + driver.getCurrentUrl() + "\n");
    }
}