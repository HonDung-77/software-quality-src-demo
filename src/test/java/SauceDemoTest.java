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

    // Khi chạy trên máy cá nhân, mỗi test kéo dài tối thiểu 15 giây
    // để dễ quan sát và chụp ảnh minh chứng.
    private static final long MIN_TEST_MILLIS = 15_000;
    private static final long DELAY_MILLIS = 1_000;

    private WebDriver driver;
    private WebDriverWait wait;
    private long startMillis;

    private boolean isCI() {
        return System.getenv("CI") != null;
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // Chạy local thì delay để dễ quan sát.
    // Trên GitHub Actions thì bỏ qua delay.
    private void delay() {
        if (!isCI()) {
            sleep(DELAY_MILLIS);
        }
    }

    @BeforeEach
    void setUp() {
        startMillis = System.currentTimeMillis();

        ChromeOptions options = new ChromeOptions();

        // Tắt trình quản lý mật khẩu và cảnh báo mật khẩu bị lộ của Chrome
        java.util.Map<String, Object> prefs = new java.util.HashMap<>();
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_enabled", false);
        prefs.put("profile.password_manager_leak_detection", false);
        options.setExperimentalOption("prefs", prefs);
        options.addArguments("--disable-features=PasswordLeakDetection");

        if (isCI()) {
            // GitHub Actions chạy Chrome không giao diện
            options.addArguments("--headless=new");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
        } else {
            // Máy cá nhân: dùng Brave nếu tìm thấy (Windows hoặc macOS),
            // nếu không có thì dùng Chrome mặc định
            String[] bravePaths = {
                    "C:\\Program Files\\BraveSoftware\\Brave-Browser\\Application\\brave.exe",
                    "/Applications/Brave Browser.app/Contents/MacOS/Brave Browser"
            };
            for (String path : bravePaths) {
                java.io.File file = new java.io.File(path);
                if (file.exists()) {
                    options.setBinary(file.getPath());
                    break;
                }
            }
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
            if (!isCI()) {
                // Giữ màn hình cuối cho đủ 15 giây rồi mới đóng trình duyệt
                long elapsed = System.currentTimeMillis() - startMillis;
                long remaining = MIN_TEST_MILLIS - elapsed;
                if (remaining > 0) {
                    sleep(remaining);
                }
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

    // Mở menu 3 gạch (nút hamburger) ở góc trên bên trái.
    // Thử lần lượt vài kiểu locator vì giao diện có thể đổi giữa các phiên bản.
    private void openMenu() {
        // Menu đã mở sẵn thì không bấm nữa (bấm lần nữa sẽ đóng menu)
        boolean alreadyOpen = driver.findElements(menuItem("All Items"))
                .stream().anyMatch(WebElement::isDisplayed);
        if (alreadyOpen) {
            return;
        }

        By[] candidates = {
                By.id("react-burger-menu-btn"),
                By.cssSelector("[data-test='open-menu']"),
                By.cssSelector("[data-test*='menu']"),
                By.cssSelector("button[aria-label*='enu']")
        };

        WebElement button = wait.until(d -> {
            for (By by : candidates) {
                for (WebElement e : d.findElements(by)) {
                    if (e.isDisplayed()) {
                        return e;
                    }
                }
            }
            return null;
        });
        button.click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(menuItem("All Items")));
    }

    // Tìm một mục trong menu theo chữ hiển thị
    private By menuItem(String text) {
        return By.xpath("//*[normalize-space(text())='" + text + "']");
    }

    // Mở rộng nhóm Dynamic Catalog nếu các mục con chưa hiển thị
    private void expandDynamicCatalog() {
        boolean expanded = driver.findElements(menuItem("Lazy Load"))
                .stream().anyMatch(WebElement::isDisplayed);
        if (!expanded) {
            driver.findElement(menuItem("Dynamic Catalog")).click();
        }
        wait.until(ExpectedConditions.visibilityOfElementLocated(menuItem("Lazy Load")));
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

    // TC03: Mở menu và hiển thị các mục danh mục
    @Test
    void testOpenMenuShowsCategories() {
        login();

        openMenu();
        delay();

        for (String item : new String[]{"All Items", "Dynamic Catalog", "About", "Logout"}) {
            assertTrue(
                    wait.until(ExpectedConditions.visibilityOfElementLocated(
                            menuItem(item))).isDisplayed(),
                    "Menu phải có mục " + item
            );
        }

        expandDynamicCatalog();
        delay();

        for (String item : new String[]{"Lazy Load", "Spinner", "Slider"}) {
            assertTrue(
                    driver.findElement(menuItem(item)).isDisplayed(),
                    "Nhóm Dynamic Catalog phải có mục " + item
            );
        }

        delay();
    }

    // TC04: Chuyển trang bằng các mục của menu Dynamic Catalog
    @Test
    void testNavigateDynamicCatalog() {
        login();
        String productsUrl = driver.getCurrentUrl();

        for (String page : new String[]{"Lazy Load", "Spinner", "Slider"}) {
            openMenu();
            expandDynamicCatalog();
            delay();

            driver.findElement(menuItem(page)).click();

            wait.until(d -> !d.getCurrentUrl().equals(productsUrl));
            assertNotEquals(productsUrl, driver.getCurrentUrl(),
                    "Phải chuyển sang trang " + page);
            delay();
        }

        // Quay lại danh sách sản phẩm bằng mục All Items
        openMenu();
        driver.findElement(menuItem("All Items")).click();
        wait.until(ExpectedConditions.urlToBe(productsUrl));
        assertEquals(productsUrl, driver.getCurrentUrl(),
                "All Items phải quay về danh sách sản phẩm");

        delay();
    }

    // TC05: Thêm sản phẩm vào giỏ hàng
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

    // TC06: Xóa sản phẩm khỏi giỏ hàng
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

    // TC07: Hoàn tất thanh toán
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