package com.larry.retry;

public class RetryContext {

    private static final ThreadLocal<Integer> currentRetry = new ThreadLocal<>();

    private RetryContext() {
    }

    public static void setCurrentRetry(int retry) {
        currentRetry.set(retry);
    }

    public static Integer getCurrentRetry() {
        return currentRetry.get();
    }

    public static void clear() {
        currentRetry.remove();
    }

    public static boolean isRetry() {
        return currentRetry.get() != null && currentRetry.get() > 0;
    }
}
