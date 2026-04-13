package com.larry.test;

import com.larry.BaseWeb;
import com.larry.data.dynamic.BookingDataFactory;
import com.larry.page.booking.AccountPage;
import com.larry.page.booking.DetailPage;
import com.larry.page.booking.RoomPage;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class BookRoomWebTest extends BaseWeb {

    @Test(description = "Book a room")
    public void bookARoom() {
        var bookingInformation = BookingDataFactory.createBookingData();

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
