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

    // Chạy local thì delay để dễ quan sát.
    // Trên GitHub Actions thì bỏ qua delay.
    private void delay() {
        if (System.getenv("CI") != null) {
            return;
        }

        try {
            Thread.sleep(1200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @BeforeEach
    void setUp() {
        ChromeOptions options = new ChromeOptions();

        if (System.getenv("CI") != null) {
            // GitHub Actions chạy Chrome không giao diện
            options.addArguments("--headless=new");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
        } else {
            // Máy cá nhân chạy Brave
            options.setBinary(
                    "C:\\Program Files\\BraveSoftware\\Brave-Browser\\Application\\brave.exe"
            );
        }

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        driver.get("https://www.saucedemo.com/");
        wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.id("login-button")
                )
        );

        delay();
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            if (System.getenv("CI") == null) {
                delay();
            }

            driver.quit();
        }
    }

    // Hàm đăng nhập dùng chung
    private void login() {
        driver.findElement(By.id("user-name"))
                .sendKeys("standard_user");
        delay();

        driver.findElement(By.id("password"))
                .sendKeys("secret_sauce");
        delay();

        driver.findElement(By.id("login-button"))
                .click();

        wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector("[data-test='title']")
                )
        );

        delay();
    }

    // TC01: Đăng nhập thành công
    @Test
    void testLoginSuccess() {
        driver.findElement(By.id("user-name"))
                .sendKeys("standard_user");
        delay();

        driver.findElement(By.id("password"))
                .sendKeys("secret_sauce");
        delay();

        driver.findElement(By.id("login-button"))
                .click();

        WebElement title = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector("[data-test='title']")
                )
        );

        assertEquals("Products", title.getText());
        delay();
    }

    // TC02: Đăng nhập thất bại
    @Test
    void testLoginFailure() {
        driver.findElement(By.id("user-name"))
                .sendKeys("standard_user");
        delay();

        driver.findElement(By.id("password"))
                .sendKeys("wrong_password");
        delay();

        driver.findElement(By.id("login-button"))
                .click();

        WebElement error = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector("[data-test='error']")
                )
        );

        assertTrue(
                error.getText().contains(
                        "Username and password do not match"
                ),
                "Phải hiển thị thông báo sai tài khoản hoặc mật khẩu"
        );

        delay();
    }

    // TC03: Thêm sản phẩm vào giỏ hàng
    @Test
    void testAddProductToCart() {
        login();

        driver.findElement(
                By.cssSelector(
                        "[data-test='add-to-cart-sauce-labs-backpack']"
                )
        ).click();
        delay();

        WebElement badge = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.className("shopping_cart_badge")
                )
        );

        assertEquals("1", badge.getText());

        driver.findElement(
                By.className("shopping_cart_link")
        ).click();

        WebElement product = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.className("inventory_item_name")
                )
        );

        assertEquals("Sauce Labs Backpack", product.getText());
        delay();
    }

    // TC04: Xóa sản phẩm khỏi giỏ hàng
    @Test
    void testRemoveProductFromCart() {
        login();

        // Thêm balo vào giỏ
        driver.findElement(
                By.cssSelector(
                        "[data-test='add-to-cart-sauce-labs-backpack']"
                )
        ).click();
        delay();

        // Mở giỏ hàng
        driver.findElement(
                By.className("shopping_cart_link")
        ).click();

        wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.className("inventory_item_name")
                )
        );
        delay();

        // Xóa balo
        driver.findElement(
                By.cssSelector(
                        "[data-test='remove-sauce-labs-backpack']"
                )
        ).click();
        delay();

        // Xác nhận không còn sản phẩm
        assertTrue(
                driver.findElements(
                        By.className("cart_item")
                ).isEmpty(),
                "Giỏ hàng phải trống sau khi xóa sản phẩm"
        );

        // Xác nhận biểu tượng số lượng không còn
        assertTrue(
                driver.findElements(
                        By.className("shopping_cart_badge")
                ).isEmpty(),
                "Số lượng trên biểu tượng giỏ hàng phải biến mất"
        );

        delay();
    }

    // TC05: Hoàn tất thanh toán
    @Test
    void testCheckoutSuccess() {
        login();

        // Thêm balo
        driver.findElement(
                By.cssSelector(
                        "[data-test='add-to-cart-sauce-labs-backpack']"
                )
        ).click();
        delay();

        // Mở giỏ hàng
        driver.findElement(
                By.className("shopping_cart_link")
        ).click();

        wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.className("cart_item")
                )
        );
        delay();

        // Bắt đầu thanh toán
        driver.findElement(
                By.id("checkout")
        ).click();

        wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.id("first-name")
                )
        );
        delay();

        // Nhập thông tin người mua
        driver.findElement(By.id("first-name"))
                .sendKeys("Nguyen");
        delay();

        driver.findElement(By.id("last-name"))
                .sendKeys("Van A");
        delay();

        driver.findElement(By.id("postal-code"))
                .sendKeys("700000");
        delay();

        // Tiếp tục
        driver.findElement(By.id("continue"))
                .click();

        wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.id("finish")
                )
        );
        delay();

        // Xác nhận đúng sản phẩm trước khi đặt hàng
        WebElement product = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.className("inventory_item_name")
                )
        );

        assertEquals("Sauce Labs Backpack", product.getText());

        // Hoàn tất đặt hàng
        driver.findElement(By.id("finish"))
                .click();

        WebElement completeHeader = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.className("complete-header")
                )
        );

        assertEquals(
                "Thank you for your order!",
                completeHeader.getText()
        );

        assertTrue(
                driver.getCurrentUrl().contains("checkout-complete"),
                "Phải chuyển đến trang hoàn tất thanh toán"
        );

        delay();
    }
}