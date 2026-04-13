package com.larry.page.booking.common;

import com.larry.page.AbstractPageObject;
import io.qameta.allure.Step;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class NavigationPage extends AbstractPageObject {

    @FindBy(xpath = "//button[normalize-space()='Next Step']")
    private WebElement next;

    @FindBy(xpath = "//button[normalize-space()='Previous']")
    private WebElement previous;

    @FindBy(xpath = "//button[normalize-space()='Finish']")
    private WebElement finish;

    @Step
    public void next() {
        next.click();
    }

    @Step
    public void finish() {
        finish.click();
    }
}
