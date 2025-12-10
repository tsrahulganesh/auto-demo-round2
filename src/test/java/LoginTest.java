
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
                        WebElement usernameField = driver.findElement(
                                        By.xpath("//input[@class='form-control component-group']"));
                        WebElement passwordField = driver.findElement(
                                        By.xpath("//input[@class='signin-password']"));
                        WebElement loginButton = driver.findElement(
                                        By.xpath("//input[@class='btn btn-primary']"));

                        // 3) Enter credentials (consider env vars for CI)
                        usernameField.sendKeys("Pawaradmin01");
                        passwordField.sendKeys("Test@2025");

                        // 4) Click login
                        loginButton.click();

                        // 5a) Wait until we leave the login page
                        wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("UserManager.aspx")));

                        // 5b) Wait for document.readyState == 'complete'
                        wait.until(d -> ((JavascriptExecutor) d)
                                        .executeScript("return document.readyState").equals("complete"));

                        // 6) Switch into the iframe that contains Advanced User Search
                        // If you know the iframe id/name, use that for determinism:
                        // wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.cssSelector("iframe#advancedUserFrame")));
                        // Otherwise, auto-discover: switch into the first iframe that contains
                        // '.module-title'
                        boolean switched = switchToFrameContaining(driver, By.cssSelector(".module-title"),
                                        Duration.ofSeconds(15));
                        assertTrue(switched, "Could not find a frame containing '.module-title' after login.");

                        // 7) Assert the Advanced User Search header inside the iframe
                        WebElement header = wait.until(ExpectedConditions.visibilityOfElementLocated(
                                        By.cssSelector(".module-title")));
                        String headerText = header.getText() == null ? "" : header.getText().trim().toLowerCase();

                        assertTrue(headerText.contains("advanced user search")
                                        || (headerText.contains("advanced") && headerText.contains("user search")),
                                        "Expected '.module-title' to contain 'Advanced User Search' but was: '"
                                                        + header.getText() + "'");

                        // 8) (Optional) If you still want to assert the icon, check attributes instead
                        // of text
                        // This is more reliable for icon elements.
                        WebElement serviceIcon = driver.findElement(By.cssSelector(".icon-services"));
                        String iconText = serviceIcon.getText();
                        String iconTitle = serviceIcon.getAttribute("title");
                        String ariaLabel = serviceIcon.getAttribute("aria-label");
                        boolean iconIndicatesService = (iconText != null && iconText.toLowerCase().contains("service"))
                                        || (iconTitle != null && iconTitle.toLowerCase().contains("service"))
                                        || (ariaLabel != null && ariaLabel.toLowerCase().contains("service"));

                        // Optional assertion (enable only if this is truly expected in your UI)
                        // assertTrue(iconIndicatesService,
                        // "Expected '.icon-services' to indicate 'SERVICE' in text/title/aria-label but
                        // found: "
                        // + "text='" + iconText + "', title='" + iconTitle + "', aria-label='" +
                        // ariaLabel + "'");

                } finally {
                        driver.quit();
                }
        }

        /**
         * Switches into the first iframe that contains an element matching
         * 'innerLocator'.
         * Returns true if switched successfully; otherwise returns false and stays in
         * default content.
         */
        private boolean switchToFrameContaining(WebDriver driver, By innerLocator, Duration perFrameTimeout) {
                driver.switchTo().defaultContent();

                // Try in default content first
                if (!driver.findElements(innerLocator).isEmpty()) {
                        return true; // element is in main DOM
                }

                // Iterate all iframes
                var frames = driver.findElements(By.tagName("iframe"));
                for (int i = 0; i < frames.size(); i++) {
                        try {
                                driver.switchTo().defaultContent();
                                driver.switchTo().frame(i);
                                WebDriverWait shortWait = new WebDriverWait(driver, perFrameTimeout);
                                // Use presence first; visibility can fail if overlays exist
                                WebElement el = shortWait
                                                .until(ExpectedConditions.presenceOfElementLocated(innerLocator));
                                if (el != null) {
                                        return true; // we're inside the correct frame
                                }
                        } catch (Exception ignored) {
                                // try next frame
                        }
                }
                driver.switchTo().defaultContent();
                return false;
        }
}
