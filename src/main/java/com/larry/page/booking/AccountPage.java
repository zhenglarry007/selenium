package com.larry.page.booking;

import com.larry.page.booking.common.NavigationPage;
import com.larry.wait.Waits;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class AccountPage extends NavigationPage {

    @FindBy(css = "[data-testid='email-input']")
    private WebElement email;

    @FindBy(css = "[data-testid='password-input']")
    private WebElement password;

    @Step
    public void fillEmail(String email) {
        Waits.waitForVisibility(this.email).sendKeys(email);
    }

    @Step
    public void fillPassword(String password) {
        Waits.waitForVisibility(this.password).sendKeys(password);
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
    }

    private void selectElementPlusOption(int dropdownIndex, String optionText) {
        var dropdowns = Waits.waitForListToLoad(By.cssSelector(".el-select"), dropdownIndex + 1);
        var dropdown = dropdowns.get(dropdownIndex);
        Waits.waitForClickability(dropdown).click();

        var option = Waits.waitForClickability(By.xpath(
                "//div[contains(@class,'el-select-dropdown') and not(contains(@style,'display: none'))]" +
                        "//li[contains(@class,'el-select-dropdown__item') and not(contains(@class,'is-disabled'))]" +
                        "[.//*[normalize-space()='" + optionText + "']]"));
        option.click();
    }
}
