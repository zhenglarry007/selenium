package com.larry.test;

import com.larry.BaseWeb;
import com.larry.data.dynamic.BookingDataFactory;
import com.larry.driver.DriverManager;
import com.larry.model.Booking;
import com.larry.page.booking.AccountPage;
import com.larry.page.booking.DetailPage;
import com.larry.page.booking.RoomPage;
import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Step;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

import static com.larry.config.ConfigurationManager.configuration;
import static org.assertj.core.api.Assertions.assertThat;

public class BookRoomWebTest extends BaseWeb {

    private static final int BOOKING_COUNT = 5;

//    /**
//     * 方式 A：@DataProvider 参数化方式
//     * 每条数据对应一个独立的测试用例，每次重新打开浏览器
//     * 数据来源：src/test/resources/testdata/booking.json（固定数据）
//     */
//    @Test(dataProvider = "bookingDataJson", dataProviderClass = TestDataProvider.class,
//            description = "Book a room with test data from JSON")
//    @Severity(SeverityLevel.CRITICAL)
//    @Story("用户预订房间功能")
//    @Description("使用JSON测试数据执行预订房间流程，包含多种房型和预算组合")
//    public void bookARoomWithJsonData(Booking bookingInformation) {
//        var accountPage = new AccountPage();
//        accountPage.fillEmail(bookingInformation.email());
//        accountPage.fillPassword(bookingInformation.password());
//        accountPage.selectCountry(bookingInformation.country());
//        accountPage.selectBudget(bookingInformation.dailyBudget());
//        accountPage.clickNewsletter();
//        accountPage.next();
//
//        var roomPage = new RoomPage();
//        roomPage.selectRoomType(bookingInformation.roomType().get());
//        roomPage.next();
//
//        var detailPage = new DetailPage();
//        detailPage.fillRoomDescription(bookingInformation.roomDescription());
//        detailPage.finish();
//
//        assertThat(detailPage.getAlertMessage())
//                .isEqualTo("Thank you for your reservation. Your booking details have been saved successfully.");
//    }

    /**
     * 方式 B：单浏览器会话内循环方式
     * 只打开/关闭浏览器一次，在同一会话内循环执行多次预订
     * 数据来源：每次循环时调用 BookingDataFactory.createBookingData() 生成新数据
     * 特点：每填入一条数据，就从随机测试数据中取一条；完成后刷新 URL 回到首页
     */
    @Test(description = "Book multiple rooms with dynamic data in a single browser session")
    @Severity(SeverityLevel.CRITICAL)
    @Story("用户预订房间功能")
    @Description("使用Faker动态生成的测试数据，在单浏览器会话内循环执行多次预订流程，每次循环生成新数据")
    public void bookMultipleRoomsInSingleSession() {
        for (int i = 1; i <= BOOKING_COUNT; i++) {
            Booking bookingInformation = BookingDataFactory.createBookingData();
            
            executeSingleBooking(i, bookingInformation);
            
            if (i < BOOKING_COUNT) {
                navigateBackToHome();
            }
        }
    }

    @Step("执行第 [{0}] 次预订 - 邮箱: {1.email}, 房型: {1.roomType}")
    private void executeSingleBooking(int index, Booking bookingInformation) {
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
                .as("第 %d 次预订应成功", index)
                .isEqualTo("Thank you for your reservation. Your booking details have been saved successfully.");
    }

    @Step("刷新 URL 回到首页，准备下一次预订")
    private void navigateBackToHome() {
        String baseUrl = configuration().url();
        DriverManager.getDriver().get(baseUrl);
    }
}
