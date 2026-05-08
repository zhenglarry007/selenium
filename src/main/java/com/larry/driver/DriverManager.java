package com.larry.driver;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

public class DriverManager {

    private static final ThreadLocal<WebDriver> driver = new ThreadLocal<>();

    private DriverManager() {}

    public static WebDriver getDriver() {
        return driver.get();
    }

    public static void setDriver(WebDriver driver) {
        DriverManager.driver.set(driver);
    }

    public static void quit() {
        WebDriver webDriver = DriverManager.driver.get();
        if (webDriver != null) {
            webDriver.quit();
        }
        driver.remove();
    }

    public static String getInfo() {
        WebDriver webDriver = DriverManager.getDriver();
        if (webDriver == null) {
            return "browser: unknown v: unknown platform: unknown";
        }

        if (!(webDriver instanceof RemoteWebDriver remoteWebDriver)) {
            return String.format("browser: %s v: unknown platform: unknown", webDriver.getClass().getSimpleName());
        }

        var cap = remoteWebDriver.getCapabilities();
        String browserName = cap.getBrowserName();
        String platform = String.valueOf(cap.getPlatformName());
        String version = cap.getBrowserVersion();

        return String.format("browser: %s v: %s platform: %s", browserName, version, platform);
    }
}
