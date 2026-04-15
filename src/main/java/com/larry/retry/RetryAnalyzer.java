package com.larry.retry;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

import static com.larry.config.ConfigurationManager.configuration;

public class RetryAnalyzer implements IRetryAnalyzer {

    private int retryCount = 0;
    private final int maxRetryCount;

    public RetryAnalyzer() {
        var env = configuration().environment();
        if ("ci".equalsIgnoreCase(env)) {
            this.maxRetryCount = configuration().retryCi();
        } else {
            this.maxRetryCount = configuration().retryLocal();
        }
    }

    @Override
    public boolean retry(ITestResult result) {
        if (retryCount < maxRetryCount) {
            retryCount++;
            RetryContext.setCurrentRetry(retryCount);
            return true;
        }
        RetryContext.clear();
        return false;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public int getMaxRetryCount() {
        return maxRetryCount;
    }
}
