
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import static org.junit.jupiter.api.Assertions.*;

public class urltest {

    private WebDriver getDriver() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new"); // Headless mode
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        return new ChromeDriver(options);
    }

    @Test
    void openSignInPage() {
        WebDriver driver = getDriver();
        try {
            String url = "https://choosethechief-p2.onlinebank.com/SignIn.aspx?p_r=1";
            driver.get(url);

            // ✅ Simple assertions without XPath
            assertTrue(driver.getCurrentUrl().contains("onlinebank.com"),
                    "URL does not contain expected domain");

            String title = driver.getTitle();
            assertNotNull(title, "Page title should not be null");
            System.out.println("Page Title: " + title);

        } finally {
            driver.quit();
        }
    }
}
