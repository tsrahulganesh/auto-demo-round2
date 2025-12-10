import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import static org.junit.jupiter.api.Assertions.*;

public class DemoTest {

    private WebDriver getDriver() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless"); 
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--remote-allow-origins=*");
        return new ChromeDriver(options);
    }

    @Test
    void openGoogleChrome() {
        WebDriver driver = getDriver();
        driver.get("https://www.google.com");
        assertTrue(driver.getTitle().toLowerCase().contains("google"));
        driver.quit();
    }

    @Test
    void openAmazon() {
        WebDriver driver = getDriver();
        driver.get("https://www.amazon.in");
        assertTrue(driver.getTitle().toLowerCase().contains("amazon"));
        driver.quit();
    }

    @Test
    void openFlipkart() {
        WebDriver driver = getDriver();
        driver.get("https://www.flipkart.com");
        assertTrue(driver.getTitle().toLowerCase().contains("flipkart"));
        driver.quit();
    }
}

