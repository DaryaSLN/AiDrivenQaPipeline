package org.demo.ui;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class CreateAccountPage {

    private static final Logger logger = LogManager.getLogger(CreateAccountPage.class);
    private final WebDriver driver;

    public CreateAccountPage(WebDriver driver) {
        this.driver = driver;
    }

    public void open() {
        logger.info("Opening Account Creation page: https://www.ae.com/us/en/account/create-account");
        driver.get("https://www.ae.com/us/en/account/create-account");
    }

    public void createAccount(String firstName, String lastName, String email, String password, String confirmPassword) {
        logger.info("Attempting to create an account with email: {}", email);
        logger.debug("First Name: {}, Last Name: {}, Password: {}", firstName, lastName, password);

        driver.findElement(By.id("firstNameInput")).sendKeys(firstName);
        logger.debug("Entered first name.");
        driver.findElement(By.id("lastNameInput")).sendKeys(lastName);
        logger.debug("Entered last name.");
        driver.findElement(By.id("emailInput")).sendKeys(email);
        logger.debug("Entered email.");
        driver.findElement(By.id("passwordInput")).sendKeys(password);
        logger.debug("Entered password.");
        driver.findElement(By.id("confirmPasswordInput")).sendKeys(confirmPassword);
        logger.debug("Entered confirm password.");
        driver.findElement(By.id("createAccountButton")).click();
        logger.info("Clicked 'Create Account' button.");
    }

    public boolean isErrorVisible() {
        boolean visible = !driver.findElements(By.cssSelector(".error-message")).isEmpty();
        logger.info("Checking for visible error message. Found: {}", visible);
        return visible;
    }

    public boolean isAccountCreatedSuccessfully() {
        boolean success = driver.getCurrentUrl().contains("account/dashboard"); // Example: redirects to a dashboard page
        logger.info("Checking for successful account creation. Result: {}", success);
        return success;
    }
}
