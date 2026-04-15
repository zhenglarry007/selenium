package com.larry.page.booking;

import com.larry.page.booking.common.NavigationPage;
import com.larry.wait.Waits;
import io.qameta.allure.Step;
import org.openqa.selenium.By;

public class RoomPage extends NavigationPage {

    @Step
    public void selectRoomType(String room) {
        Waits.waitForClickability(By.xpath(
                "//div[contains(@class,'cursor-pointer') and .//*[contains(normalize-space(),'" + room + "')]]"))
                .click();
    }
}
