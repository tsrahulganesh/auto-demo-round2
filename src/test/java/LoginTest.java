
import org.junit.jupiter.api.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class LoginTest {

    private WebDriver getDriver() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new"); // Try removing headless if the site blocks automation
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--ignore-certificate-errors");
        options.setAcceptInsecureCerts(true);

        // Reduce detection in headless
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--disable-gpu");
        options.addArguments("--start-maximized");
        options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
                + "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36");

        return new ChromeDriver(options);
    }

    private static final String LOGIN_URL_USER_MANAGER = "https://bankubt.onlinebank.com/Service/UserManager.aspx";
    private static final String LOGIN_URL_SIGNIN = "https://bankubt.onlinebank.com/SignIn.aspx";

    // ✅ ASP.NET-friendly selectors by id-suffix (both pages often share the same
    // naming scheme)
    private static final By USERNAME_INPUT = By.cssSelector("input[id$='txtLoginName'], input[id$='txtUserName']");
    private static final By PASSWORD_INPUT = By.cssSelector("input[id$='txtPassword']");
    private static final By CONTINUE_BUTTON = By.cssSelector(
            "input[id$='cmdContinue'], button[id$='cmdContinue'], a[id$='cmdContinue'], input[type='submit']");

    // Optional: common validation/error areas
    private static final By VALIDATION_SUMMARY = By.cssSelector(".validation-summary, .alert-danger, .text-danger");

    private WebDriverWait wait(WebDriver driver, long seconds) {
        return new WebDriverWait(driver, Duration.ofSeconds(seconds));
    }

    private void waitForDocumentReady(WebDriver driver) {
        wait(driver, 30).until((ExpectedCondition<Boolean>) d -> "complete"
                .equals(((JavascriptExecutor) d).executeScript("return document.readyState")));
    }

    /** Switch to a frame containing the username input if needed */
    private void switchToLoginFrameIfNeeded(WebDriver driver) {
        driver.switchTo().defaultContent();

        if (!driver.findElements(USERNAME_INPUT).isEmpty()) {
            return;
        }
        List<WebElement> frames = driver.findElements(By.tagName("iframe"));
        for (int i = 0; i < frames.size(); i++) {
            try {
                driver.switchTo().defaultContent();
                driver.switchTo().frame(i);
                if (!driver.findElements(USERNAME_INPUT).isEmpty()) {
                    return;
                }
            } catch (WebDriverException ignored) {
            }
        }
        driver.switchTo().defaultContent();
        throw new NoSuchElementException("Username input not found in default content or any iframe.");
    }

    /**
     * Some sites open a new tab/window after login; ensure we’re on the last opened
     * one
     */
    private void switchToLatestWindow(WebDriver driver) {
        String current = driver.getWindowHandle();
        Set<String> handles = driver.getWindowHandles();
        for (String h : handles) {
            if (!h.equals(current)) {
                driver.switchTo().window(h);
            }
        }
    }

    /** Perform login on the current page (SignIn.aspx or UserManager.aspx) */
    private void performLogin(WebDriver driver, String username, String password) {
        switchToLoginFrameIfNeeded(driver);

        WebElement usernameField = wait(driver, 20)
                .until(ExpectedConditions.visibilityOfElementLocated(USERNAME_INPUT));
        WebElement passwordField = wait(driver, 20)
                .until(ExpectedConditions.visibilityOfElementLocated(PASSWORD_INPUT));

        // Fill credentials
        usernameField.clear();
        passwordField.clear();
        usernameField.sendKeys(username);
        passwordField.sendKeys(password);

        // Prefer click; fallback to Enter submit
        List<WebElement> buttons = driver.findElements(CONTINUE_BUTTON);
        if (!buttons.isEmpty()) {
            WebElement loginButton = buttons.get(0);
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({block:'center'});", loginButton);
            try {
                wait(driver, 15).until(ExpectedConditions.elementToBeClickable(loginButton));
                loginButton.click();
            } catch (ElementClickInterceptedException | TimeoutException e) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", loginButton);
            }
        } else {
            // Fallback: submit via Enter on password
            passwordField.sendKeys(Keys.ENTER);
        }
    }

    /**
     * Wait for a redirect away from SignIn.aspx or UserManager.aspx to a
     * dashboard/advanced page
     */
    private void waitForPostLogin(WebDriver driver) {
        waitForDocumentReady(driver);

        // Sometimes the site shows a spinner/backdrop; wait for it to disappear if
        // present
        try {
            By overlay = By.cssSelector(".modal-backdrop, .blockUI, .loading, .spinner, .pace");
            wait(driver, 10).until(ExpectedConditions.invisibilityOfElementLocated(overlay));
        } catch (TimeoutException ignored) {
        }

        // If a new window opened, switch to it
        switchToLatestWindow(driver);

        // Wait for URL or title to indicate success
        wait(driver, 20).until(d -> {
            String url = d.getCurrentUrl();
            String title = d.getTitle() == null ? "" : d.getTitle().toLowerCase();
            boolean onDashboard = url.contains("Advanced")
                    || url.toLowerCase().contains("dashboard")
                    || title.contains("dashboard")
                    || title.contains("advanced");
            boolean stillOnSignIn = url.contains("SignIn.aspx");
            return onDashboard || !stillOnSignIn;
        });
    }

    /** If login failed, surface any validation errors to the test output */
    private String extractVisibleErrors(WebDriver driver) {
        StringBuilder sb = new StringBuilder();
        List<WebElement> errors = driver.findElements(VALIDATION_SUMMARY);
        for (WebElement e : errors) {
            if (e.isDisplayed())
                sb.append(e.getText()).append("\n");
        }
        // Common ASP.NET inline validation spans
        List<WebElement> spans = driver.findElements(By.cssSelector("span[id*='val'], span.field-validation-error"));
        for (WebElement s : spans) {
            if (s.isDisplayed())
                sb.append(s.getText()).append("\n");
        }
        return sb.toString().trim();
    }

    @Test
    void simpleLoginTest() {
        WebDriver driver = getDriver();
        try {
            // Start from SignIn (many portals redirect there first)
            driver.get(LOGIN_URL_SIGNIN);
            waitForDocumentReady(driver);

            // If we got redirected to UserManager, fine; otherwise stay
            String startUrl = driver.getCurrentUrl();
            if (!(startUrl.contains("SignIn.aspx") || startUrl.contains("UserManager.aspx"))) {
                // In case of forced redirect, navigate explicitly to one of the known pages
                driver.get(LOGIN_URL_USER_MANAGER);
                waitForDocumentReady(driver);
            }

            // Perform login on the current page
            performLogin(driver, "Pawaradmin01", "Test@2025");

            // Wait for post-login redirect/content load
            waitForPostLogin(driver);

            // Final assertion: not on SignIn, and URL/title suggests dashboard
            String finalUrl = driver.getCurrentUrl();
            String finalTitle = driver.getTitle() == null ? "" : driver.getTitle();
            boolean success = finalUrl.contains("Advanced")
                    || finalUrl.toLowerCase().contains("dashboard")
                    || finalTitle.toLowerCase().contains("dashboard")
                    || finalTitle.toLowerCase().contains("advanced");

            if (!success) {
                // Gather visible errors to help diagnose
                String errors = extractVisibleErrors(driver);
                throw new AssertionError(
                        "Expected to be on Dashboard/Advanced after login.\n"
                                + "Actual: URL=" + finalUrl + " Title=" + finalTitle + "\n"
                                + (errors.isEmpty() ? "" : ("Visible validation/errors:\n" + errors)));
            }

            assertTrue(success,
                    "Expected to be on Dashboard/Advanced after login. Actual: URL=" + finalUrl
                            + " Title=" + finalTitle);

        } finally {
            driver.quit();
        }
    }
}
