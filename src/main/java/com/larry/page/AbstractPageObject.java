package com.larry.page;

import com.larry.driver.DriverManager;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;

import static com.larry.config.ConfigurationManager.configuration;
import static org.openqa.selenium.support.PageFactory.initElements;

public class AbstractPageObject {

    protected AbstractPageObject() {
        initElements(new AjaxElementLocatorFactory(DriverManager.getDriver(), configuration().timeout()), this);
    }
}
