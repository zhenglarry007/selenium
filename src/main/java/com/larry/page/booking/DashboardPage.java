package com.larry.page.booking;

import com.larry.driver.DriverManager;
import com.larry.page.AbstractPageObject;
import com.larry.wait.Waits;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.larry.config.ConfigurationManager.configuration;

public class DashboardPage extends AbstractPageObject {

    private final By refreshButton = By.xpath("//button[normalize-space()='刷新数据']");
    private final By totalBookingsLabel = By.xpath("//*[normalize-space()='总预订数']");
    private final By pendingBookingsLabel = By.xpath("//*[normalize-space()='待处理预订']");
    private final By roomTypesLabel = By.xpath("//*[normalize-space()='房间类型数']");
    private final By lastUpdateLabel = By.xpath("//*[normalize-space()='最后更新']");
    private final By bookingRecordTable = By.xpath("//table");
    private final By bookingRecordRows = By.xpath("//table/tbody/tr");
    private final By dashboardLink = By.xpath("//a[normalize-space()='数据仪表盘']");
    
    private final By totalBookingsValue = By.cssSelector(".text-3xl.font-bold.text-gray-800");
    private final By pendingBookingsValue = By.cssSelector(".text-3xl.font-bold.text-orange-600");
    private final By roomTypesValue = By.cssSelector(".text-3xl.font-bold.text-green-600");
    private final By lastUpdateValue = By.cssSelector(".text-xl.font-bold.text-purple-600");

    @Step
    public void navigateToDashboard() {
        var currentUrl = DriverManager.getDriver().getCurrentUrl();
        System.out.println("[DashboardPage.navigateToDashboard] 当前 URL: " + currentUrl);
        
        if (!currentUrl.contains("/dashboard")) {
            String baseUrl = configuration().url();
            if (baseUrl.endsWith("/")) {
                baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
            }
            String targetUrl = baseUrl + "/dashboard";
            System.out.println("[DashboardPage.navigateToDashboard] 导航到: " + targetUrl);
            DriverManager.getDriver().get(targetUrl);
        }
        
        System.out.println("[DashboardPage.navigateToDashboard] 等待 '总预订数' 标签可见...");
        Waits.waitForVisibility(totalBookingsLabel);
        System.out.println("[DashboardPage.navigateToDashboard] '总预订数' 标签已可见");
    }

    @Step
    public void clickRefreshButton() {
        System.out.println("[DashboardPage.clickRefreshButton] 点击刷新按钮...");
        Waits.waitForClickability(refreshButton).click();
        System.out.println("[DashboardPage.clickRefreshButton] 刷新按钮已点击");
    }

    @Step
    public String getTotalBookings() {
        System.out.println("[DashboardPage.getTotalBookings] 获取总预订数...");
        String result = getStatValueByLabel(totalBookingsLabel, totalBookingsValue);
        System.out.println("[DashboardPage.getTotalBookings] 返回值: " + result);
        return result;
    }

    @Step
    public int getTotalBookingsAsInt() {
        System.out.println("[DashboardPage.getTotalBookingsAsInt] 获取总预订数(整数)...");
        String totalBookings = getTotalBookings();
        int result = Integer.parseInt(totalBookings);
        System.out.println("[DashboardPage.getTotalBookingsAsInt] 返回值: " + result);
        return result;
    }

    @Step
    public String getPendingBookings() {
        System.out.println("[DashboardPage.getPendingBookings] 获取待处理预订数...");
        String result = getStatValueByLabel(pendingBookingsLabel, pendingBookingsValue);
        System.out.println("[DashboardPage.getPendingBookings] 返回值: " + result);
        return result;
    }

    @Step
    public int getPendingBookingsAsInt() {
        System.out.println("[DashboardPage.getPendingBookingsAsInt] 获取待处理预订数(整数)...");
        String pendingBookings = getPendingBookings();
        int result = Integer.parseInt(pendingBookings);
        System.out.println("[DashboardPage.getPendingBookingsAsInt] 返回值: " + result);
        return result;
    }

    @Step
    public String getRoomTypesCount() {
        System.out.println("[DashboardPage.getRoomTypesCount] 获取房间类型数...");
        String result = getStatValueByLabel(roomTypesLabel, roomTypesValue);
        System.out.println("[DashboardPage.getRoomTypesCount] 返回值: " + result);
        return result;
    }

    @Step
    public int getRoomTypesCountAsInt() {
        System.out.println("[DashboardPage.getRoomTypesCountAsInt] 获取房间类型数(整数)...");
        String roomTypes = getRoomTypesCount();
        int result = Integer.parseInt(roomTypes);
        System.out.println("[DashboardPage.getRoomTypesCountAsInt] 返回值: " + result);
        return result;
    }

    @Step
    public String getLastUpdateText() {
        System.out.println("[DashboardPage.getLastUpdateText] 获取最后更新时间...");
        Waits.waitForVisibility(lastUpdateLabel);
        String result = Waits.waitForVisibility(lastUpdateValue).getText().trim();
        System.out.println("[DashboardPage.getLastUpdateText] 返回值: " + result);
        return result;
    }

    private String getStatValueByLabel(By labelLocator, By valueLocator) {
        System.out.println("[DashboardPage.getStatValueByLabel] 标签定位器: " + labelLocator);
        System.out.println("[DashboardPage.getStatValueByLabel] 值定位器: " + valueLocator);
        
        Waits.waitForVisibility(labelLocator);
        System.out.println("[DashboardPage.getStatValueByLabel] 标签已可见");
        
        var elements = DriverManager.getDriver().findElements(valueLocator);
        System.out.println("[DashboardPage.getStatValueByLabel] 找到 " + elements.size() + " 个匹配元素");
        
        for (int i = 0; i < elements.size(); i++) {
            var element = elements.get(i);
            var text = element.getText().trim();
            System.out.println("[DashboardPage.getStatValueByLabel] 元素 " + i + " 文本: '" + text + "', 是数字: " + isNumeric(text));
            
            if (isNumeric(text)) {
                String result = Waits.waitForVisibility(element).getText().trim();
                System.out.println("[DashboardPage.getStatValueByLabel] 使用元素 " + i + ", 返回值: " + result);
                return result;
            }
        }
        
        String result = Waits.waitForVisibility(elements.get(elements.size() - 1)).getText().trim();
        System.out.println("[DashboardPage.getStatValueByLabel] 使用最后一个元素, 返回值: " + result);
        return result;
    }

    private boolean isNumeric(String text) {
        if (text == null || text.isEmpty()) {
            System.out.println("[DashboardPage.isNumeric] 文本为空或 null, 返回 false");
            return false;
        }
        try {
            Integer.parseInt(text);
            System.out.println("[DashboardPage.isNumeric] 文本 '" + text + "' 是数字, 返回 true");
            return true;
        } catch (NumberFormatException e) {
            System.out.println("[DashboardPage.isNumeric] 文本 '" + text + "' 不是数字, 返回 false");
            return false;
        }
    }

    private boolean isTimeFormat(String text) {
        if (text == null || text.isEmpty()) {
            System.out.println("[DashboardPage.isTimeFormat] 文本为空或 null, 返回 false");
            return false;
        }
        boolean result = text.matches("\\d{2}:\\d{2}:\\d{2}");
        System.out.println("[DashboardPage.isTimeFormat] 文本 '" + text + "' 是时间格式: " + result);
        return result;
    }

    @Step
    public List<Map<String, String>> getBookingRecords() {
        System.out.println("[DashboardPage.getBookingRecords] 获取预订记录...");
        List<Map<String, String>> records = new ArrayList<>();
        
        var rows = Waits.waitForListToLoad(bookingRecordRows, 1);
        System.out.println("[DashboardPage.getBookingRecords] 找到 " + rows.size() + " 行记录");
        
        var headerRow = DriverManager.getDriver().findElement(By.xpath("//table/thead/tr"));
        var headers = headerRow.findElements(By.tagName("th"));
        List<String> headerNames = new ArrayList<>();
        for (var header : headers) {
            headerNames.add(header.getText().trim());
        }
        System.out.println("[DashboardPage.getBookingRecords] 表头: " + headerNames);

        for (int i = 0; i < rows.size(); i++) {
            var row = rows.get(i);
            Map<String, String> record = new HashMap<>();
            var cells = row.findElements(By.tagName("td"));
            for (int j = 0; j < cells.size() && j < headerNames.size(); j++) {
                record.put(headerNames.get(j), cells.get(j).getText().trim());
            }
            System.out.println("[DashboardPage.getBookingRecords] 记录 " + (i+1) + ": " + record);
            records.add(record);
        }
        
        System.out.println("[DashboardPage.getBookingRecords] 返回 " + records.size() + " 条记录");
        return records;
    }

    @Step
    public int getBookingRecordCount() {
        System.out.println("[DashboardPage.getBookingRecordCount] 获取预订记录数量...");
        try {
            var rows = Waits.waitForListToLoad(bookingRecordRows, 1);
            int result = rows.size();
            System.out.println("[DashboardPage.getBookingRecordCount] 返回值: " + result);
            return result;
        } catch (Exception e) {
            System.out.println("[DashboardPage.getBookingRecordCount] 异常: " + e.getMessage() + ", 返回 0");
            return 0;
        }
    }

    @Step
    public boolean isRefreshButtonVisible() {
        System.out.println("[DashboardPage.isRefreshButtonVisible] 检查刷新按钮是否可见...");
        try {
            boolean result = Waits.waitForVisibility(refreshButton).isDisplayed();
            System.out.println("[DashboardPage.isRefreshButtonVisible] 返回值: " + result);
            return result;
        } catch (Exception e) {
            System.out.println("[DashboardPage.isRefreshButtonVisible] 异常: " + e.getMessage() + ", 返回 false");
            return false;
        }
    }

    @Step
    public boolean isDashboardPageDisplayed() {
        System.out.println("[DashboardPage.isDashboardPageDisplayed] 检查仪表盘页面是否显示...");
        try {
            boolean result = Waits.waitForVisibility(totalBookingsLabel).isDisplayed();
            System.out.println("[DashboardPage.isDashboardPageDisplayed] 返回值: " + result);
            return result;
        } catch (Exception e) {
            System.out.println("[DashboardPage.isDashboardPageDisplayed] 异常: " + e.getMessage() + ", 返回 false");
            return false;
        }
    }
}
