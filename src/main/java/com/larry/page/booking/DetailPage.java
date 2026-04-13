package com.larry.page.booking;

import com.larry.page.booking.common.NavigationPage;
import io.qameta.allure.Step;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class DetailPage extends NavigationPage {

    @FindBy(css = "textarea[placeholder^='Example:']")
    private WebElement roomDescription;

    @FindBy(xpath = "//p[contains(normalize-space(),'Thank you for your reservation')]")
    private WebElement message;

    @Step
    public void fillRoomDescription(String description) {
        roomDescription.sendKeys(description);
    }

    @Step
    public String getAlertMessage() {
        return message.getText();
    }
}
