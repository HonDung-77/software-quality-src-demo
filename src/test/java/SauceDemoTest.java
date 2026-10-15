
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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

public class SauceDemoTest {

    private WebDriver driver;
    private WebDriverWait wait;

    // Delay 2 giây để quan sát thao tác
    private void delay() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @BeforeEach
    void setUp() {
        ChromeOptions options = new ChromeOptions();

        // Đường dẫn trình duyệt Brave
        options.setBinary(
                "C:\\Program Files\\BraveSoftware\\Brave-Browser\\Application\\brave.exe"
        );

        driver = new ChromeDriver(options);

        wait = new WebDriverWait(
                driver, Duration.ofSeconds(10)
        );

        // Mở trang đăng nhập
        driver.get("https://www.saucedemo.com/");
        delay();
    }

    @AfterEach
    void tearDown() {
        // Chờ để quan sát kết quả trước khi đóng Brave
        if (driver != null) {
            delay();
            driver.quit();
        }
    }

    // TC01: Đăng nhập thành công
    @Test
    void testLoginSuccess() {
        // Nhập tên đăng nhập
        driver.findElement(By.id("user-name"))
                .sendKeys("standard_user");
        delay();

        // Nhập mật khẩu
        driver.findElement(By.id("password"))
                .sendKeys("secret_sauce");
        delay();

        // Nhấn nút đăng nhập
        driver.findElement(By.id("login-button"))
                .click();
        delay();

        // Chờ trang sản phẩm xuất hiện
        WebElement title = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector("[data-test='title']")
                )
        );

        // Kiểm tra đăng nhập thành công
        assertEquals("Products", title.getText());
        delay();
    }

    // TC02: Đăng nhập thất bại
    @Test
    void testLoginFailure() {
        // Nhập tên đăng nhập
        driver.findElement(By.id("user-name"))
                .sendKeys("standard_user");
        delay();

        // Nhập mật khẩu sai
        driver.findElement(By.id("password"))
                .sendKeys("wrong_password");
        delay();

        // Nhấn nút đăng nhập
        driver.findElement(By.id("login-button"))
                .click();
        delay();

        // Chờ thông báo lỗi xuất hiện
        WebElement error = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector("[data-test='error']")
                )
        );

        // Kiểm tra thông báo đăng nhập sai
        assertTrue(
                error.getText().contains(
                        "Username and password do not match"
                )
        );
        delay();
    }

    // TC03: Thêm sản phẩm vào giỏ hàng
    @Test
    void testAddProductToCart() {
        // Nhập tên đăng nhập
        driver.findElement(By.id("user-name"))
                .sendKeys("standard_user");
        delay();

        // Nhập mật khẩu
        driver.findElement(By.id("password"))
                .sendKeys("secret_sauce");
        delay();

        // Đăng nhập
        driver.findElement(By.id("login-button"))
                .click();

        // Chờ trang sản phẩm xuất hiện
        wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector("[data-test='title']")
                )
        );
        delay();

        // Thêm balo vào giỏ hàng
        driver.findElement(
                By.cssSelector(
                        "[data-test='add-to-cart-sauce-labs-backpack']"
                )
        ).click();
        delay();

        // Kiểm tra số lượng sản phẩm trong giỏ
        WebElement badge = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.className("shopping_cart_badge")
                )
        );

        assertEquals("1", badge.getText());
        delay();

        // Mở giỏ hàng
        driver.findElement(
                By.className("shopping_cart_link")
        ).click();
        delay();

        // Chờ tên sản phẩm xuất hiện
        WebElement product = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.className("inventory_item_name")
                )
        );

        // Kiểm tra đúng sản phẩm đã được thêm
        assertEquals(
                "Sauce Labs Backpack",
                product.getText()
        );
        delay();
    }
}
