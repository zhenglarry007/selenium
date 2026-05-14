package com.larry.wait;

import com.larry.driver.DriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.function.Function;

import static com.larry.config.ConfigurationManager.configuration;

public final class Waits {

    private Waits() {
    }

    private static WebDriverWait getWait() {
        return new WebDriverWait(DriverManager.getDriver(), Duration.ofSeconds(configuration().timeout()));
    }

    private static WebDriverWait getWait(long timeoutInSeconds) {
        return new WebDriverWait(DriverManager.getDriver(), Duration.ofSeconds(timeoutInSeconds));
    }

    public static WebElement waitForVisibility(By locator) {
        return getWait().until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public static WebElement waitForVisibility(By locator, long timeoutInSeconds) {
        return getWait(timeoutInSeconds).until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public static WebElement waitForVisibility(WebElement element) {
        return getWait().until(ExpectedConditions.visibilityOf(element));
    }

    public static WebElement waitForVisibility(WebElement element, long timeoutInSeconds) {
        return getWait(timeoutInSeconds).until(ExpectedConditions.visibilityOf(element));
    }

    public static List<WebElement> waitForVisibilityOfAllElements(By locator) {
        return getWait().until(ExpectedConditions.visibilityOfAllElementsLocatedBy(locator));
    }

    public static List<WebElement> waitForVisibilityOfAllElements(By locator, long timeoutInSeconds) {
        return getWait(timeoutInSeconds).until(ExpectedConditions.visibilityOfAllElementsLocatedBy(locator));
    }

    public static WebElement waitForClickability(By locator) {
        return getWait().until(ExpectedConditions.elementToBeClickable(locator));
    }

    public static WebElement waitForClickability(By locator, long timeoutInSeconds) {
        return getWait(timeoutInSeconds).until(ExpectedConditions.elementToBeClickable(locator));
    }

    public static WebElement waitForClickability(WebElement element) {
        return getWait().until(ExpectedConditions.elementToBeClickable(element));
    }

    public static WebElement waitForClickability(WebElement element, long timeoutInSeconds) {
        return getWait(timeoutInSeconds).until(ExpectedConditions.elementToBeClickable(element));
    }

    public static WebElement waitForPresence(By locator) {
        return getWait().until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    public static WebElement waitForPresence(By locator, long timeoutInSeconds) {
        return getWait(timeoutInSeconds).until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    public static List<WebElement> waitForPresenceOfAllElements(By locator) {
        return getWait().until(ExpectedConditions.presenceOfAllElementsLocatedBy(locator));
    }

    public static List<WebElement> waitForPresenceOfAllElements(By locator, long timeoutInSeconds) {
        return getWait(timeoutInSeconds).until(ExpectedConditions.presenceOfAllElementsLocatedBy(locator));
    }

    public static Boolean waitForInvisibility(By locator) {
        return getWait().until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    public static Boolean waitForInvisibility(By locator, long timeoutInSeconds) {
        return getWait(timeoutInSeconds).until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    public static Boolean waitForInvisibility(WebElement element) {
        return getWait().until(ExpectedConditions.invisibilityOf(element));
    }

    public static Boolean waitForInvisibility(WebElement element, long timeoutInSeconds) {
        return getWait(timeoutInSeconds).until(ExpectedConditions.invisibilityOf(element));
    }

    public static Boolean waitForTextToBe(By locator, String expectedText) {
        return getWait().until(ExpectedConditions.textToBe(locator, expectedText));
    }

    public static Boolean waitForTextToBe(By locator, String expectedText, long timeoutInSeconds) {
        return getWait(timeoutInSeconds).until(ExpectedConditions.textToBe(locator, expectedText));
    }

    public static Boolean waitForTextToBePresentInElement(By locator, String text) {
        return getWait().until(ExpectedConditions.textToBePresentInElementLocated(locator, text));
    }

    public static Boolean waitForTextToBePresentInElement(By locator, String text, long timeoutInSeconds) {
        return getWait(timeoutInSeconds).until(ExpectedConditions.textToBePresentInElementLocated(locator, text));
    }

    public static Boolean waitForTextToChange(By locator, String initialText) {
        return getWait().until(ExpectedConditions.not(ExpectedConditions.textToBe(locator, initialText)));
    }

    public static Boolean waitForTextToChange(By locator, String initialText, long timeoutInSeconds) {
        return getWait(timeoutInSeconds).until(ExpectedConditions.not(ExpectedConditions.textToBe(locator, initialText)));
    }

    public static Boolean waitForAttributeToBe(By locator, String attribute, String value) {
        return getWait().until(ExpectedConditions.attributeToBe(locator, attribute, value));
    }

    public static Boolean waitForAttributeToBe(By locator, String attribute, String value, long timeoutInSeconds) {
        return getWait(timeoutInSeconds).until(ExpectedConditions.attributeToBe(locator, attribute, value));
    }

    public static Boolean waitForAttributeContains(By locator, String attribute, String value) {
        return getWait().until(ExpectedConditions.attributeContains(locator, attribute, value));
    }

    public static Boolean waitForAttributeContains(By locator, String attribute, String value, long timeoutInSeconds) {
        return getWait(timeoutInSeconds).until(ExpectedConditions.attributeContains(locator, attribute, value));
    }

    public static List<WebElement> waitForListToLoad(By locator, int expectedMinimumSize) {
        return getWait().until(driver -> {
            List<WebElement> elements = driver.findElements(locator);
            return elements.size() >= expectedMinimumSize ? elements : null;
        });
    }

    public static List<WebElement> waitForListToLoad(By locator, int expectedMinimumSize, long timeoutInSeconds) {
        return getWait(timeoutInSeconds).until(driver -> {
            List<WebElement> elements = driver.findElements(locator);
            return elements.size() >= expectedMinimumSize ? elements : null;
        });
    }

    public static List<WebElement> waitForListToBeNotEmpty(By locator) {
        return waitForListToLoad(locator, 1);
    }

    public static List<WebElement> waitForListToBeNotEmpty(By locator, long timeoutInSeconds) {
        return waitForListToLoad(locator, 1, timeoutInSeconds);
    }

    public static Boolean waitForUrlToContain(String fraction) {
        return getWait().until(ExpectedConditions.urlContains(fraction));
    }

    public static Boolean waitForUrlToContain(String fraction, long timeoutInSeconds) {
        return getWait(timeoutInSeconds).until(ExpectedConditions.urlContains(fraction));
    }

    public static Boolean waitForUrlToBe(String expectedUrl) {
        return getWait().until(ExpectedConditions.urlToBe(expectedUrl));
    }

    public static Boolean waitForUrlToBe(String expectedUrl, long timeoutInSeconds) {
        return getWait(timeoutInSeconds).until(ExpectedConditions.urlToBe(expectedUrl));
    }

    public static Boolean waitForTitleToBe(String expectedTitle) {
        return getWait().until(ExpectedConditions.titleIs(expectedTitle));
    }

    public static Boolean waitForTitleToBe(String expectedTitle, long timeoutInSeconds) {
        return getWait(timeoutInSeconds).until(ExpectedConditions.titleIs(expectedTitle));
    }

    public static Boolean waitForTitleToContain(String titleFraction) {
        return getWait().until(ExpectedConditions.titleContains(titleFraction));
    }

    public static Boolean waitForTitleToContain(String titleFraction, long timeoutInSeconds) {
        return getWait(timeoutInSeconds).until(ExpectedConditions.titleContains(titleFraction));
    }

    public static Boolean waitForElementToBeSelected(By locator) {
        return getWait().until(ExpectedConditions.elementToBeSelected(locator));
    }

    public static Boolean waitForElementToBeSelected(By locator, long timeoutInSeconds) {
        return getWait(timeoutInSeconds).until(ExpectedConditions.elementToBeSelected(locator));
    }

    public static Boolean waitForElementToBeSelected(WebElement element) {
        return getWait().until(ExpectedConditions.elementToBeSelected(element));
    }

    public static Boolean waitForElementToBeSelected(WebElement element, long timeoutInSeconds) {
        return getWait(timeoutInSeconds).until(ExpectedConditions.elementToBeSelected(element));
    }

    public static WebDriver waitForFrameToBeAvailableAndSwitchToIt(By locator) {
        return getWait().until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(locator));
    }

    public static WebDriver waitForFrameToBeAvailableAndSwitchToIt(By locator, long timeoutInSeconds) {
        return getWait(timeoutInSeconds).until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(locator));
    }

    public static Boolean waitForNumberOfWindowsToBe(int expectedNumberOfWindows) {
        return getWait().until(ExpectedConditions.numberOfWindowsToBe(expectedNumberOfWindows));
    }

    public static Boolean waitForNumberOfWindowsToBe(int expectedNumberOfWindows, long timeoutInSeconds) {
        return getWait(timeoutInSeconds).until(ExpectedConditions.numberOfWindowsToBe(expectedNumberOfWindows));
    }

    public static <T> T waitForCustomCondition(Function<WebDriver, T> condition) {
        return getWait().until(condition);
    }

    public static <T> T waitForCustomCondition(Function<WebDriver, T> condition, long timeoutInSeconds) {
        return getWait(timeoutInSeconds).until(condition);
    }

    public static void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Thread was interrupted during sleep", e);
        }
    }
}
