package com.larry.page.booking;

import com.larry.driver.DriverManager;
import com.larry.page.booking.common.NavigationPage;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import static com.larry.config.ConfigurationManager.configuration;

public class RoomPage extends NavigationPage {

    @Step
    public void selectRoomType(String room) {
        var wait = new WebDriverWait(DriverManager.getDriver(), java.time.Duration.ofSeconds(configuration().timeout()));
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
                "//div[contains(@class,'cursor-pointer') and .//*[contains(normalize-space(),'" + room + "')]]")))
                .click();
    }
}
