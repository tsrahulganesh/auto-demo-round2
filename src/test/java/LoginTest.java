
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.JavascriptExecutor;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

public class LoginTest {

        private WebDriver getDriver() {
                ChromeOptions options = new ChromeOptions();
                options.addArguments("--headless=new"); // Headless mode for CI
                options.addArguments("--no-sandbox");
                options.addArguments("--disable-dev-shm-usage");
                options.addArguments("--window-size=1920,1080"); // stabilize headless layout
                return new ChromeDriver(options);
        }

        @Test
        void simpleLoginTest() {
                WebDriver driver = getDriver();
                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));

                try {
                        // 1) Open login page
                        driver.get("https://bankubt.onlinebank.com/Service/UserManager.aspx");

                        // 2) Locate username and password fields
                        WebElement usernameField = driver
                                        .findElement(By.xpath("//input[@class='form-control component-group']"));
                        WebElement passwordField = driver.findElement(By.xpath("//input[@class='signin-password']"));
                        WebElement loginButton = driver.findElement(By.xpath("//input[@class='btn btn-primary']"));

                        // 3) Enter credentials
                        usernameField.sendKeys("Pawaradmin01");
                        passwordField.sendKeys("Test@2025");

                        // 4) Click login
                        loginButton.click();

                        // 5) Wait until we leave the login page
                        wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("UserManager.aspx")));

                        // 6) Wait for document.readyState == 'complete'
                        wait.until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState")
                                        .equals("complete"));

                        // ✅ 7) Assert that the page title contains "SERVICE"
                        String pageTitle = driver.getTitle();
                        assertNotNull(pageTitle, "Page title is null after login.");
                        assertTrue(pageTitle.toUpperCase().contains("SERVICE"),
                                        "Expected page title to contain 'SERVICE' but was: " + pageTitle);

                } finally {
                        driver.quit();
                }
        }
}
