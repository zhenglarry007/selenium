package com.larry.test;

import com.larry.api.opennana.OpenNanaApiClient;
import com.larry.api.opennana.OpenNanaApiResponse;
import com.larry.data.model.PromptData;
import com.larry.data.util.DataMerger;
import com.larry.data.util.DataSaver;
import com.larry.data.util.ExtractionMetadata;
import com.larry.driver.DriverManager;
import com.larry.driver.TargetFactory;
import com.larry.page.opennana.OpenNanaDetailPage;
import com.larry.wait.Waits;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class OpenNanaScraperTest {

    private static final int TARGET_CARD_COUNT = 10;
    private static final String OUTPUT_DIRECTORY = "output";
    private static final String DEFAULT_MODEL = "ChatGPT";
    
    private static final long DETAIL_PAGE_DELAY_MS = 1500;
    private static final boolean ENABLE_VERBOSE_LOGGING = false;

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

    @Test(description = "Incremental extraction - only extract new items")
    public void extractIncremental() {
        DataSaver.ensureDirectoryExists(OUTPUT_DIRECTORY);
        
        ExtractionMetadata metadata = ExtractionMetadata.getInstance(OUTPUT_DIRECTORY);
        DataMerger merger = new DataMerger(OUTPUT_DIRECTORY);
        
        System.out.println("\n" + "=".repeat(70));
        System.out.println("🔄 Incremental Extraction Mode");
        System.out.println("=".repeat(70));
        
        metadata.printSummary();
        merger.printMasterSummary();

        OpenNanaApiClient apiClient = new OpenNanaApiClient();
        OpenNanaDetailPage detailPage = new OpenNanaDetailPage();

        System.out.println("\n" + "=".repeat(70));
        System.out.println("📥 Step 1: Fetching card list from OpenNana API");
        System.out.println("=".repeat(70));

        List<OpenNanaApiResponse.Item> items = apiClient.fetchAllPrompts();
        
        if (items == null || items.isEmpty()) {
            System.out.println("❌ No items fetched from API!");
            return;
        }

        List<String> allSlugs = items.stream()
                .map(OpenNanaApiResponse.Item::getSlug)
                .toList();

        int newItemCount = metadata.getNewItemCount(allSlugs);
        int existingItemCount = metadata.getAllSlugs().size();

        System.out.println("\n" + "=".repeat(70));
        System.out.println("📊 Analysis");
        System.out.println("=".repeat(70));
        System.out.println("   Total in API: " + items.size());
        System.out.println("   Already extracted: " + existingItemCount);
        System.out.println("   New items to extract: " + newItemCount);

        if (newItemCount == 0) {
            System.out.println("\n✅ No new items to extract. You are up to date!");
            System.out.println("=".repeat(70));
            return;
        }

        List<OpenNanaApiResponse.Item> newItems = new ArrayList<>();
        for (OpenNanaApiResponse.Item item : items) {
            if (!metadata.isExtracted(item.getSlug())) {
                newItems.add(item);
            }
        }

        System.out.println("\nNew items to extract:");
        for (int i = 0; i < Math.min(newItems.size(), 20); i++) {
            System.out.println("   " + (i + 1) + ". " + newItems.get(i).getTitle());
        }
        if (newItems.size() > 20) {
            System.out.println("   ... and " + (newItems.size() - 20) + " more");
        }

        System.out.println("\n" + "=".repeat(70));
        System.out.println("🔍 Step 2: Extracting new data from detail pages");
        System.out.println("   Delay between pages: " + (DETAIL_PAGE_DELAY_MS / 1000.0) + "s");
        System.out.println("=".repeat(70));

        List<PromptData> extractedData = new ArrayList<>();
        int successCount = 0;
        int errorCount = 0;
        int startTime = (int) (System.currentTimeMillis() / 1000);

        ExtractionMetadata.ExtractionSession session = new ExtractionMetadata.ExtractionSession();
        session.setSessionId("incremental_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));
        session.setStartedAt(LocalDateTime.now());

        for (int i = 0; i < newItems.size(); i++) {
            OpenNanaApiResponse.Item item = newItems.get(i);
            String detailUrl = item.getDetailUrl();
            String slug = item.getSlug();

            int currentIndex = i + 1;
            int remaining = newItems.size() - i - 1;
            
            System.out.println("\n" + "-".repeat(70));
            System.out.println("📝 [" + currentIndex + "/" + newItems.size() + "] " + item.getTitle());
            
            double progress = (double) currentIndex / newItems.size() * 100;
            int elapsed = (int) (System.currentTimeMillis() / 1000) - startTime;
            int eta = (elapsed > 0 && currentIndex > 0) ? 
                (int) ((double) elapsed / currentIndex * remaining) : 0;
            
            System.out.println("   Progress: " + String.format("%.1f%%", progress) + 
                    " | Elapsed: " + formatDuration(elapsed) + 
                    " | ETA: " + formatDuration(eta));
            System.out.println("   URL: " + detailUrl);
            System.out.println("-".repeat(70) + "\n");

            try {
                if (detailUrl == null || detailUrl.isEmpty()) {
                    System.out.println("⚠️ Warning: Detail URL is empty, skipping this item");
                    metadata.markFailed(slug, item.getTitle(), "Empty URL");
                    errorCount++;
                    continue;
                }

                DriverManager.getDriver().get(detailUrl);
                Waits.sleep(2000);

                String currentUrl = detailPage.getCurrentUrl();
                String pageTitle = detailPage.getPageTitle();
                String sampleImageUrl = detailPage.getFirstSampleImageUrl();

                List<String> allPrompts = detailPage.extractPrompts();

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

                PromptData data = new PromptData();
                data.setTitle(pageTitle != null ? pageTitle : item.getTitle());
                data.setImageUrl(sampleImageUrl != null ? sampleImageUrl : item.getCoverImage());
                data.setPromptEn(promptEn);
                data.setPromptCh(promptCh);
                data.setSourceUrl(currentUrl);
                data.setCreatedAt(LocalDateTime.now());

                extractedData.add(data);
                successCount++;
                
                metadata.markExtracted(slug, item.getTitle(), currentUrl);
                metadata.save();

                System.out.println("\n✅ Successfully extracted!");
                System.out.println("   English: " + (promptEn != null ? "✓" : "✗"));
                System.out.println("   Chinese: " + (promptCh != null ? "✓" : "✗"));

            } catch (Exception e) {
                errorCount++;
                metadata.markFailed(slug, item.getTitle(), e.getMessage());
                metadata.save();
                System.out.println("❌ Error processing item: " + e.getMessage());
                if (ENABLE_VERBOSE_LOGGING) {
                    e.printStackTrace();
                }
            }

            if (i < newItems.size() - 1 && DETAIL_PAGE_DELAY_MS > 0) {
                Waits.sleep(DETAIL_PAGE_DELAY_MS);
            }
        }

        int totalTime = (int) (System.currentTimeMillis() / 1000) - startTime;
        
        session.setCompletedAt(LocalDateTime.now());
        session.setItemsProcessed(newItems.size());
        session.setItemsAdded(successCount);
        session.setItemsFailed(errorCount);
        session.setDurationSeconds(totalTime);
        
        metadata.getMetadata().addSession(session);
        metadata.save();

        System.out.println("\n" + "=".repeat(70));
        System.out.println("🎉 Incremental Extraction Complete!");
        System.out.println("=".repeat(70));
        System.out.println("   Total new items: " + newItems.size());
        System.out.println("   Success: " + successCount);
        System.out.println("   Errors: " + errorCount);
        System.out.println("   Total time: " + formatDuration(totalTime));
        
        DataSaver.printSummary(extractedData);

        System.out.println("\n" + "=".repeat(70));
        System.out.println("🔄 Step 3: Merging with master data");
        System.out.println("=".repeat(70));

        DataMerger.MergeResult mergeResult = merger.mergeNewData(extractedData);
        mergeResult.printSummary();

        System.out.println("\n📊 Final Summary");
        metadata.printSummary();
        merger.printMasterSummary();
    }

    @Test(description = "Check for incremental updates (no extraction)")
    public void checkForUpdates() {
        DataSaver.ensureDirectoryExists(OUTPUT_DIRECTORY);
        
        ExtractionMetadata metadata = ExtractionMetadata.getInstance(OUTPUT_DIRECTORY);
        DataMerger merger = new DataMerger(OUTPUT_DIRECTORY);
        
        System.out.println("\n" + "=".repeat(70));
        System.out.println("🔍 Checking for Updates");
        System.out.println("=".repeat(70));
        
        metadata.printSummary();
        merger.printMasterSummary();

        OpenNanaApiClient apiClient = new OpenNanaApiClient();

        System.out.println("\n📥 Fetching latest data from API...");

        List<OpenNanaApiResponse.Item> items = apiClient.fetchAllPrompts();
        
        if (items == null || items.isEmpty()) {
            System.out.println("❌ No items fetched from API!");
            return;
        }

        List<String> allSlugs = items.stream()
                .map(OpenNanaApiResponse.Item::getSlug)
                .toList();

        int newItemCount = metadata.getNewItemCount(allSlugs);
        int existingItemCount = metadata.getAllSlugs().size();

        System.out.println("\n" + "=".repeat(70));
        System.out.println("📊 Update Analysis");
        System.out.println("=".repeat(70));
        System.out.println("   Total in API: " + items.size());
        System.out.println("   Already extracted: " + existingItemCount);
        System.out.println("   New items available: " + newItemCount);

        if (newItemCount == 0) {
            System.out.println("\n✅ You are up to date! No new items to extract.");
        } else {
            System.out.println("\n⚠️ There are " + newItemCount + " new items available.");
            System.out.println("   Run 'extractIncremental' to extract them.");
        }

        List<ExtractionMetadata.SlugInfo> failedItems = metadata.getFailedItems();
        if (!failedItems.isEmpty()) {
            System.out.println("\n⚠️ Failed items from previous runs: " + failedItems.size());
            for (ExtractionMetadata.SlugInfo item : failedItems) {
                System.out.println("   - " + item.getTitle() + ": " + item.getErrorMessage());
            }
        }

        System.out.println("=".repeat(70));
    }

    @Test(description = "Extract ALL prompts (full extraction) - API + Selenium")
    public void extractAllPrompts() {
        extractPrompts(OpenNanaApiClient.allItems(), false);
    }

    @Test(description = "Extract ALL prompts with resume support")
    public void extractAllPromptsWithResume() {
        extractPrompts(OpenNanaApiClient.allItems(), true);
    }

    @Test(description = "Extract prompt data using API + Selenium hybrid approach")
    public void extractPromptData() {
        extractPrompts(TARGET_CARD_COUNT, false);
    }

    private void extractPrompts(int maxItems, boolean enableResume) {
        DataSaver.ensureDirectoryExists(OUTPUT_DIRECTORY);
        
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        
        OpenNanaApiClient apiClient = new OpenNanaApiClient();
        OpenNanaDetailPage detailPage = new OpenNanaDetailPage();
        List<PromptData> extractedData = new ArrayList<>();
        
        ExtractionMetadata metadata = null;
        if (enableResume) {
            metadata = ExtractionMetadata.getInstance(OUTPUT_DIRECTORY);
            System.out.println("\n📂 Resumed from previous session: " + 
                    metadata.getMetadata().getTotalExtracted() + " items already processed");
        }

        System.out.println("\n" + "=".repeat(70));
        System.out.println("📥 Step 1: Fetching card list from OpenNana API");
        System.out.println("=".repeat(70));

        List<OpenNanaApiResponse.Item> items;
        if (maxItems == OpenNanaApiClient.allItems()) {
            items = apiClient.fetchAllPrompts(DEFAULT_MODEL, maxItems);
        } else {
            items = apiClient.fetchAllPrompts(DEFAULT_MODEL, maxItems);
        }
        
        if (items == null || items.isEmpty()) {
            System.out.println("❌ No items fetched from API!");
            return;
        }

        int totalAvailable = items.size();
        int itemsToProcess = (maxItems == OpenNanaApiClient.allItems()) ? 
            totalAvailable : Math.min(totalAvailable, maxItems);
        
        System.out.println("\n" + "=".repeat(70));
        System.out.println("📋 Preloaded " + totalAvailable + " items from API!");
        System.out.println("=".repeat(70));
        
        if (ENABLE_VERBOSE_LOGGING) {
            for (int i = 0; i < Math.min(itemsToProcess, 20); i++) {
                OpenNanaApiResponse.Item item = items.get(i);
                System.out.println("  " + (i + 1) + ". " + item.getTitle());
                System.out.println("     URL: " + item.getDetailUrl());
            }
            if (itemsToProcess > 20) {
                System.out.println("  ... and " + (itemsToProcess - 20) + " more");
            }
        }

        System.out.println("\n" + "=".repeat(70));
        System.out.println("🔍 Step 2: Extracting data from each detail page using Selenium");
        System.out.println("   Delay between pages: " + (DETAIL_PAGE_DELAY_MS / 1000.0) + "s");
        System.out.println("=".repeat(70));

        int successCount = 0;
        int errorCount = 0;
        int skipCount = 0;
        int startTime = (int) (System.currentTimeMillis() / 1000);

        for (int i = 0; i < items.size(); i++) {
            OpenNanaApiResponse.Item item = items.get(i);
            String detailUrl = item.getDetailUrl();
            String slug = item.getSlug();
            
            if (enableResume && metadata != null && metadata.isExtracted(slug)) {
                skipCount++;
                System.out.println("\n⏭️ [" + (i + 1) + "/" + items.size() + "] Skipping (already processed): " + item.getTitle());
                continue;
            }

            int currentIndex = i + 1 - skipCount;
            int remaining = items.size() - i - 1;
            
            System.out.println("\n" + "-".repeat(70));
            System.out.println("📝 [" + currentIndex + "/" + itemsToProcess + "] " + item.getTitle());
            
            if (totalAvailable > 0) {
                double progress = (double) currentIndex / itemsToProcess * 100;
                int elapsed = (int) (System.currentTimeMillis() / 1000) - startTime;
                int eta = (elapsed > 0 && currentIndex > 0) ? 
                    (int) ((double) elapsed / currentIndex * remaining) : 0;
                
                System.out.println("   Progress: " + String.format("%.1f%%", progress) + 
                        " | Elapsed: " + formatDuration(elapsed) + 
                        " | ETA: " + formatDuration(eta));
            }
            System.out.println("   URL: " + detailUrl);
            System.out.println("-".repeat(70) + "\n");

            try {
                if (detailUrl == null || detailUrl.isEmpty()) {
                    System.out.println("⚠️ Warning: Detail URL is empty, skipping this item");
                    if (metadata != null) {
                        metadata.markFailed(slug, item.getTitle(), "Empty URL");
                    }
                    errorCount++;
                    continue;
                }

                if (ENABLE_VERBOSE_LOGGING) {
                    System.out.println("🌐 Navigating to detail page...");
                }
                DriverManager.getDriver().get(detailUrl);
                Waits.sleep(2000);

                String currentUrl = detailPage.getCurrentUrl();
                String pageTitle = detailPage.getPageTitle();
                String sampleImageUrl = detailPage.getFirstSampleImageUrl();

                if (ENABLE_VERBOSE_LOGGING) {
                    System.out.println("📍 Current URL: " + currentUrl);
                    System.out.println("📄 Page Title: " + pageTitle);
                    System.out.println("🖼️ Sample Image URL: " + (sampleImageUrl != null ? "found" : "null"));
                    System.out.println("\n🔍 Extracting prompts...");
                }

                List<String> allPrompts = detailPage.extractPrompts();
                
                if (ENABLE_VERBOSE_LOGGING) {
                    System.out.println("Found " + allPrompts.size() + " potential prompts");
                    
                    for (int j = 0; j < allPrompts.size(); j++) {
                        String p = allPrompts.get(j);
                        System.out.println("\nPrompt " + (j + 1) + " (length: " + p.length() + "):");
                        System.out.println(p.substring(0, Math.min(200, p.length())));
                        if (p.length() > 200) {
                            System.out.println("... (truncated, total length: " + p.length() + ")");
                        }
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

                if (ENABLE_VERBOSE_LOGGING) {
                    System.out.println("\n--- Final Selection ---");
                    System.out.println("English Prompt: " + (promptEn != null ? 
                        promptEn.substring(0, Math.min(100, promptEn.length())) + "..." : "null"));
                    System.out.println("Chinese Prompt: " + (promptCh != null ? 
                        promptCh.substring(0, Math.min(100, promptCh.length())) + "..." : "null"));
                }

                PromptData data = new PromptData();
                data.setTitle(pageTitle != null ? pageTitle : item.getTitle());
                data.setImageUrl(sampleImageUrl != null ? sampleImageUrl : item.getCoverImage());
                data.setPromptEn(promptEn);
                data.setPromptCh(promptCh);
                data.setSourceUrl(currentUrl);
                data.setCreatedAt(LocalDateTime.now());

                extractedData.add(data);
                successCount++;
                
                if (enableResume && metadata != null) {
                    metadata.markExtracted(slug, item.getTitle(), currentUrl);
                    metadata.save();
                }
                
                System.out.println("\n✅ Successfully extracted!");
                System.out.println("   English: " + (promptEn != null ? "✓" : "✗"));
                System.out.println("   Chinese: " + (promptCh != null ? "✓" : "✗"));

            } catch (Exception e) {
                errorCount++;
                if (metadata != null) {
                    metadata.markFailed(slug, item.getTitle(), e.getMessage());
                    metadata.save();
                }
                System.out.println("❌ Error processing item: " + e.getMessage());
                if (ENABLE_VERBOSE_LOGGING) {
                    e.printStackTrace();
                }
            }

            if (i < items.size() - 1 && DETAIL_PAGE_DELAY_MS > 0) {
                if (ENABLE_VERBOSE_LOGGING) {
                    System.out.println("\n⏳ Waiting " + (DETAIL_PAGE_DELAY_MS / 1000.0) + "s before next page...");
                }
                Waits.sleep(DETAIL_PAGE_DELAY_MS);
            }
        }

        int totalTime = (int) (System.currentTimeMillis() / 1000) - startTime;
        
        System.out.println("\n" + "=".repeat(70));
        System.out.println("🎉 Data Extraction Complete!");
        System.out.println("=".repeat(70));
        System.out.println("   Total items: " + items.size());
        System.out.println("   Success: " + successCount);
        System.out.println("   Errors: " + errorCount);
        System.out.println("   Skipped: " + skipCount);
        System.out.println("   Total time: " + formatDuration(totalTime));
        System.out.println("   Avg per item: " + (totalTime > 0 && successCount > 0 ? 
            String.format("%.1fs", (double) totalTime / successCount) : "N/A"));
        
        DataSaver.printSummary(extractedData);

        String baseFileName = "opennana_prompts_" + timestamp;
        String jsonFilePath = OUTPUT_DIRECTORY + "/" + baseFileName + ".json";
        String csvFilePath = OUTPUT_DIRECTORY + "/" + baseFileName + ".csv";
        
        DataSaver.saveToJson(extractedData, jsonFilePath);
        DataSaver.saveToCsv(extractedData, csvFilePath);

        System.out.println("\n✅ Files saved:");
        System.out.println("   JSON: " + jsonFilePath);
        System.out.println("   CSV: " + csvFilePath);
    }

    @Test(description = "Test API fetch only (no Selenium)")
    public void testApiFetch() {
        OpenNanaApiClient apiClient = new OpenNanaApiClient();

        System.out.println("\n=== Testing API Fetch ===");
        
        List<OpenNanaApiResponse.Item> items = apiClient.fetchAllPrompts(DEFAULT_MODEL, 15);
        
        System.out.println("\n=== Results ===");
        System.out.println("Total items fetched: " + (items != null ? items.size() : 0));
        
        if (items != null) {
            for (int i = 0; i < items.size(); i++) {
                OpenNanaApiResponse.Item item = items.get(i);
                System.out.println("\n" + (i + 1) + ". " + item.getTitle());
                System.out.println("   ID: " + item.getId());
                System.out.println("   Slug: " + item.getSlug());
                System.out.println("   Cover Image: " + (item.getCoverImage() != null ? 
                    item.getCoverImage().substring(0, Math.min(50, item.getCoverImage().length())) + "..." : "null"));
                System.out.println("   Detail URL: " + item.getDetailUrl());
            }
        }
        
        System.out.println("\n=== Test Complete ===");
    }

    @Test(description = "Test fetch ALL from API (no Selenium)")
    public void testApiFetchAll() {
        OpenNanaApiClient apiClient = new OpenNanaApiClient();

        System.out.println("\n" + "=".repeat(70));
        System.out.println("📥 Testing API Fetch ALL");
        System.out.println("=".repeat(70));
        
        List<OpenNanaApiResponse.Item> items = apiClient.fetchAllPrompts();
        
        System.out.println("\n=== Results ===");
        System.out.println("Total items fetched: " + (items != null ? items.size() : 0));
        
        if (items != null && !items.isEmpty()) {
            System.out.println("\nFirst 10 items:");
            for (int i = 0; i < Math.min(items.size(), 10); i++) {
                OpenNanaApiResponse.Item item = items.get(i);
                System.out.println("   " + (i + 1) + ". " + item.getTitle());
            }
            
            if (items.size() > 10) {
                System.out.println("   ... and " + (items.size() - 10) + " more");
            }
        }
        
        System.out.println("\n=== Test Complete ===");
    }

    private String formatDuration(int seconds) {
        if (seconds < 60) {
            return seconds + "s";
        } else if (seconds < 3600) {
            return (seconds / 60) + "m " + (seconds % 60) + "s";
        } else {
            return (seconds / 3600) + "h " + ((seconds % 3600) / 60) + "m";
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
