package com.larry.page.opennana;

import com.larry.driver.DriverManager;
import com.larry.page.AbstractPageObject;
import com.larry.wait.Waits;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

public class OpenNanaGalleryPage extends AbstractPageObject {

    private final By galleryContainer = By.cssSelector("div.flex.-ml-4.w-auto");
    private final By imageCards = By.cssSelector("div.flex.-ml-4.w-auto > div");
    private final By cardImage = By.cssSelector("div.relative.overflow-hidden > img");
    private final By cardTitle = By.cssSelector("h3, h4");

    private static final String GALLERY_URL = "https://opennana.com/awesome-prompt-gallery?media_type=image&model=ChatGPT";

    @Step("Navigate to OpenNana gallery page")
    public void navigateToGallery() {
        System.out.println("[OpenNanaGalleryPage] Navigating to: " + GALLERY_URL);
        DriverManager.getDriver().get(GALLERY_URL);
        Waits.sleep(2000);
        System.out.println("[OpenNanaGalleryPage] Page loaded");
    }

    @Step("Navigate to URL: {url}")
    public void navigateToUrl(String url) {
        System.out.println("[OpenNanaGalleryPage] Navigating to: " + url);
        DriverManager.getDriver().get(url);
        Waits.sleep(2000);
        System.out.println("[OpenNanaGalleryPage] Page loaded");
    }

    @Step("Scroll down to load more content")
    public void scrollDown() {
        System.out.println("[OpenNanaGalleryPage] Scrolling down to load more content...");
        JavascriptExecutor js = (JavascriptExecutor) DriverManager.getDriver();
        js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
        waitForLoading();
        System.out.println("[OpenNanaGalleryPage] Scroll completed");
    }

    @Step("Wait for loading to complete")
    public void waitForLoading() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Step("Get all image cards on page")
    public List<WebElement> getImageCards() {
        try {
            List<WebElement> cards = DriverManager.getDriver().findElements(imageCards);
            System.out.println("[OpenNanaGalleryPage] Found " + cards.size() + " image cards");
            return cards;
        } catch (Exception e) {
            System.out.println("[OpenNanaGalleryPage] Error getting image cards: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Step("Get image URL from card")
    public String getImageUrlFromCard(WebElement card) {
        try {
            WebElement img = card.findElement(cardImage);
            String src = img.getAttribute("src");
            System.out.println("[OpenNanaGalleryPage] Image URL: " + src);
            return src;
        } catch (Exception e) {
            System.out.println("[OpenNanaGalleryPage] Error getting image URL: " + e.getMessage());
            return null;
        }
    }

    @Step("Get title from card")
    public String getTitleFromCard(WebElement card) {
        try {
            WebElement titleElement = card.findElement(cardTitle);
            String title = titleElement.getText().trim();
            System.out.println("[OpenNanaGalleryPage] Card title: " + title);
            return title;
        } catch (Exception e) {
            System.out.println("[OpenNanaGalleryPage] Error getting card title: " + e.getMessage());
            return null;
        }
    }

    @Step("Click on card to open detail page")
    public void clickCard(WebElement card) {
        System.out.println("[OpenNanaGalleryPage] Clicking on card...");
        try {
            Waits.waitForClickability(card).click();
            Waits.sleep(2000);
            System.out.println("[OpenNanaGalleryPage] Card clicked, detail page should be open");
        } catch (Exception e) {
            System.out.println("[OpenNanaGalleryPage] Error clicking card: " + e.getMessage());
        }
    }

    @Step("Scroll to load {targetCount} cards")
    public void scrollToLoadCards(int targetCount) {
        System.out.println("[OpenNanaGalleryPage] Attempting to load " + targetCount + " cards...");
        int previousCount = 0;
        int currentCount = getImageCards().size();
        int scrollAttempts = 0;
        int maxScrollAttempts = 20;

        while (currentCount < targetCount && scrollAttempts < maxScrollAttempts && currentCount != previousCount) {
            previousCount = currentCount;
            scrollDown();
            currentCount = getImageCards().size();
            scrollAttempts++;
            System.out.println("[OpenNanaGalleryPage] Scroll " + scrollAttempts + ": " + currentCount + " cards loaded");
        }

        if (currentCount >= targetCount) {
            System.out.println("[OpenNanaGalleryPage] Successfully loaded " + currentCount + " cards (target: " + targetCount + ")");
        } else {
            System.out.println("[OpenNanaGalleryPage] Could only load " + currentCount + " cards after " + scrollAttempts + " scrolls (target: " + targetCount + ")");
        }
    }

    @Step("Get current page title")
    public String getCurrentPageTitle() {
        return DriverManager.getDriver().getTitle();
    }

    @Step("Get current URL")
    public String getCurrentUrl() {
        return DriverManager.getDriver().getCurrentUrl();
    }
}
