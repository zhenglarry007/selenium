package com.larry.report;

import com.larry.driver.DriverManager;
import io.qameta.allure.Attachment;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class AllureFailureAttachment {

    private AllureFailureAttachment() {
    }

    public static void attachAllFailureInfo() {
        attachPageSource();
        attachBrowserConsoleLogs();
        attachNetworkErrors();
    }

    @Attachment(value = "Page Source", type = "text/html")
    public static String attachPageSource() {
        try {
            WebDriver driver = DriverManager.getDriver();
            if (driver != null) {
                String pageSource = driver.getPageSource();
                return truncatePageSource(pageSource);
            }
        } catch (Exception e) {
            return "Failed to get page source: " + e.getMessage();
        }
        return "Page source not available";
    }

    @Attachment(value = "Browser Console Logs", type = "text/plain")
    public static String attachBrowserConsoleLogs() {
        try {
            WebDriver driver = DriverManager.getDriver();
            if (driver != null) {
                var logs = driver.manage().logs();
                if (logs != null) {
                    List<LogEntry> allLogs = new ArrayList<>();
                    
                    try {
                        var browserLogs = logs.get(LogType.BROWSER);
                        if (browserLogs != null) {
                            allLogs.addAll(browserLogs.getAll());
                        }
                    } catch (Exception e) {
                    }
                    
                    try {
                        var driverLogs = logs.get(LogType.DRIVER);
                        if (driverLogs != null) {
                            allLogs.addAll(driverLogs.getAll());
                        }
                    } catch (Exception e) {
                    }

                    if (!allLogs.isEmpty()) {
                        return formatLogEntries(allLogs);
                    }
                }
            }
        } catch (Exception e) {
            return "Failed to get browser console logs: " + e.getMessage();
        }
        return "No browser console logs available";
    }

    @Attachment(value = "Network Errors", type = "text/plain")
    public static String attachNetworkErrors() {
        try {
            WebDriver driver = DriverManager.getDriver();
            if (driver instanceof JavascriptExecutor jsExecutor) {
                String script = 
                    "var performance = window.performance || window.webkitPerformance || window.msPerformance || window.mozPerformance;" +
                    "if (performance) {" +
                    "  var entries = performance.getEntriesByType('resource');" +
                    "  var errors = [];" +
                    "  for (var i = 0; i < entries.length; i++) {" +
                    "    var entry = entries[i];" +
                    "    if (entry.responseStatus && entry.responseStatus >= 400) {" +
                    "      errors.push({" +
                    "        name: entry.name," +
                    "        status: entry.responseStatus," +
                    "        type: entry.initiatorType" +
                    "      });" +
                    "    }" +
                    "  }" +
                    "  return JSON.stringify(errors);" +
                    "}" +
                    "return '[]';";
                
                Object result = jsExecutor.executeScript(script);
                if (result != null && !result.toString().equals("[]")) {
                    return formatNetworkErrors(result.toString());
                }
                
                return getConsoleErrorsFromLogs();
            }
        } catch (Exception e) {
            return "Failed to get network errors: " + e.getMessage();
        }
        return "No network errors detected";
    }

    private static String getConsoleErrorsFromLogs() {
        try {
            WebDriver driver = DriverManager.getDriver();
            if (driver != null) {
                var logs = driver.manage().logs();
                if (logs != null) {
                    try {
                        var browserLogs = logs.get(LogType.BROWSER);
                        if (browserLogs != null) {
                            var errorLogs = browserLogs.getAll().stream()
                                    .filter(entry -> entry.getLevel().toString().equals("SEVERE") 
                                            || entry.getLevel().toString().equals("WARNING"))
                                    .collect(Collectors.toList());
                            
                            if (!errorLogs.isEmpty()) {
                                return "Console errors/warnings found:\n\n" + formatLogEntries(errorLogs);
                            }
                        }
                    } catch (Exception e) {
                    }
                }
            }
        } catch (Exception e) {
        }
        return "No network errors detected";
    }

    private static String formatLogEntries(List<LogEntry> entries) {
        StringBuilder sb = new StringBuilder();
        sb.append("Total log entries: ").append(entries.size()).append("\n\n");
        
        for (LogEntry entry : entries) {
            sb.append("[").append(entry.getLevel()).append("] ");
            sb.append(new java.util.Date(entry.getTimestamp())).append("\n");
            sb.append(entry.getMessage()).append("\n\n");
        }
        
        return sb.toString();
    }

    private static String formatNetworkErrors(String jsonErrors) {
        StringBuilder sb = new StringBuilder();
        sb.append("Network errors detected:\n\n");
        sb.append(jsonErrors);
        return sb.toString();
    }

    private static String truncatePageSource(String pageSource) {
        int maxLength = 500000;
        if (pageSource.length() > maxLength) {
            return pageSource.substring(0, maxLength) + 
                "\n\n... [Page source truncated - original length: " + pageSource.length() + " characters]";
        }
        return pageSource;
    }
}
