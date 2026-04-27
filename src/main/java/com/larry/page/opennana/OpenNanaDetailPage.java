package com.larry.page.opennana;

import com.larry.driver.DriverManager;
import com.larry.page.AbstractPageObject;
import com.larry.wait.Waits;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

public class OpenNanaDetailPage extends AbstractPageObject {

    private final By pageTitle = By.cssSelector("h1");
    private final By promptSection = By.cssSelector("section:nth-child(3)");
    private final By copyButtons = By.xpath("//button[text()='复制']");
    private final By sampleImages = By.cssSelector("img[alt^='示例']");
    private final By goToAIImageButton = By.xpath("//a[text()='去 AI 生图']");

    private final By modalTitle = By.cssSelector("div.fixed.inset-0.z-50 h1");
    private final By modalPromptEn = By.cssSelector("div.fixed.inset-0.z-50 section:nth-child(3) > div > div:nth-child(1)");
    private final By modalPromptCh = By.cssSelector("div.fixed.inset-0.z-50 section:nth-child(3) > div > div:nth-child(2)");

    @Step("Get page title")
    public String getPageTitle() {
        try {
            WebElement titleElement = Waits.waitForVisibility(pageTitle);
            String title = titleElement.getText().trim();
            System.out.println("[OpenNanaDetailPage] Page title: " + title);
            return title;
        } catch (Exception e) {
            System.out.println("[OpenNanaDetailPage] Error getting page title: " + e.getMessage());
            return null;
        }
    }

    @Step("Get sample image URLs")
    public List<String> getSampleImageUrls() {
        List<String> urls = new ArrayList<>();
        try {
            List<WebElement> images = DriverManager.getDriver().findElements(sampleImages);
            for (WebElement img : images) {
                String src = img.getAttribute("src");
                if (src != null && !src.isEmpty()) {
                    urls.add(src);
                    System.out.println("[OpenNanaDetailPage] Sample image URL: " + src);
                }
            }
        } catch (Exception e) {
            System.out.println("[OpenNanaDetailPage] Error getting sample images: " + e.getMessage());
        }
        return urls;
    }

    @Step("Get first sample image URL")
    public String getFirstSampleImageUrl() {
        List<String> urls = getSampleImageUrls();
        return urls.isEmpty() ? null : urls.get(0);
    }

    @Step("Get all text from section containing prompts")
    public String getPromptSectionText() {
        try {
            List<WebElement> sections = DriverManager.getDriver().findElements(By.tagName("section"));
            System.out.println("[OpenNanaDetailPage] Found " + sections.size() + " sections");
            
            for (int i = 0; i < sections.size(); i++) {
                WebElement section = sections.get(i);
                String text = section.getText();
                System.out.println("[OpenNanaDetailPage] Section " + i + " text length: " + text.length());
                if (text.length() > 100) {
                    System.out.println("[OpenNanaDetailPage] Section " + i + " preview: " + text.substring(0, Math.min(200, text.length())));
                }
            }
            
            return DriverManager.getDriver().getPageSource();
        } catch (Exception e) {
            System.out.println("[OpenNanaDetailPage] Error getting prompt section text: " + e.getMessage());
            return null;
        }
    }

    @Step("Find all copy buttons and get their parent text")
    public List<String> getPromptsFromCopyButtons() {
        List<String> prompts = new ArrayList<>();
        try {
            List<WebElement> copyBtns = DriverManager.getDriver().findElements(copyButtons);
            System.out.println("[OpenNanaDetailPage] Found " + copyBtns.size() + " copy buttons");
            
            for (WebElement btn : copyBtns) {
                try {
                    WebElement parent = btn.findElement(By.xpath("./.."));
                    String parentText = parent.getText();
                    System.out.println("[OpenNanaDetailPage] Copy button parent text: " + parentText);
                    
                    WebElement grandParent = parent.findElement(By.xpath("./.."));
                    String grandParentText = grandParent.getText();
                    System.out.println("[OpenNanaDetailPage] Copy button grandparent text: " + grandParentText);
                    
                    WebElement promptElement = grandParent.findElement(By.xpath(".//div[not(contains(@class, 'flex'))]"));
                    if (promptElement != null) {
                        String prompt = promptElement.getText();
                        if (prompt != null && !prompt.isEmpty() && !prompt.contains("复制")) {
                            prompts.add(prompt);
                            System.out.println("[OpenNanaDetailPage] Found prompt: " + prompt.substring(0, Math.min(100, prompt.length())));
                        }
                    }
                } catch (Exception e) {
                    System.out.println("[OpenNanaDetailPage] Error getting text for copy button: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.out.println("[OpenNanaDetailPage] Error finding copy buttons: " + e.getMessage());
        }
        return prompts;
    }

    @Step("Extract prompts by finding all divs with long text")
    public List<String> extractPrompts() {
        List<String> prompts = new ArrayList<>();
        try {
            List<WebElement> allDivs = DriverManager.getDriver().findElements(By.tagName("div"));
            System.out.println("[OpenNanaDetailPage] Found " + allDivs.size() + " divs");
            
            for (WebElement div : allDivs) {
                try {
                    String text = div.getText();
                    if (text != null && text.length() > 50 && !text.contains("复制") && !text.contains("AI 生图")) {
                        if (!prompts.contains(text)) {
                            prompts.add(text);
                            System.out.println("[OpenNanaDetailPage] Found potential prompt (length: " + text.length() + "): " + text.substring(0, Math.min(100, text.length())));
                        }
                    }
                } catch (Exception e) {
                }
            }
        } catch (Exception e) {
            System.out.println("[OpenNanaDetailPage] Error extracting prompts: " + e.getMessage());
        }
        return prompts;
    }

    @Step("Get English prompt")
    public String getPromptEn() {
        List<String> prompts = extractPrompts();
        if (prompts.size() >= 1) {
            return prompts.get(0);
        }
        
        List<String> copyPrompts = getPromptsFromCopyButtons();
        if (copyPrompts.size() >= 1) {
            return copyPrompts.get(0);
        }
        
        return null;
    }

    @Step("Get Chinese prompt")
    public String getPromptCh() {
        List<String> prompts = extractPrompts();
        if (prompts.size() >= 2) {
            return prompts.get(1);
        }
        
        List<String> copyPrompts = getPromptsFromCopyButtons();
        if (copyPrompts.size() >= 2) {
            return copyPrompts.get(1);
        }
        
        return null;
    }

    @Step("Check if detail page is displayed")
    public boolean isDetailPageDisplayed() {
        try {
            WebElement title = Waits.waitForVisibility(pageTitle);
            return title != null && title.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    @Step("Go back to gallery page")
    public void goBack() {
        System.out.println("[OpenNanaDetailPage] Navigating back...");
        DriverManager.getDriver().navigate().back();
        Waits.waitForPageToLoad();
    }

    @Step("Get current URL")
    public String getCurrentUrl() {
        return DriverManager.getDriver().getCurrentUrl();
    }
}
