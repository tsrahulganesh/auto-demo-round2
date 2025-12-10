import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

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
        try {
            // 1) Open login page
            driver.get("https://bankubt.onlinebank.com/Service/UserManager.aspx"); // ✅ Replace with your actual login
                                                                                   // URL

            // 2) Locate username and password fields using XPath
            WebElement usernameField = driver.findElement(
                    By.xpath("//input[@class='form-control component-group']"));
            WebElement passwordField = driver.findElement(
                    By.xpath("//input[@class='signin-password']"));

            // 3) Locate login button using XPath (handles spaces in text)
            WebElement loginButton = driver
                    .findElement(By.xpath("//input[@class='btn btn-primary']"));

            // 4) Enter credentials
            usernameField.sendKeys("Pawaradmin01"); // ✅ Replace with valid username
            passwordField.sendKeys("Test@2025"); // ✅ Replace with valid password

            // 5) Click login
            loginButton.click();

            // 6) Assert successful login by checking URL or title
            assertTrue(driver.getCurrentUrl().contains("Advanced") || driver.getTitle().contains("Dashboard"),
                    "module-title");

        } finally {
            driver.quit();
        }
    }
}
