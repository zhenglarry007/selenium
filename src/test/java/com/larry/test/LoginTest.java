package com.larry.test;

import com.larry.BaseWeb;
import com.larry.data.provider.TestDataProvider;
import com.larry.model.LoginTestData;
import com.larry.page.guru99.LoginPage;
import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class LoginTest extends BaseWeb {

    @Test(dataProvider = "loginTestData", dataProviderClass = TestDataProvider.class,
            description = "Guru99 Bank 登录功能测试")
    @Severity(SeverityLevel.CRITICAL)
    @Story("用户登录功能")
    @Description("测试登录功能，包含正向和反向用例")
    public void testLogin(LoginTestData testData) {
        var loginPage = new LoginPage();
        
        loginPage.enterUsername(testData.username());
        loginPage.enterPassword(testData.password());
        loginPage.clickLogin();
        
        if (testData.shouldLoginSuccess()) {
            verifyLoginSuccess(loginPage, testData);
        } else {
            verifyLoginFailure(loginPage, testData);
        }
    }

    @io.qameta.allure.Step("验证登录成功")
    private void verifyLoginSuccess(LoginPage loginPage, LoginTestData testData) {
        String currentTitle = loginPage.getPageTitle();
        assertThat(currentTitle)
                .as("页面标题应包含 Manager HomePage")
                .contains("Manager");
        
        String currentUrl = loginPage.getCurrentUrl();
        assertThat(currentUrl)
                .as("URL 应不包含 login")
                .doesNotContain("login");
    }

    @io.qameta.allure.Step("验证登录失败")
    private void verifyLoginFailure(LoginPage loginPage, LoginTestData testData) {
        String alertText = loginPage.getAlertText();
        assertThat(alertText)
                .as("应显示错误提示")
                .isNotNull();
        
        if (testData.expectedAlertText() != null) {
            assertThat(alertText)
                    .as("错误提示应包含: " + testData.expectedAlertText())
                    .contains(testData.expectedAlertText());
        }
        
        loginPage.acceptAlert();
        
        String currentTitle = loginPage.getPageTitle();
        assertThat(currentTitle)
                .as("页面标题应仍为登录页面")
                .contains("Guru99 Bank");
    }
}
