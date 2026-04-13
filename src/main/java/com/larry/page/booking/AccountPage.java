package com.larry.page.booking;

import com.larry.driver.DriverManager;
import com.larry.page.booking.common.NavigationPage;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import static com.larry.config.ConfigurationManager.configuration;

public class AccountPage extends NavigationPage {

    @FindBy(css = "input[placeholder='email@example.com']")
    private WebElement email;

    @FindBy(css = "input[placeholder='Enter your password']")
    private WebElement password;

    @Step
    public void fillEmail(String email) {
        this.email.sendKeys(email);
    }

    @Step
    public void fillPassword(String password) {
        this.password.sendKeys(password);
    }

    @Step
    public void selectCountry(String province) {
        selectElementPlusOption(0, province);
    }

    @Step
    public void selectBudget(String value) {
        selectElementPlusOption(1, value);
    }

    @Step
    public void clickNewsletter() {
        // The new page does not have the newsletter checkbox anymore.
        // Keep this method for backward compatibility with test flow.
    }

    private void selectElementPlusOption(int dropdownIndex, String optionText) {
        var driver = DriverManager.getDriver();
        var wait = new WebDriverWait(driver, java.time.Duration.ofSeconds(configuration().timeout()));

        var dropdowns = wait.until(d -> {
            var items = d.findElements(By.cssSelector(".el-select"));
            return items.size() > dropdownIndex ? items : null;
        });
        var dropdown = dropdowns.get(dropdownIndex);
        dropdown.click();

        var option = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
                "//div[contains(@class,'el-select-dropdown') and not(contains(@style,'display: none'))]" +
                        "//li[contains(@class,'el-select-dropdown__item') and not(contains(@class,'is-disabled'))]" +
                        "[.//*[normalize-space()='" + optionText + "']]")));
        option.click();
    }
}
