package org.demo.ui;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public class BaseUiTest {

    protected WebDriver driver;

    @BeforeMethod
    public void setUp() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) driver.quit();
    }
}