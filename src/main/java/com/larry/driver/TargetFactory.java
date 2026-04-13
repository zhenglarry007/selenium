package com.larry.driver;

import com.larry.enums.Target;
import com.larry.report.SeleniumActionListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.events.EventFiringDecorator;

import java.net.URI;

import static com.larry.config.ConfigurationManager.configuration;
import static com.larry.driver.BrowserFactory.valueOf;
import static java.lang.String.format;

public class TargetFactory {

    private static final Logger logger = LogManager.getLogger(TargetFactory.class);

    public WebDriver createInstance(String browser) {
        Target target = Target.get(configuration().target().toUpperCase());

        WebDriver driver = switch (target) {
            case LOCAL -> valueOf(configuration().browser().toUpperCase()).createLocalDriver();
            case LOCAL_SUITE -> valueOf(browser.toUpperCase()).createLocalDriver();
            case SELENIUM_GRID -> createRemoteInstance(valueOf(browser.toUpperCase()).getOptions());
            case TESTCONTAINERS -> valueOf(configuration().browser().toUpperCase()).createTestContainerDriver();
        };

        return decorateWithActionListener(driver);
    }

    private RemoteWebDriver createRemoteInstance(MutableCapabilities capability) {
        RemoteWebDriver remoteWebDriver = null;
        try {
            String gridURL = format("http://%s:%s", configuration().gridUrl(), configuration().gridPort());

            remoteWebDriver = new RemoteWebDriver(URI.create(gridURL).toURL(), capability);
        } catch (java.net.MalformedURLException e) {
            logger.error("Grid URL is invalid or Grid is not available");
            logger.error("Browser: {}", capability.getBrowserName(), e);
        } catch (IllegalArgumentException e) {
            logger.error("Browser {} is not valid or recognized", capability.getBrowserName(), e);
        }

        return remoteWebDriver;
    }

    private WebDriver decorateWithActionListener(WebDriver driver) {
        if (driver == null) {
            return null;
        }
        return new EventFiringDecorator<WebDriver>(new SeleniumActionListener()).decorate(driver);
    }
}
