
import org.junit.jupiter.api.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class LoginTest {

    private WebDriver getDriver() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new"); // Headless mode for CI
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--ignore-certificate-errors");
        options.setAcceptInsecureCerts(true);
        return new ChromeDriver(options);
    }

    private static final String LOGIN_URL = "https://bankubt.onlinebank.com/Service/UserManager.aspx";

    // ✅ Stable selectors using ID ends-with (ASP.NET friendly)
    private static final By USERNAME_INPUT = By.cssSelector("input[id$='txtLoginName']");
    private static final By PASSWORD_INPUT = By.cssSelector("input[id$='txtPassword']");

    // ✅ Robust login button selector (covers <button> and <input type=submit>)
    private static final By LOGIN_BUTTON = By.cssSelector(
            "button[type='submit'], input[type='submit'], button.btn.btn-primary, a.btn.btn-primary");

    private WebDriverWait wait(WebDriver driver, long seconds) {
        return new WebDriverWait(driver, Duration.ofSeconds(seconds));
    }

    private void waitForDocumentReady(WebDriver driver) {
        wait(driver, 20).until((ExpectedCondition<Boolean>) d -> ((JavascriptExecutor) d)
                .executeScript("return document.readyState").equals("complete"));
    }

    /**
     * If inputs are inside an iframe, switch into the frame that contains the
     * username field.
     */
    private void switchToLoginFrameIfNeeded(WebDriver driver) {
        driver.switchTo().defaultContent();

        // Try default content first
        List<WebElement> direct = driver.findElements(USERNAME_INPUT);
        if (!direct.isEmpty() && direct.get(0).isDisplayed()) {
            return;
        }

        // Try all iframes
        List<WebElement> frames = driver.findElements(By.tagName("iframe"));
        for (int i = 0; i < frames.size(); i++) {
            try {
                driver.switchTo().defaultContent();
                driver.switchTo().frame(i);
                if (!driver.findElements(USERNAME_INPUT).isEmpty()) {
                    return; // Found the right frame
                }
            } catch (WebDriverException ignored) {
            }
        }

        // If not found in any frame
        driver.switchTo().defaultContent();
        throw new NoSuchElementException("Username input not found in default content or any iframe.");
    }

    @Test
    void simpleLoginTest() {
        WebDriver driver = getDriver();
        try {
            // 1) Open login page
            driver.get(LOGIN_URL);
            waitForDocumentReady(driver);

            // 2) Switch to iframe if required
            switchToLoginFrameIfNeeded(driver);

            // 3) Locate fields and button
            WebElement usernameField = wait(driver, 15)
                    .until(ExpectedConditions.visibilityOfElementLocated(USERNAME_INPUT));
            WebElement passwordField = wait(driver, 15)
                    .until(ExpectedConditions.visibilityOfElementLocated(PASSWORD_INPUT));
            WebElement loginButton = wait(driver, 15)
                    .until(ExpectedConditions.elementToBeClickable(LOGIN_BUTTON));

            // 4) Enter credentials
            usernameField.clear();
            passwordField.clear();
            usernameField.sendKeys("Pawaradmin01");
            passwordField.sendKeys("Test@2025");

            // 5) Click login
            loginButton.click();

            // 6) Assert successful login by checking URL or title
            waitForDocumentReady(driver);
            assertTrue(
                    driver.getCurrentUrl().contains("Advanced")
                            || driver.getTitle().toLowerCase().contains("dashboard"),
                    "Expected to be on Dashboard/Advanced after login. Actual: URL=" + driver.getCurrentUrl()
                            + " Title=" + driver.getTitle());

        } finally {
            driver.quit();
        }
    }
}
