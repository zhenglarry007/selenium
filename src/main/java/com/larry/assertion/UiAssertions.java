package com.larry.assertion;

import com.larry.driver.DriverManager;
import com.larry.wait.Waits;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static org.assertj.core.api.Assertions.assertThat;

public final class UiAssertions {

    private UiAssertions() {
    }

    @Step("Assert element is visible: {locator}")
    public static void assertElementVisible(By locator) {
        WebElement element = Waits.waitForVisibility(locator);
        assertThat(element.isDisplayed())
                .as("Element should be visible: " + locator)
                .isTrue();
    }

    @Step("Assert element is visible: {element}")
    public static void assertElementVisible(WebElement element) {
        Waits.waitForVisibility(element);
        assertThat(element.isDisplayed())
                .as("Element should be visible")
                .isTrue();
    }

    @Step("Assert element is not visible: {locator}")
    public static void assertElementNotVisible(By locator) {
        boolean isInvisible = Waits.waitForInvisibility(locator);
        assertThat(isInvisible)
                .as("Element should not be visible: " + locator)
                .isTrue();
    }

    @Step("Assert element is not visible: {element}")
    public static void assertElementNotVisible(WebElement element) {
        boolean isInvisible = Waits.waitForInvisibility(element);
        assertThat(isInvisible)
                .as("Element should not be visible")
                .isTrue();
    }

    @Step("Assert element is present: {locator}")
    public static void assertElementPresent(By locator) {
        WebElement element = Waits.waitForPresence(locator);
        assertThat(element)
                .as("Element should be present in DOM: " + locator)
                .isNotNull();
    }

    @Step("Assert element is enabled: {locator}")
    public static void assertElementEnabled(By locator) {
        WebElement element = Waits.waitForVisibility(locator);
        assertThat(element.isEnabled())
                .as("Element should be enabled: " + locator)
                .isTrue();
    }

    @Step("Assert element is enabled: {element}")
    public static void assertElementEnabled(WebElement element) {
        Waits.waitForVisibility(element);
        assertThat(element.isEnabled())
                .as("Element should be enabled")
                .isTrue();
    }

    @Step("Assert element is disabled: {locator}")
    public static void assertElementDisabled(By locator) {
        WebElement element = Waits.waitForVisibility(locator);
        assertThat(element.isEnabled())
                .as("Element should be disabled: " + locator)
                .isFalse();
    }

    @Step("Assert element is disabled: {element}")
    public static void assertElementDisabled(WebElement element) {
        Waits.waitForVisibility(element);
        assertThat(element.isEnabled())
                .as("Element should be disabled")
                .isFalse();
    }

    @Step("Assert element is selected: {locator}")
    public static void assertElementSelected(By locator) {
        Waits.waitForElementToBeSelected(locator);
        WebElement element = DriverManager.getDriver().findElement(locator);
        assertThat(element.isSelected())
                .as("Element should be selected: " + locator)
                .isTrue();
    }

    @Step("Assert element is selected: {element}")
    public static void assertElementSelected(WebElement element) {
        Waits.waitForElementToBeSelected(element);
        assertThat(element.isSelected())
                .as("Element should be selected")
                .isTrue();
    }

    @Step("Assert element is not selected: {locator}")
    public static void assertElementNotSelected(By locator) {
        WebElement element = Waits.waitForVisibility(locator);
        assertThat(element.isSelected())
                .as("Element should not be selected: " + locator)
                .isFalse();
    }

    @Step("Assert element is not selected: {element}")
    public static void assertElementNotSelected(WebElement element) {
        Waits.waitForVisibility(element);
        assertThat(element.isSelected())
                .as("Element should not be selected")
                .isFalse();
    }

    @Step("Assert text equals: expected='{expectedText}'")
    public static void assertTextEquals(By locator, String expectedText) {
        Waits.waitForTextToBe(locator, expectedText);
        String actualText = DriverManager.getDriver().findElement(locator).getText();
        assertThat(actualText)
                .as("Text should equal: " + expectedText)
                .isEqualTo(expectedText);
    }

    @Step("Assert text equals: expected='{expectedText}'")
    public static void assertTextEquals(WebElement element, String expectedText) {
        Waits.waitForVisibility(element);
        String actualText = element.getText();
        assertThat(actualText)
                .as("Text should equal: " + expectedText)
                .isEqualTo(expectedText);
    }

    @Step("Assert text contains: expected='{expectedText}'")
    public static void assertTextContains(By locator, String expectedText) {
        Waits.waitForTextToBePresentInElement(locator, expectedText);
        String actualText = DriverManager.getDriver().findElement(locator).getText();
        assertThat(actualText)
                .as("Text should contain: " + expectedText)
                .contains(expectedText);
    }

    @Step("Assert text contains: expected='{expectedText}'")
    public static void assertTextContains(WebElement element, String expectedText) {
        Waits.waitForVisibility(element);
        String actualText = element.getText();
        assertThat(actualText)
                .as("Text should contain: " + expectedText)
                .contains(expectedText);
    }

    @Step("Assert text does not contain: unexpected='{unexpectedText}'")
    public static void assertTextDoesNotContain(By locator, String unexpectedText) {
        WebElement element = Waits.waitForVisibility(locator);
        String actualText = element.getText();
        assertThat(actualText)
                .as("Text should not contain: " + unexpectedText)
                .doesNotContain(unexpectedText);
    }

    @Step("Assert text does not contain: unexpected='{unexpectedText}'")
    public static void assertTextDoesNotContain(WebElement element, String unexpectedText) {
        Waits.waitForVisibility(element);
        String actualText = element.getText();
        assertThat(actualText)
                .as("Text should not contain: " + unexpectedText)
                .doesNotContain(unexpectedText);
    }

    @Step("Assert text matches pattern: {pattern}")
    public static void assertTextMatches(By locator, String pattern) {
        WebElement element = Waits.waitForVisibility(locator);
        String actualText = element.getText();
        assertThat(actualText)
                .as("Text should match pattern: " + pattern)
                .matches(pattern);
    }

    @Step("Assert text matches pattern: {pattern}")
    public static void assertTextMatches(WebElement element, String pattern) {
        Waits.waitForVisibility(element);
        String actualText = element.getText();
        assertThat(actualText)
                .as("Text should match pattern: " + pattern)
                .matches(pattern);
    }

    @Step("Assert attribute equals: {attribute}='{expectedValue}'")
    public static void assertAttributeEquals(By locator, String attribute, String expectedValue) {
        Waits.waitForAttributeToBe(locator, attribute, expectedValue);
        String actualValue = DriverManager.getDriver().findElement(locator).getAttribute(attribute);
        assertThat(actualValue)
                .as("Attribute '" + attribute + "' should equal: " + expectedValue)
                .isEqualTo(expectedValue);
    }

    @Step("Assert attribute equals: {attribute}='{expectedValue}'")
    public static void assertAttributeEquals(WebElement element, String attribute, String expectedValue) {
        Waits.waitForVisibility(element);
        String actualValue = element.getAttribute(attribute);
        assertThat(actualValue)
                .as("Attribute '" + attribute + "' should equal: " + expectedValue)
                .isEqualTo(expectedValue);
    }

    @Step("Assert attribute contains: {attribute} contains '{expectedValue}'")
    public static void assertAttributeContains(By locator, String attribute, String expectedValue) {
        Waits.waitForAttributeContains(locator, attribute, expectedValue);
        String actualValue = DriverManager.getDriver().findElement(locator).getAttribute(attribute);
        assertThat(actualValue)
                .as("Attribute '" + attribute + "' should contain: " + expectedValue)
                .contains(expectedValue);
    }

    @Step("Assert attribute contains: {attribute} contains '{expectedValue}'")
    public static void assertAttributeContains(WebElement element, String attribute, String expectedValue) {
        Waits.waitForVisibility(element);
        String actualValue = element.getAttribute(attribute);
        assertThat(actualValue)
                .as("Attribute '" + attribute + "' should contain: " + expectedValue)
                .contains(expectedValue);
    }

    @Step("Assert CSS value equals: {property}='{expectedValue}'")
    public static void assertCssValueEquals(By locator, String property, String expectedValue) {
        WebElement element = Waits.waitForVisibility(locator);
        String actualValue = element.getCssValue(property);
        assertThat(actualValue)
                .as("CSS property '" + property + "' should equal: " + expectedValue)
                .isEqualTo(expectedValue);
    }

    @Step("Assert CSS value equals: {property}='{expectedValue}'")
    public static void assertCssValueEquals(WebElement element, String property, String expectedValue) {
        Waits.waitForVisibility(element);
        String actualValue = element.getCssValue(property);
        assertThat(actualValue)
                .as("CSS property '" + property + "' should equal: " + expectedValue)
                .isEqualTo(expectedValue);
    }

    @Step("Assert element count: expected={expectedCount}")
    public static void assertElementCount(By locator, int expectedCount) {
        var elements = Waits.waitForListToLoad(locator, expectedCount);
        assertThat(elements.size())
                .as("Number of elements should be: " + expectedCount)
                .isEqualTo(expectedCount);
    }

    @Step("Assert element count at least: minimum={minimumCount}")
    public static void assertElementCountAtLeast(By locator, int minimumCount) {
        var elements = Waits.waitForListToLoad(locator, minimumCount);
        assertThat(elements.size())
                .as("Number of elements should be at least: " + minimumCount)
                .isGreaterThanOrEqualTo(minimumCount);
    }

    @Step("Assert URL contains: {expectedFraction}")
    public static void assertUrlContains(String expectedFraction) {
        Waits.waitForUrlToContain(expectedFraction);
        String currentUrl = DriverManager.getDriver().getCurrentUrl();
        assertThat(currentUrl)
                .as("URL should contain: " + expectedFraction)
                .contains(expectedFraction);
    }

    @Step("Assert URL equals: {expectedUrl}")
    public static void assertUrlEquals(String expectedUrl) {
        Waits.waitForUrlToBe(expectedUrl);
        String currentUrl = DriverManager.getDriver().getCurrentUrl();
        assertThat(currentUrl)
                .as("URL should equal: " + expectedUrl)
                .isEqualTo(expectedUrl);
    }

    @Step("Assert title contains: {expectedFraction}")
    public static void assertTitleContains(String expectedFraction) {
        Waits.waitForTitleToContain(expectedFraction);
        String currentTitle = DriverManager.getDriver().getTitle();
        assertThat(currentTitle)
                .as("Title should contain: " + expectedFraction)
                .contains(expectedFraction);
    }

    @Step("Assert title equals: {expectedTitle}")
    public static void assertTitleEquals(String expectedTitle) {
        Waits.waitForTitleToBe(expectedTitle);
        String currentTitle = DriverManager.getDriver().getTitle();
        assertThat(currentTitle)
                .as("Title should equal: " + expectedTitle)
                .isEqualTo(expectedTitle);
    }

    @Step("Assert element is displayed: {locator}")
    public static void assertDisplayed(By locator) {
        assertElementVisible(locator);
    }

    @Step("Assert element is displayed: {element}")
    public static void assertDisplayed(WebElement element) {
        assertElementVisible(element);
    }

    @Step("Assert element is not displayed: {locator}")
    public static void assertNotDisplayed(By locator) {
        assertElementNotVisible(locator);
    }

    @Step("Assert element is not displayed: {element}")
    public static void assertNotDisplayed(WebElement element) {
        assertElementNotVisible(element);
    }
}
