package com.larry.report;

import com.larry.driver.DriverManager;
import com.larry.retry.RetryContext;
import io.qameta.allure.Attachment;
import io.qameta.allure.listener.TestLifecycleListener;
import io.qameta.allure.model.TestResult;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import static io.qameta.allure.model.Status.BROKEN;
import static io.qameta.allure.model.Status.FAILED;
import static io.qameta.allure.model.Status.PASSED;

public class AllureTestLifecycleListener implements TestLifecycleListener {

    public AllureTestLifecycleListener() {
    }

    @Attachment(value = "Page Screenshot", type = "image/png")
    public byte[] saveScreenshot(WebDriver driver) {
        return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
    }

    @Override
    public void beforeTestStop(TestResult result) {
        if (RetryContext.isRetry()) {
            var retryCount = RetryContext.getCurrentRetry();
            result.setName(result.getName() + " [Retry " + retryCount + "]");
            result.setDescription((result.getDescription() == null ? "" : result.getDescription()) + 
                " | This is retry attempt #" + retryCount);
        }

        if (FAILED == result.getStatus() || BROKEN == result.getStatus()) {
            saveScreenshot(DriverManager.getDriver());
            AllureFailureAttachment.attachAllFailureInfo();
        }

        if (PASSED == result.getStatus() && RetryContext.isRetry()) {
            RetryContext.clear();
        }
    }
}
