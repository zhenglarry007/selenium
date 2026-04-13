package com.larry.report;

import com.larry.driver.DriverManager;
import io.qameta.allure.Allure;
import io.qameta.allure.Attachment;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.events.WebDriverListener;

import java.util.Arrays;

public class SeleniumActionListener implements WebDriverListener {

    @Override
    public void afterGet(WebDriver driver, String url) {
        logStepWithScreenshot(driver, "Open URL: " + url);
    }

    @Override
    public void afterClick(WebElement element) {
        logStepWithScreenshot(DriverManager.getDriver(), "Click element: " + describeElement(element));
    }

    @Override
    public void afterSendKeys(WebElement element, CharSequence... keysToSend) {
        var value = Arrays.stream(keysToSend)
                .map(CharSequence::toString)
                .reduce("", String::concat);
        logStepWithScreenshot(DriverManager.getDriver(), "Type into element: " + describeElement(element) + " value: " + mask(value));
    }

    @Override
    public void afterClear(WebElement element) {
        logStepWithScreenshot(DriverManager.getDriver(), "Clear element: " + describeElement(element));
    }

    @Override
    public void afterTo(WebDriver.Navigation navigation, String url) {
        logStepWithScreenshot(DriverManager.getDriver(), "Navigate to: " + url);
    }

    @Override
    public void afterBack(WebDriver.Navigation navigation) {
        logStepWithScreenshot(DriverManager.getDriver(), "Navigate back");
    }

    @Override
    public void afterForward(WebDriver.Navigation navigation) {
        logStepWithScreenshot(DriverManager.getDriver(), "Navigate forward");
    }

    @Override
    public void afterRefresh(WebDriver.Navigation navigation) {
        logStepWithScreenshot(DriverManager.getDriver(), "Refresh current page");
    }

    private void logStepWithScreenshot(WebDriver driver, String message) {
        if (driver == null) {
            return;
        }
        Allure.step(message);
        saveScreenshot(driver, "Selenium Action");
    }

    private String describeElement(WebElement element) {
        try {
            if (element instanceof org.openqa.selenium.WrapsElement wrappedElement) {
                element = wrappedElement.getWrappedElement();
            }
            var id = element.getAttribute("id");
            if (id != null && !id.isBlank()) {
                return "id=" + id;
            }

            var name = element.getAttribute("name");
            if (name != null && !name.isBlank()) {
                return "name=" + name;
            }

            var cssClass = element.getAttribute("class");
            if (cssClass != null && !cssClass.isBlank()) {
                return "class=" + cssClass;
            }

            return "tag=" + element.getTagName();
        } catch (Exception e) {
            return "unknown element";
        }
    }

    private String mask(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return "<empty>";
        }
        return "*".repeat(Math.min(rawValue.length(), 8));
    }

    @Attachment(value = "{attachmentName}", type = "image/png")
    public byte[] saveScreenshot(WebDriver driver, String attachmentName) {
        if (!(driver instanceof TakesScreenshot screenshotDriver)) {
            return new byte[0];
        }
        return screenshotDriver.getScreenshotAs(OutputType.BYTES);
    }
}
