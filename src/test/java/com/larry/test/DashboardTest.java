package com.larry.test;

import com.larry.BaseWeb;
import com.larry.data.provider.TestDataProvider;
import com.larry.driver.DriverManager;
import com.larry.model.DashboardConfig;
import com.larry.page.booking.DashboardPage;
import com.larry.wait.Waits;
import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.testng.annotations.Test;

import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

public class DashboardTest extends BaseWeb {

    private final DashboardConfig config = TestDataProvider.getDashboardConfig();
    
    private static final int PAGE_REFRESH_INTERVAL_SECONDS = 5;
    private static final int WAIT_TIMEOUT_SECONDS = 15;

    @Test(description = "Dashboard 数据仪表盘综合测试 - 一次验证所有页面元素和数据")
    @Severity(SeverityLevel.BLOCKER)
    @Story("数据仪表盘综合功能")
    @Description("在一次浏览器会话中验证数据仪表盘的所有页面元素和数据，处理页面自动刷新")
    public void testDashboardComprehensive() throws InterruptedException {
        var dashboardPage = new DashboardPage();
        
        Waits.sleep(1000);
        
        dashboardPage.navigateToDashboard();
        
        Waits.sleep(1000);
        
        waitForPageToStabilize();
        verifyPageLoad(dashboardPage);
        
        Waits.sleep(1000);
        
        verifyStatisticsWithRetry(dashboardPage);
        
        scrollToBottom();
        Waits.sleep(1000);
        
        verifyBookingRecordsWithRetry(dashboardPage);
        
        scrollToTop();
        Waits.sleep(1000);
        
        verifyRefreshButtonWithRetry(dashboardPage);
        
        Waits.sleep(1000);
        verifyPageUrl();
        
        Waits.sleep(2000);
    }

    @io.qameta.allure.Step("等待页面稳定")
    private void waitForPageToStabilize() {
        var totalBookingsLabel = By.xpath("//*[normalize-space()='总预订数']");
        Waits.waitForVisibility(totalBookingsLabel, WAIT_TIMEOUT_SECONDS);
        
        var totalBookingsValue = By.cssSelector(".text-3xl.font-bold.text-gray-800");
        Waits.waitForVisibility(totalBookingsValue, WAIT_TIMEOUT_SECONDS);
    }

    @io.qameta.allure.Step("滚动到页面底部")
    private void scrollToBottom() {
        var driver = DriverManager.getDriver();
        if (driver instanceof JavascriptExecutor js) {
            js.executeScript("window.scrollTo(0, document.body.scrollHeight)");
        }
        waitForScrollToComplete();
    }

    @io.qameta.allure.Step("滚动到页面顶部")
    private void scrollToTop() {
        var driver = DriverManager.getDriver();
        if (driver instanceof JavascriptExecutor js) {
            js.executeScript("window.scrollTo(0, 0)");
        }
        waitForScrollToComplete();
    }

    @io.qameta.allure.Step("等待滚动完成")
    private void waitForScrollToComplete() {
        Waits.sleep(500);
    }

    @io.qameta.allure.Step("验证页面加载")
    private void verifyPageLoad(DashboardPage dashboardPage) {
        boolean displayed = executeWithRetry(() -> dashboardPage.isDashboardPageDisplayed());
        assertThat(displayed)
                .as("仪表盘页面应正确显示")
                .isTrue();
    }

    @io.qameta.allure.Step("验证统计数据（带重试）")
    private void verifyStatisticsWithRetry(DashboardPage dashboardPage) {
        executeWithRetry(() -> {
            verifyStatistics(dashboardPage);
            return null;
        });
    }

    @io.qameta.allure.Step("验证统计数据")
    private void verifyStatistics(DashboardPage dashboardPage) {
        String totalBookings = dashboardPage.getTotalBookings();
        assertThat(totalBookings)
                .as("总预订数应显示")
                .isNotNull()
                .isNotEmpty();
        Waits.sleep(500);
        
        int totalBookingsInt = Integer.parseInt(totalBookings);
        assertThat(totalBookingsInt)
                .as("总预订数应大于等于 %d", config.expectedMinimumTotalBookings())
                .isGreaterThanOrEqualTo(config.expectedMinimumTotalBookings());
        Waits.sleep(500);
        
        String pendingBookings = dashboardPage.getPendingBookings();
        assertThat(pendingBookings)
                .as("待处理预订数应显示")
                .isNotNull()
                .isNotEmpty();
        Waits.sleep(500);
        
        int pendingBookingsInt = Integer.parseInt(pendingBookings);
        assertThat(pendingBookingsInt)
                .as("待处理预订数应大于等于 %d", config.expectedMinimumPendingBookings())
                .isGreaterThanOrEqualTo(config.expectedMinimumPendingBookings());
        Waits.sleep(500);
        
        String roomTypes = dashboardPage.getRoomTypesCount();
        assertThat(roomTypes)
                .as("房间类型数应显示")
                .isNotNull()
                .isNotEmpty();
        Waits.sleep(500);
        
        int roomTypesInt = Integer.parseInt(roomTypes);
        assertThat(roomTypesInt)
                .as("房间类型数应大于等于 %d", config.expectedMinimumRoomTypes())
                .isGreaterThanOrEqualTo(config.expectedMinimumRoomTypes());
        Waits.sleep(500);
        
        String lastUpdate = dashboardPage.getLastUpdateText();
        assertThat(lastUpdate)
                .as("最后更新时间应显示")
                .isNotNull()
                .isNotEmpty();
        Waits.sleep(500);
        
        assertThat(lastUpdate)
                .as("最后更新时间格式应为 HH:MM:SS")
                .matches("\\d{2}:\\d{2}:\\d{2}");
        Waits.sleep(500);
    }

    @io.qameta.allure.Step("验证预订记录（带重试）")
    private void verifyBookingRecordsWithRetry(DashboardPage dashboardPage) {
        executeWithRetry(() -> {
            verifyBookingRecords(dashboardPage);
            return null;
        });
    }

    @io.qameta.allure.Step("验证预订记录")
    private void verifyBookingRecords(DashboardPage dashboardPage) {
        var tableLocator = By.xpath("//table/tbody/tr");
        Waits.waitForListToLoad(tableLocator, config.expectedMinimumRecords(), WAIT_TIMEOUT_SECONDS);
        
        int recordCount = dashboardPage.getBookingRecordCount();
        assertThat(recordCount)
                .as("预订记录数量应大于等于 %d", config.expectedMinimumRecords())
                .isGreaterThanOrEqualTo(config.expectedMinimumRecords());
        Waits.sleep(500);
        
        var records = dashboardPage.getBookingRecords();
        assertThat(records)
                .as("预订记录列表不应为空")
                .isNotEmpty();
        Waits.sleep(500);
        
        assertThat(records.size())
                .as("预订记录数量应大于等于 %d", config.expectedMinimumRecords())
                .isGreaterThanOrEqualTo(config.expectedMinimumRecords());
        Waits.sleep(500);
        
        var firstRecord = records.get(0);
        assertThat(firstRecord)
                .as("第一条记录应包含数据")
                .isNotNull();
        Waits.sleep(500);
        
        assertThat(firstRecord.keySet())
                .as("记录应包含列标题")
                .isNotEmpty();
        Waits.sleep(500);
        
        assertThat(firstRecord)
                .as("记录应包含订单号字段")
                .containsKey("订单号");
        Waits.sleep(500);
        
        assertThat(firstRecord)
                .as("记录应包含邮箱字段")
                .containsKey("邮箱");
        Waits.sleep(500);
        
        assertThat(firstRecord)
                .as("记录应包含房间类型字段")
                .containsKey("房间类型");
        Waits.sleep(500);
        
        assertThat(firstRecord)
                .as("记录应包含省份字段")
                .containsKey("省份");
        Waits.sleep(500);
        
        assertThat(firstRecord)
                .as("记录应包含预算字段")
                .containsKey("预算");
        Waits.sleep(500);
        
        assertThat(firstRecord)
                .as("记录应包含状态字段")
                .containsKey("状态");
        Waits.sleep(1000);
    }

    @io.qameta.allure.Step("验证刷新按钮（带重试）")
    private void verifyRefreshButtonWithRetry(DashboardPage dashboardPage) {
        boolean visible = executeWithRetry(() -> dashboardPage.isRefreshButtonVisible());
        assertThat(visible)
                .as("刷新按钮应可见")
                .isTrue();
    }

    @io.qameta.allure.Step("验证页面 URL")
    private void verifyPageUrl() {
        String currentUrl = DriverManager.getDriver().getCurrentUrl();
        assertThat(currentUrl)
                .as("URL 应包含 dashboard")
                .contains("dashboard");
        Waits.sleep(500);
    }

    private <T> T executeWithRetry(Supplier<T> action) {
        int maxAttempts = 3;
        for (int i = 0; i < maxAttempts; i++) {
            try {
                return action.get();
            } catch (StaleElementReferenceException e) {
                if (i == maxAttempts - 1) {
                    throw e;
                }
                Waits.sleep(PAGE_REFRESH_INTERVAL_SECONDS * 1000L + 500);
                waitForPageToStabilize();
            } catch (Exception e) {
                if (i == maxAttempts - 1) {
                    throw e;
                }
                Waits.sleep(PAGE_REFRESH_INTERVAL_SECONDS * 1000L + 500);
            }
        }
        throw new RuntimeException("Unexpected error after retries");
    }
}
