package com.larry.test;

import com.larry.data.model.PromptData;
import com.larry.data.util.DataSaver;
import com.larry.driver.DriverManager;
import com.larry.driver.TargetFactory;
import com.larry.page.opennana.OpenNanaDetailPage;
import com.larry.page.opennana.OpenNanaGalleryPage;
import com.larry.wait.Waits;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

public class OpenNanaScraperTest {

    private static final int TARGET_CARD_COUNT = 5;
    private static final String OUTPUT_DIRECTORY = "output";

    @BeforeMethod
    public void setup() {
        WebDriver driver = new TargetFactory().createInstance("chrome");
        DriverManager.setDriver(driver);
        driver.manage().window().maximize();
    }

    @AfterMethod
    public void teardown() {
        DriverManager.quit();
    }

    @Test(description = "Extract prompt data from OpenNana gallery")
    public void extractPromptData() {
        DataSaver.ensureDirectoryExists(OUTPUT_DIRECTORY);
        
        OpenNanaGalleryPage galleryPage = new OpenNanaGalleryPage();
        OpenNanaDetailPage detailPage = new OpenNanaDetailPage();
        List<PromptData> extractedData = new ArrayList<>();

        galleryPage.navigateToGallery();
        Waits.sleep(3000);

        galleryPage.scrollToLoadCards(TARGET_CARD_COUNT);
        
        List<WebElement> cards = galleryPage.getImageCards();
        int cardsToProcess = Math.min(cards.size(), TARGET_CARD_COUNT);
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println("Starting data extraction for " + cardsToProcess + " cards...");
        System.out.println("=".repeat(60) + "\n");

        for (int i = 0; i < cardsToProcess; i++) {
            System.out.println("\n--- Processing card " + (i + 1) + " of " + cardsToProcess + " ---");
            
            try {
                cards = galleryPage.getImageCards();
                if (i >= cards.size()) {
                    System.out.println("Warning: Card index " + i + " is out of bounds, skipping...");
                    continue;
                }
                
                WebElement card = cards.get(i);
                
                String cardTitle = galleryPage.getTitleFromCard(card);
                String imageUrl = galleryPage.getImageUrlFromCard(card);
                
                System.out.println("Card Title: " + cardTitle);
                System.out.println("Image URL: " + imageUrl);

                galleryPage.clickCard(card);
                Waits.sleep(3000);

                String currentUrl = detailPage.getCurrentUrl();
                System.out.println("Detail Page URL: " + currentUrl);

                String pageTitle = detailPage.getPageTitle();
                System.out.println("Page Title: " + pageTitle);

                String sampleImageUrl = detailPage.getFirstSampleImageUrl();
                System.out.println("Sample Image URL: " + sampleImageUrl);

                System.out.println("\nExtracting prompts...");
                List<String> allPrompts = detailPage.extractPrompts();
                System.out.println("Found " + allPrompts.size() + " potential prompts");
                
                for (int j = 0; j < allPrompts.size(); j++) {
                    String p = allPrompts.get(j);
                    System.out.println("\nPrompt " + (j + 1) + " (length: " + p.length() + "):");
                    System.out.println(p.substring(0, Math.min(200, p.length())));
                    if (p.length() > 200) {
                        System.out.println("... (truncated, total length: " + p.length() + ")");
                    }
                }

                String promptEn = null;
                String promptCh = null;
                
                for (String p : allPrompts) {
                    if (containsChinese(p)) {
                        if (promptCh == null || p.length() > promptCh.length()) {
                            promptCh = p;
                        }
                    } else {
                        if (promptEn == null || p.length() > promptEn.length()) {
                            promptEn = p;
                        }
                    }
                }

                if (allPrompts.size() == 1) {
                    String p = allPrompts.get(0);
                    if (containsChinese(p)) {
                        promptCh = p;
                    } else {
                        promptEn = p;
                    }
                }

                System.out.println("\n--- Final Selection ---");
                System.out.println("English Prompt: " + (promptEn != null ? promptEn.substring(0, Math.min(100, promptEn.length())) + "..." : "null"));
                System.out.println("Chinese Prompt: " + (promptCh != null ? promptCh.substring(0, Math.min(100, promptCh.length())) + "..." : "null"));

                PromptData data = new PromptData();
                data.setTitle(pageTitle != null ? pageTitle : cardTitle);
                data.setImageUrl(sampleImageUrl != null ? sampleImageUrl : imageUrl);
                data.setPromptEn(promptEn);
                data.setPromptCh(promptCh);
                data.setSourceUrl(currentUrl);

                extractedData.add(data);
                System.out.println("\n✅ Successfully extracted: " + data.getTitle());

                detailPage.goBack();
                Waits.sleep(2000);

            } catch (Exception e) {
                System.out.println("❌ Error processing card " + (i + 1) + ": " + e.getMessage());
                e.printStackTrace();
                
                try {
                    String currentUrl = DriverManager.getDriver().getCurrentUrl();
                    if (!currentUrl.contains("awesome-prompt-gallery?media_type=image")) {
                        System.out.println("Returning to gallery page...");
                        galleryPage.navigateToGallery();
                        Waits.sleep(2000);
                    }
                } catch (Exception ex) {
                    System.out.println("Error during recovery: " + ex.getMessage());
                }
            }
        }

        System.out.println("\n" + "=".repeat(60));
        System.out.println("Data Extraction Complete!");
        System.out.println("=".repeat(60));
        
        DataSaver.printSummary(extractedData);

        String jsonFilePath = OUTPUT_DIRECTORY + "/" + DataSaver.generateOutputFilePath("opennana_prompts");
        DataSaver.saveToJson(extractedData, jsonFilePath);

        String csvFilePath = OUTPUT_DIRECTORY + "/" + DataSaver.generateOutputFilePath("opennana_prompts").replace(".json", ".csv");
        DataSaver.saveToCsv(extractedData, csvFilePath);

        System.out.println("\n✅ Files saved:");
        System.out.println("   JSON: " + jsonFilePath);
        System.out.println("   CSV: " + csvFilePath);
    }

    @Test(description = "Test single card extraction for debugging")
    public void testSingleCardExtraction() {
        DataSaver.ensureDirectoryExists(OUTPUT_DIRECTORY);
        
        OpenNanaGalleryPage galleryPage = new OpenNanaGalleryPage();
        OpenNanaDetailPage detailPage = new OpenNanaDetailPage();

        galleryPage.navigateToGallery();
        Waits.sleep(3000);

        List<WebElement> cards = galleryPage.getImageCards();
        if (cards.isEmpty()) {
            System.out.println("No cards found!");
            return;
        }

        System.out.println("\n=== Testing Single Card Extraction ===");
        System.out.println("Found " + cards.size() + " cards\n");

        try {
            WebElement firstCard = cards.get(0);
            
            String cardTitle = galleryPage.getTitleFromCard(firstCard);
            String imageUrl = galleryPage.getImageUrlFromCard(firstCard);
            
            System.out.println("Card Title: " + cardTitle);
            System.out.println("Card Image URL: " + imageUrl);

            galleryPage.clickCard(firstCard);
            Waits.sleep(3000);

            String currentUrl = detailPage.getCurrentUrl();
            System.out.println("\nDetail Page URL: " + currentUrl);

            String pageTitle = detailPage.getPageTitle();
            System.out.println("Page Title: " + pageTitle);

            String sampleImageUrl = detailPage.getFirstSampleImageUrl();
            System.out.println("Sample Image URL: " + sampleImageUrl);

            System.out.println("\n--- Extracting all possible prompts ---");
            List<String> allPrompts = detailPage.extractPrompts();
            System.out.println("\nTotal prompts found: " + allPrompts.size());
            
            for (int i = 0; i < allPrompts.size(); i++) {
                String p = allPrompts.get(i);
                System.out.println("\n=== Prompt " + (i + 1) + " (length: " + p.length() + ") ===");
                System.out.println(p);
                System.out.println("\nContains Chinese: " + containsChinese(p));
            }

            System.out.println("\n=== Trying copy buttons approach ===");
            // This is now integrated into extractPrompts() method
            System.out.println("Copy button extraction is now integrated into main extraction logic");

            System.out.println("\n=== Test Complete ===");

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
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
}
