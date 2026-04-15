package com.larry.model;

public record LoginTestData(
    String testCaseName,
    String username,
    String password,
    String expectedResult,
    String expectedAlertText,
    String expectedPageTitle,
    Boolean shouldLoginSuccess
) {
}
