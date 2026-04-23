package com.larry.test;

import com.larry.BaseWeb;
import com.larry.data.provider.TestDataProvider;
import com.larry.model.Booking;
import com.larry.page.booking.AccountPage;
import com.larry.page.booking.DetailPage;
import com.larry.page.booking.RoomPage;
import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class BookRoomWebTest extends BaseWeb {

    @Test(dataProvider = "bookingDataJson", dataProviderClass = TestDataProvider.class,
            description = "Book a room with test data from JSON")
    @Severity(SeverityLevel.CRITICAL)
    @Story("用户预订房间功能")
    @Description("使用JSON测试数据执行预订房间流程，包含多种房型和预算组合")
    public void bookARoom(Booking bookingInformation) {
        var accountPage = new AccountPage();
        accountPage.fillEmail(bookingInformation.email());
        accountPage.fillPassword(bookingInformation.password());
        accountPage.selectCountry(bookingInformation.country());
        accountPage.selectBudget(bookingInformation.dailyBudget());
        accountPage.clickNewsletter();
        accountPage.next();

        var roomPage = new RoomPage();
        roomPage.selectRoomType(bookingInformation.roomType().get());
        roomPage.next();

        var detailPage = new DetailPage();
        detailPage.fillRoomDescription(bookingInformation.roomDescription());
        detailPage.finish();

        assertThat(detailPage.getAlertMessage())
                .isEqualTo("Thank you for your reservation. Your booking details have been saved successfully.");
    }
}
