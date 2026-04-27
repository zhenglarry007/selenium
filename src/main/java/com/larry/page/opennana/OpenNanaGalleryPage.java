package com.larry.page.opennana;

import com.larry.driver.DriverManager;
import com.larry.page.AbstractPageObject;
import com.larry.wait.Waits;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OpenNanaGalleryPage extends AbstractPageObject {

    private final By galleryContainer = By.cssSelector("div.flex.-ml-4.w-auto");
    private final By imageCards = By.cssSelector("div.flex.-ml-4.w-auto > div");
    private final By cardImage = By.cssSelector("div.relative.overflow-hidden > img");
    private final By cardTitle = By.cssSelector("h3, h4");
    private final By cardLink = By.cssSelector("a");

    private static final String GALLERY_URL = "https://opennana.com/awesome-prompt-gallery?media_type=image&model=ChatGPT";

    @Step("Navigate to OpenNana gallery page")
    public void navigateToGallery() {
        System.out.println("[OpenNanaGalleryPage] Navigating to: " + GALLERY_URL);
        DriverManager.getDriver().get(GALLERY_URL);
        Waits.sleep(3000);
        System.out.println("[OpenNanaGalleryPage] Page loaded");
    }

    @Step("Navigate to URL: {url}")
    public void navigateToUrl(String url) {
        System.out.println("[OpenNanaGalleryPage] Navigating to: " + url);
        DriverManager.getDriver().get(url);
        Waits.sleep(3000);
        System.out.println("[OpenNanaGalleryPage] Page loaded");
    }

    @Step("Scroll down using multiple strategies")
    public void scrollDownSmart() {
        System.out.println("[OpenNanaGalleryPage] Smart scrolling down...");
        JavascriptExecutor js = (JavascriptExecutor) DriverManager.getDriver();
        
        try {
            WebElement container = DriverManager.getDriver().findElement(galleryContainer);
            System.out.println("[OpenNanaGalleryPage] Found gallery container, scrolling container...");
            
            long currentScrollHeight = (Long) js.executeScript("return arguments[0].scrollHeight;", container);
            long currentScrollTop = (Long) js.executeScript("return arguments[0].scrollTop;", container);
            
            js.executeScript("arguments[0].scrollTop = arguments[0].scrollHeight;", container);
            System.out.println("[OpenNanaGalleryPage] Scrolled container from " + currentScrollTop + " to " + currentScrollHeight);
            
        } catch (Exception e) {
            System.out.println("[OpenNanaGalleryPage] Container scroll failed, falling back to window scroll: " + e.getMessage());
            
            js.executeScript("window.scrollBy(0, 800);");
            System.out.println("[OpenNanaGalleryPage] Scrolled window by 800px");
        }
        
        Waits.sleep(3000);
        System.out.println("[OpenNanaGalleryPage] Smart scroll completed");
    }

    @Step("Scroll to bottom of page")
    public void scrollToBottom() {
        System.out.println("[OpenNanaGalleryPage] Scrolling to bottom...");
        JavascriptExecutor js = (JavascriptExecutor) DriverManager.getDriver();
        
        long lastHeight = (Long) js.executeScript("return document.body.scrollHeight;");
        int attempts = 0;
        int maxAttempts = 10;
        
        while (attempts < maxAttempts) {
            js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
            Waits.sleep(3000);
            
            long newHeight = (Long) js.executeScript("return document.body.scrollHeight;");
            System.out.println("[OpenNanaGalleryPage] Scroll attempt " + (attempts + 1) + ": height=" + newHeight);
            
            if (newHeight == lastHeight) {
                System.out.println("[OpenNanaGalleryPage] Reached bottom (height not changing)");
                break;
            }
            lastHeight = newHeight;
            attempts++;
        }
        
        js.executeScript("window.scrollTo(0, 0);");
        Waits.sleep(1000);
        System.out.println("[OpenNanaGalleryPage] Scroll to bottom completed");
    }

    @Step("Scroll to load {targetCount} cards (improved)")
    public List<CardInfo> scrollToLoadCards(int targetCount) {
        System.out.println("[OpenNanaGalleryPage] Attempting to load " + targetCount + " cards...");
        
        List<CardInfo> allCards = new ArrayList<>();
        Set<String> seenTitles = new HashSet<>();
        int scrollAttempts = 0;
        int maxScrollAttempts = 50;
        int noNewCardsCount = 0;
        
        while (allCards.size() < targetCount && scrollAttempts < maxScrollAttempts && noNewCardsCount < 5) {
            int previousSize = allCards.size();
            
            List<WebElement> currentCards = getImageCards();
            for (WebElement card : currentCards) {
                try {
                    String title = getTitleFromCard(card);
                    if (title != null && !seenTitles.contains(title)) {
                        String imageUrl = getImageUrlFromCard(card);
                        String href = getCardHref(card);
                        
                        CardInfo info = new CardInfo(title, imageUrl, href);
                        allCards.add(info);
                        seenTitles.add(title);
                        
                        System.out.println("[OpenNanaGalleryPage] Found new card: " + title);
                    }
                } catch (Exception e) {
                }
            }
            
            if (allCards.size() >= targetCount) {
                break;
            }
            
            if (allCards.size() == previousSize) {
                noNewCardsCount++;
                System.out.println("[OpenNanaGalleryPage] No new cards found, attempt " + noNewCardsCount);
            } else {
                noNewCardsCount = 0;
            }
            
            scrollDownSmart();
            scrollAttempts++;
            
            System.out.println("[OpenNanaGalleryPage] After scroll " + scrollAttempts + ": total=" + allCards.size() + " cards");
        }
        
        System.out.println("[OpenNanaGalleryPage] Final count: " + allCards.size() + " cards (target: " + targetCount + ")");
        
        if (allCards.size() >= targetCount) {
            return allCards.subList(0, targetCount);
        }
        return allCards;
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
            return src;
        } catch (Exception e) {
            return null;
        }
    }

    @Step("Get title from card")
    public String getTitleFromCard(WebElement card) {
        try {
            WebElement titleElement = card.findElement(cardTitle);
            String title = titleElement.getText().trim();
            return title;
        } catch (Exception e) {
            return null;
        }
    }

    @Step("Get card href (link)")
    public String getCardHref(WebElement card) {
        try {
            WebElement link = card.findElement(cardLink);
            String href = link.getAttribute("href");
            return href;
        } catch (Exception e) {
            return null;
        }
    }

    @Step("Click card by index (with retry)")
    public void clickCardByIndex(int index) {
        System.out.println("[OpenNanaGalleryPage] Clicking card at index " + index);
        
        int maxRetries = 3;
        for (int attempt = 0; attempt < maxRetries; attempt++) {
            try {
                List<WebElement> cards = getImageCards();
                if (index < cards.size()) {
                    WebElement card = cards.get(index);
                    
                    ((JavascriptExecutor) DriverManager.getDriver())
                        .executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", card);
                    Waits.sleep(500);
                    
                    Waits.waitForClickability(card).click();
                    Waits.sleep(2000);
                    
                    System.out.println("[OpenNanaGalleryPage] Card clicked successfully");
                    return;
                }
            } catch (Exception e) {
                System.out.println("[OpenNanaGalleryPage] Click attempt " + (attempt + 1) + " failed: " + e.getMessage());
                Waits.sleep(1000);
            }
        }
        
        System.out.println("[OpenNanaGalleryPage] All click attempts failed");
    }

    @Step("Click card by title")
    public boolean clickCardByTitle(String title) {
        System.out.println("[OpenNanaGalleryPage] Looking for card with title: " + title);
        
        int maxRetries = 3;
        for (int attempt = 0; attempt < maxRetries; attempt++) {
            try {
                List<WebElement> cards = getImageCards();
                for (WebElement card : cards) {
                    try {
                        String cardTitle = getTitleFromCard(card);
                        if (cardTitle != null && cardTitle.equals(title)) {
                            System.out.println("[OpenNanaGalleryPage] Found card, scrolling into view...");
                            
                            ((JavascriptExecutor) DriverManager.getDriver())
                                .executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", card);
                            Waits.sleep(500);
                            
                            Waits.waitForClickability(card).click();
                            Waits.sleep(2000);
                            
                            System.out.println("[OpenNanaGalleryPage] Card clicked successfully");
                            return true;
                        }
                    } catch (Exception e) {
                    }
                }
            } catch (Exception e) {
                System.out.println("[OpenNanaGalleryPage] Search attempt " + (attempt + 1) + " failed: " + e.getMessage());
            }
            Waits.sleep(1000);
        }
        
        System.out.println("[OpenNanaGalleryPage] Card not found: " + title);
        return false;
    }

    @Step("Navigate to detail page by URL")
    public void navigateToDetailPage(String url) {
        if (url == null || url.isEmpty()) {
            System.out.println("[OpenNanaGalleryPage] URL is empty, cannot navigate");
            return;
        }
        System.out.println("[OpenNanaGalleryPage] Navigating directly to detail page: " + url);
        DriverManager.getDriver().get(url);
        Waits.sleep(2000);
    }

    @Step("Get current page title")
    public String getCurrentPageTitle() {
        return DriverManager.getDriver().getTitle();
    }

    @Step("Get current URL")
    public String getCurrentUrl() {
        return DriverManager.getDriver().getCurrentUrl();
    }

    public static class CardInfo {
        private final String title;
        private final String imageUrl;
        private final String detailUrl;

        public CardInfo(String title, String imageUrl, String detailUrl) {
            this.title = title;
            this.imageUrl = imageUrl;
            this.detailUrl = detailUrl;
        }

        public String getTitle() {
            return title;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public String getDetailUrl() {
            return detailUrl;
        }

        @Override
        public String toString() {
            return "CardInfo{" +
                    "title='" + title + '\'' +
                    ", imageUrl='" + (imageUrl != null ? "exists" : "null") + '\'' +
                    ", detailUrl='" + (detailUrl != null ? "exists" : "null") + '\'' +
                    '}';
        }
    }
}
