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

public class OpenNanaDetailPage extends AbstractPageObject {

    private final By pageTitle = By.cssSelector("h1");
    private final By copyButtons = By.xpath("//button[text()='复制']");
    private final By sampleImages = By.cssSelector("img[alt^='示例']");
    private final By sections = By.tagName("section");
    private final By allDivs = By.tagName("div");

    @Step("Get page title")
    public String getPageTitle() {
        try {
            List<WebElement> h1Elements = DriverManager.getDriver().findElements(pageTitle);
            String bestTitle = null;
            for (WebElement h1 : h1Elements) {
                try {
                    String title = h1.getText().trim();
                    System.out.println("[OpenNanaDetailPage] Found h1 title: " + title);
                    if (title != null && !title.isEmpty() && !title.contains("提示词图库")) {
                        bestTitle = title;
                    }
                } catch (Exception e) {
                }
            }
            return bestTitle;
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
            
            if (urls.isEmpty()) {
                List<WebElement> allImages = DriverManager.getDriver().findElements(By.tagName("img"));
                for (WebElement img : allImages) {
                    try {
                        String src = img.getAttribute("src");
                        String alt = img.getAttribute("alt");
                        if (src != null && src.contains("opennana.com") && 
                            !src.contains("thumb") && !src.contains("480")) {
                            System.out.println("[OpenNanaDetailPage] Found image (alt=" + alt + "): " + src);
                            if (!urls.contains(src)) {
                                urls.add(src);
                            }
                        }
                    } catch (Exception e) {
                    }
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

    @Step("Find all sections and analyze")
    public void analyzeSections() {
        try {
            List<WebElement> sectionElements = DriverManager.getDriver().findElements(sections);
            System.out.println("[OpenNanaDetailPage] Found " + sectionElements.size() + " sections");
            
            for (int i = 0; i < sectionElements.size(); i++) {
                WebElement section = sectionElements.get(i);
                try {
                    String text = section.getText();
                    System.out.println("\n[OpenNanaDetailPage] Section " + i + " (length: " + text.length() + "):");
                    if (text.length() > 0) {
                        System.out.println(text.substring(0, Math.min(500, text.length())));
                        if (text.length() > 500) {
                            System.out.println("... (truncated, total: " + text.length() + " chars)");
                        }
                    }
                } catch (Exception e) {
                    System.out.println("[OpenNanaDetailPage] Error reading section " + i + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.out.println("[OpenNanaDetailPage] Error analyzing sections: " + e.getMessage());
        }
    }

    @Step("Extract prompts using copy buttons as anchor")
    public List<String> extractPromptsFromCopyButtons() {
        List<String> prompts = new ArrayList<>();
        try {
            List<WebElement> copyBtns = DriverManager.getDriver().findElements(copyButtons);
            System.out.println("[OpenNanaDetailPage] Found " + copyBtns.size() + " copy buttons");
            
            for (int btnIndex = 0; btnIndex < copyBtns.size(); btnIndex++) {
                WebElement btn = copyBtns.get(btnIndex);
                try {
                    System.out.println("\n=== Analyzing copy button " + (btnIndex + 1) + " ===");
                    
                    WebElement parent = btn.findElement(By.xpath("./.."));
                    String parentText = parent.getText();
                    System.out.println("[OpenNanaDetailPage] Parent text: " + parentText);
                    
                    WebElement grandParent = parent.findElement(By.xpath("./.."));
                    String grandParentText = grandParent.getText();
                    System.out.println("[OpenNanaDetailPage] GrandParent text: " + grandParentText);
                    
                    WebElement greatGrandParent = grandParent.findElement(By.xpath("./.."));
                    String greatGrandParentText = greatGrandParent.getText();
                    System.out.println("[OpenNanaDetailPage] GreatGrandParent text (length: " + greatGrandParentText.length() + "):");
                    if (greatGrandParentText.length() > 0) {
                        System.out.println(greatGrandParentText.substring(0, Math.min(300, greatGrandParentText.length())));
                    }
                    
                    String prompt = extractPromptFromContainer(greatGrandParent);
                    if (prompt != null && !prompt.isEmpty()) {
                        prompts.add(prompt);
                        System.out.println("[OpenNanaDetailPage] Extracted prompt (length: " + prompt.length() + ")");
                    }
                    
                } catch (Exception e) {
                    System.out.println("[OpenNanaDetailPage] Error analyzing copy button " + btnIndex + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.out.println("[OpenNanaDetailPage] Error extracting from copy buttons: " + e.getMessage());
        }
        return prompts;
    }

    private String extractPromptFromContainer(WebElement container) {
        try {
            List<WebElement> allChildren = container.findElements(By.xpath(".//*"));
            String longestText = null;
            int maxLength = 0;
            
            for (WebElement child : allChildren) {
                try {
                    String text = child.getText();
                    if (text != null && text.length() > maxLength) {
                        if (!text.contains("复制") && !text.contains("去 AI 生图") && 
                            !text.contains("ENGLISH") && !text.contains("中文")) {
                            maxLength = text.length();
                            longestText = text;
                        }
                    }
                } catch (Exception e) {
                }
            }
            
            if (longestText != null && longestText.length() > 20) {
                return longestText;
            }
            
            String containerText = container.getText();
            if (containerText != null && containerText.length() > 50) {
                String[] lines = containerText.split("\n");
                StringBuilder promptBuilder = new StringBuilder();
                for (String line : lines) {
                    if (!line.trim().equals("复制") && 
                        !line.trim().equals("去 AI 生图") &&
                        !line.trim().equals("ENGLISH") &&
                        !line.trim().equals("中文")) {
                        if (promptBuilder.length() > 0) {
                            promptBuilder.append("\n");
                        }
                        promptBuilder.append(line);
                    }
                }
                String result = promptBuilder.toString().trim();
                if (result.length() > 20) {
                    return result;
                }
            }
            
        } catch (Exception e) {
            System.out.println("[OpenNanaDetailPage] Error extracting from container: " + e.getMessage());
        }
        return null;
    }

    @Step("Extract prompts by finding all text blocks")
    public List<String> extractPrompts() {
        List<String> prompts = new ArrayList<>();
        
        System.out.println("\n=== Method 1: Extract from copy buttons ===");
        List<String> fromCopyButtons = extractPromptsFromCopyButtons();
        for (String p : fromCopyButtons) {
            if (!prompts.contains(p) && p.length() > 20) {
                prompts.add(p);
            }
        }
        
        if (!prompts.isEmpty()) {
            System.out.println("\n[OpenNanaDetailPage] Found " + prompts.size() + " prompts from copy buttons");
            return prompts;
        }
        
        System.out.println("\n=== Method 2: Scan all divs ===");
        try {
            List<WebElement> divElements = DriverManager.getDriver().findElements(allDivs);
            System.out.println("[OpenNanaDetailPage] Found " + divElements.size() + " divs");
            
            for (int i = 0; i < divElements.size(); i++) {
                try {
                    WebElement div = divElements.get(i);
                    String text = div.getText();
                    
                    if (text != null && text.length() > 100) {
                        if (!text.contains("复制") && !text.contains("去 AI 生图") &&
                            !text.contains("提示词图库") && !text.contains("Nano Banana")) {
                            if (!prompts.contains(text)) {
                                prompts.add(text);
                                System.out.println("[OpenNanaDetailPage] Found prompt " + prompts.size() + " (length: " + text.length() + ")");
                                System.out.println(text.substring(0, Math.min(200, text.length())));
                            }
                        }
                    }
                } catch (Exception e) {
                }
            }
        } catch (Exception e) {
            System.out.println("[OpenNanaDetailPage] Error scanning divs: " + e.getMessage());
        }
        
        System.out.println("\n=== Method 3: Analyze sections ===");
        analyzeSections();
        
        return prompts;
    }

    @Step("Get English prompt")
    public String getPromptEn() {
        List<String> prompts = extractPrompts();
        
        for (String prompt : prompts) {
            if (!containsChinese(prompt) && prompt.length() > 50) {
                return prompt;
            }
        }
        
        if (prompts.size() >= 1) {
            return prompts.get(0);
        }
        
        return null;
    }

    @Step("Get Chinese prompt")
    public String getPromptCh() {
        List<String> prompts = extractPrompts();
        
        for (String prompt : prompts) {
            if (containsChinese(prompt) && prompt.length() > 50) {
                return prompt;
            }
        }
        
        if (prompts.size() >= 2) {
            return prompts.get(1);
        }
        
        return null;
    }

    private boolean containsChinese(String text) {
        if (text == null) return false;
        for (char c : text.toCharArray()) {
            if (Character.UnicodeScript.of(c) == Character.UnicodeScript.HAN) {
                return true;
            }
        }
        return false;
    }

    @Step("Check if detail page is displayed")
    public boolean isDetailPageDisplayed() {
        try {
            String currentUrl = DriverManager.getDriver().getCurrentUrl();
            return currentUrl.contains("/awesome-prompt-gallery/");
        } catch (Exception e) {
            return false;
        }
    }

    @Step("Go back to gallery page")
    public void goBack() {
        System.out.println("[OpenNanaDetailPage] Navigating back...");
        DriverManager.getDriver().navigate().back();
        Waits.sleep(1500);
    }

    @Step("Get current URL")
    public String getCurrentUrl() {
        return DriverManager.getDriver().getCurrentUrl();
    }

    @Step("Execute JavaScript to get all text")
    public String getAllTextViaJS() {
        try {
            JavascriptExecutor js = (JavascriptExecutor) DriverManager.getDriver();
            String script = "return document.body.innerText;";
            String allText = (String) js.executeScript(script);
            System.out.println("[OpenNanaDetailPage] All text length: " + allText.length());
            return allText;
        } catch (Exception e) {
            System.out.println("[OpenNanaDetailPage] Error getting text via JS: " + e.getMessage());
            return null;
        }
    }
}
