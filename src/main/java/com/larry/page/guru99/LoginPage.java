package com.larry.page.guru99;

import com.larry.page.AbstractPageObject;
import com.larry.wait.Waits;
import io.qameta.allure.Step;
import org.openqa.selenium.Alert;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class LoginPage extends AbstractPageObject {

    @FindBy(name = "uid")
    private WebElement usernameInput;

    @FindBy(name = "password")
    private WebElement passwordInput;

    @FindBy(name = "btnLogin")
    private WebElement loginButton;

    @FindBy(name = "btnReset")
    private WebElement resetButton;

    @Step("Enter username: {username}")
    public void enterUsername(String username) {
        Waits.waitForVisibility(usernameInput).clear();
        usernameInput.sendKeys(username);
    }

    @Step("Enter password")
    public void enterPassword(String password) {
        Waits.waitForVisibility(passwordInput).clear();
        passwordInput.sendKeys(password);
    }

    @Step("Click login button")
    public void clickLogin() {
        Waits.waitForClickability(loginButton).click();
    }

    @Step("Click reset button")
    public void clickReset() {
        Waits.waitForClickability(resetButton).click();
    }

    @Step("Login with username: {username}")
    public void login(String username, String password) {
        enterUsername(username);
        enterPassword(password);
        clickLogin();
    }

    @Step("Get alert text")
    public String getAlertText() {
        try {
            Waits.waitForCustomCondition(ExpectedConditions.alertIsPresent());
            Alert alert = Waits.waitForCustomCondition(ExpectedConditions.alertIsPresent());
            return alert.getText();
        } catch (Exception e) {
            return null;
        }
    }

    @Step("Accept alert")
    public void acceptAlert() {
        try {
            Alert alert = Waits.waitForCustomCondition(ExpectedConditions.alertIsPresent());
            alert.accept();
        } catch (Exception e) {
        }
    }

    @Step("Get page title")
    public String getPageTitle() {
        return com.larry.driver.DriverManager.getDriver().getTitle();
    }

    @Step("Get current URL")
    public String getCurrentUrl() {
        return com.larry.driver.DriverManager.getDriver().getCurrentUrl();
    }
}
