
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

public class LoginTest {

    private WebDriver getDriver() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new"); // Headless mode for CI
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        return new ChromeDriver(options);
    }

    @Test
    void simpleLoginTest() {
        WebDriver driver = getDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

        try {
            // 1) Open login page
            driver.get("https://bankubt.onlinebank.com/Service/UserManager.aspx");

            // 2) Locate username and password fields using XPath
            WebElement usernameField = driver.findElement(
                    By.xpath("//input[@class='form-control component-group']"));
            WebElement passwordField = driver.findElement(
                    By.xpath("//input[@class='signin-password']"));

            // 3) Locate login button using XPath
            WebElement loginButton = driver.findElement(
                    By.xpath("//input[@class='btn btn-primary']"));

            // 4) Enter credentials
            usernameField.sendKeys("Pawaradmin01"); // Replace with valid username
            passwordField.sendKeys("Test@2025"); // Replace with valid password

            // 5) Click login
            loginButton.click();

            // (Optional) Wait until we leave the login page
            wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("UserManager.aspx")));

            // 6) Wait for the Advanced User Search header element and assert its text
            // 'module-title' is the class for the header of Advanced User Search
            WebElement header = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector("module-title")));

            String headerText = header.getText().trim();
            assertTrue(headerText.toLowerCase().contains("Advanced User Search"),
                    "Expected 'module-title' text to contain 'Advanced User Search' but was: '" + headerText + "'");

            // (Optional) If you also want a title check, keep it tolerant:
            // assertTrue(driver.getTitle().toLowerCase().contains("advanced user search"),
            // "Expected page title to contain 'Advanced User Search' but was: " +
            // driver.getTitle());

        } finally {
            driver.quit();
        }
    }
}
