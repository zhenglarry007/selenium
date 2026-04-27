package com.larry.test;

import com.larry.data.model.PromptData;
import com.larry.data.util.DataSaver;
import com.larry.driver.DriverManager;
import com.larry.driver.TargetFactory;
import com.larry.page.opennana.OpenNanaDetailPage;
import com.larry.page.opennana.OpenNanaGalleryPage;
import com.larry.wait.Waits;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

public class OpenNanaScraperTest {

    private static final int TARGET_CARD_COUNT = 10;
    private static final String OUTPUT_DIRECTORY = "output";

    @BeforeMethod
    public void setup() {
        System.out.println("Setting up WebDriver...");
        WebDriver driver = new TargetFactory().createInstance("chrome");
        DriverManager.setDriver(driver);
        System.out.println("WebDriver created successfully");
        Waits.sleep(1000);
    }

    @AfterMethod
    public void teardown() {
        DriverManager.quit();
    }

    @Test(description = "Extract prompt data from OpenNana gallery (Improved Strategy)")
    public void extractPromptData() {
        DataSaver.ensureDirectoryExists(OUTPUT_DIRECTORY);
        
        OpenNanaGalleryPage galleryPage = new OpenNanaGalleryPage();
        OpenNanaDetailPage detailPage = new OpenNanaDetailPage();
        List<PromptData> extractedData = new ArrayList<>();

        System.out.println("\n" + "=".repeat(70));
        System.out.println("Step 1: Preloading card information (title, image URL, detail URL)");
        System.out.println("=".repeat(70) + "\n");

        galleryPage.navigateToGallery();
        Waits.sleep(3000);

        List<OpenNanaGalleryPage.CardInfo> allCards = galleryPage.scrollToLoadCards(TARGET_CARD_COUNT);
        
        int cardsToProcess = Math.min(allCards.size(), TARGET_CARD_COUNT);
        
        System.out.println("\n" + "=".repeat(70));
        System.out.println("Preloaded " + cardsToProcess + " cards!");
        System.out.println("=".repeat(70));
        
        for (int i = 0; i < cardsToProcess; i++) {
            OpenNanaGalleryPage.CardInfo card = allCards.get(i);
            System.out.println("  " + (i + 1) + ". " + card.getTitle());
            System.out.println("     URL: " + card.getDetailUrl());
        }

        System.out.println("\n" + "=".repeat(70));
        System.out.println("Step 2: Extracting data from each detail page");
        System.out.println("=".repeat(70) + "\n");

        for (int i = 0; i < cardsToProcess; i++) {
            OpenNanaGalleryPage.CardInfo cardInfo = allCards.get(i);
            
            System.out.println("\n" + "-".repeat(70));
            System.out.println("Processing card " + (i + 1) + " of " + cardsToProcess);
            System.out.println("Title: " + cardInfo.getTitle());
            System.out.println("Detail URL: " + cardInfo.getDetailUrl());
            System.out.println("-".repeat(70) + "\n");

            try {
                if (cardInfo.getDetailUrl() == null || cardInfo.getDetailUrl().isEmpty()) {
                    System.out.println("⚠️ Warning: Detail URL is empty, skipping this card");
                    continue;
                }

                System.out.println("Navigating directly to detail page...");
                galleryPage.navigateToDetailPage(cardInfo.getDetailUrl());
                Waits.sleep(2000);

                String currentUrl = detailPage.getCurrentUrl();
                System.out.println("Current URL: " + currentUrl);

                String pageTitle = detailPage.getPageTitle();
                System.out.println("Page Title: " + pageTitle);

                String sampleImageUrl = detailPage.getFirstSampleImageUrl();
                System.out.println("Sample Image URL: " + (sampleImageUrl != null ? sampleImageUrl : "null"));

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
                System.out.println("English Prompt: " + (promptEn != null ? 
                    promptEn.substring(0, Math.min(100, promptEn.length())) + "..." : "null"));
                System.out.println("Chinese Prompt: " + (promptCh != null ? 
                    promptCh.substring(0, Math.min(100, promptCh.length())) + "..." : "null"));

                PromptData data = new PromptData();
                data.setTitle(pageTitle != null ? pageTitle : cardInfo.getTitle());
                data.setImageUrl(sampleImageUrl != null ? sampleImageUrl : cardInfo.getImageUrl());
                data.setPromptEn(promptEn);
                data.setPromptCh(promptCh);
                data.setSourceUrl(currentUrl);

                extractedData.add(data);
                System.out.println("\n✅ Successfully extracted: " + data.getTitle());

            } catch (Exception e) {
                System.out.println("❌ Error processing card " + (i + 1) + ": " + e.getMessage());
                e.printStackTrace();
            }
        }

        System.out.println("\n" + "=".repeat(70));
        System.out.println("Data Extraction Complete!");
        System.out.println("=".repeat(70));
        
        DataSaver.printSummary(extractedData);

        String jsonFilePath = OUTPUT_DIRECTORY + "/" + DataSaver.generateOutputFilePath("opennana_prompts");
        DataSaver.saveToJson(extractedData, jsonFilePath);

        String csvFilePath = OUTPUT_DIRECTORY + "/" + DataSaver.generateOutputFilePath("opennana_prompts").replace(".json", ".csv");
        DataSaver.saveToCsv(extractedData, csvFilePath);

        System.out.println("\n✅ Files saved:");
        System.out.println("   JSON: " + jsonFilePath);
        System.out.println("   CSV: " + csvFilePath);
    }

    @Test(description = "Test scroll and card loading")
    public void testScrollLoading() {
        OpenNanaGalleryPage galleryPage = new OpenNanaGalleryPage();

        System.out.println("\n=== Testing Scroll Loading ===");
        
        galleryPage.navigateToGallery();
        Waits.sleep(3000);

        List<OpenNanaGalleryPage.CardInfo> cards = galleryPage.scrollToLoadCards(15);
        
        System.out.println("\n=== Results ===");
        System.out.println("Total cards loaded: " + cards.size());
        
        for (int i = 0; i < cards.size(); i++) {
            OpenNanaGalleryPage.CardInfo card = cards.get(i);
            System.out.println("\n" + (i + 1) + ". " + card.getTitle());
            System.out.println("   Image URL: " + (card.getImageUrl() != null ? card.getImageUrl().substring(0, 50) + "..." : "null"));
            System.out.println("   Detail URL: " + card.getDetailUrl());
        }
        
        System.out.println("\n=== Test Complete ===");
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
