package com.larry.data.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.larry.data.model.PromptData;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class DataMerger {

    private static final String MASTER_FILE = "opennana_prompts_master.json";
    private static final String MASTER_CSV_FILE = "opennana_prompts_master.csv";
    private static final String ARCHIVE_DIR = "archive";

    private final ObjectMapper objectMapper;
    private final String outputDirectory;

    public DataMerger(String outputDirectory) {
        this.outputDirectory = outputDirectory;
        this.objectMapper = new ObjectMapper()
                .enable(SerializationFeature.INDENT_OUTPUT);
    }

    public MergeResult mergeNewData(List<PromptData> newData) {
        DataSaver.ensureDirectoryExists(outputDirectory);
        
        List<PromptData> existingData = loadMasterData();
        Map<String, PromptData> existingMap = existingData.stream()
                .collect(Collectors.toMap(
                        PromptData::getSourceUrl,
                        data -> data,
                        (existing, replacement) -> existing
                ));

        List<PromptData> addedItems = new ArrayList<>();
        List<PromptData> updatedItems = new ArrayList<>();
        List<PromptData> unchangedItems = new ArrayList<>();

        for (PromptData newItem : newData) {
            String sourceUrl = newItem.getSourceUrl();
            
            if (sourceUrl == null || sourceUrl.isEmpty()) {
                continue;
            }

            if (existingMap.containsKey(sourceUrl)) {
                PromptData existingItem = existingMap.get(sourceUrl);
                if (hasChanges(existingItem, newItem)) {
                    newItem.setUpdatedAt(LocalDateTime.now());
                    updatedItems.add(newItem);
                    existingMap.put(sourceUrl, newItem);
                } else {
                    unchangedItems.add(existingItem);
                }
            } else {
                newItem.setCreatedAt(LocalDateTime.now());
                addedItems.add(newItem);
                existingMap.put(sourceUrl, newItem);
            }
        }

        List<PromptData> mergedData = new ArrayList<>(existingMap.values());
        
        saveMasterData(mergedData);
        saveIncrementalData(addedItems, updatedItems);

        MergeResult result = new MergeResult();
        result.setTotalExisting(existingData.size());
        result.setTotalNew(newData.size());
        result.setAdded(addedItems.size());
        result.setUpdated(updatedItems.size());
        result.setUnchanged(unchangedItems.size());
        result.setTotalAfterMerge(mergedData.size());

        return result;
    }

    public List<PromptData> loadMasterData() {
        Path masterPath = Paths.get(outputDirectory, MASTER_FILE);
        
        if (!Files.exists(masterPath)) {
            System.out.println("📂 No master file found, starting with empty dataset");
            return new ArrayList<>();
        }

        try {
            String json = Files.readString(masterPath);
            return objectMapper.readValue(json, new TypeReference<List<PromptData>>() {});
        } catch (IOException e) {
            System.out.println("⚠️ Failed to load master data: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public void saveMasterData(List<PromptData> data) {
        DataSaver.ensureDirectoryExists(outputDirectory);
        
        Path masterPath = Paths.get(outputDirectory, MASTER_FILE);
        
        try {
            String json = objectMapper.writeValueAsString(data);
            Files.writeString(masterPath, json);
            System.out.println("💾 Master data saved: " + data.size() + " items");
        } catch (IOException e) {
            System.out.println("❌ Failed to save master data: " + e.getMessage());
        }

        try {
            String csvPath = outputDirectory + File.separator + MASTER_CSV_FILE;
            DataSaver.saveToCsv(data, csvPath);
        } catch (Exception e) {
            System.out.println("⚠️ Failed to save master CSV: " + e.getMessage());
        }
    }

    private void saveIncrementalData(List<PromptData> addedItems, List<PromptData> updatedItems) {
        if (addedItems.isEmpty() && updatedItems.isEmpty()) {
            return;
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        Path archivePath = Paths.get(outputDirectory, ARCHIVE_DIR);
        
        try {
            Files.createDirectories(archivePath);
        } catch (IOException e) {
            System.out.println("⚠️ Failed to create archive directory: " + e.getMessage());
        }

        if (!addedItems.isEmpty()) {
            String addedFileName = "incremental_added_" + timestamp + ".json";
            Path addedPath = archivePath.resolve(addedFileName);
            try {
                String json = objectMapper.writeValueAsString(addedItems);
                Files.writeString(addedPath, json);
                System.out.println("💾 Added items saved: " + addedItems.size() + " items -> " + addedFileName);
            } catch (IOException e) {
                System.out.println("⚠️ Failed to save added items: " + e.getMessage());
            }
        }

        if (!updatedItems.isEmpty()) {
            String updatedFileName = "incremental_updated_" + timestamp + ".json";
            Path updatedPath = archivePath.resolve(updatedFileName);
            try {
                String json = objectMapper.writeValueAsString(updatedItems);
                Files.writeString(updatedPath, json);
                System.out.println("💾 Updated items saved: " + updatedItems.size() + " items -> " + updatedFileName);
            } catch (IOException e) {
                System.out.println("⚠️ Failed to save updated items: " + e.getMessage());
            }
        }
    }

    private boolean hasChanges(PromptData existing, PromptData newData) {
        if (!equalsOrNull(existing.getTitle(), newData.getTitle())) {
            return true;
        }
        if (!equalsOrNull(existing.getImageUrl(), newData.getImageUrl())) {
            return true;
        }
        if (!equalsOrNull(existing.getPromptEn(), newData.getPromptEn())) {
            return true;
        }
        if (!equalsOrNull(existing.getPromptCh(), newData.getPromptCh())) {
            return true;
        }
        return false;
    }

    private boolean equalsOrNull(String a, String b) {
        if (a == null && b == null) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        return a.equals(b);
    }

    public int getMasterDataCount() {
        return loadMasterData().size();
    }

    public void printMasterSummary() {
        List<PromptData> masterData = loadMasterData();
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println("📊 Master Data Summary");
        System.out.println("=".repeat(60));
        System.out.println("   Total items: " + masterData.size());
        
        long withEn = masterData.stream()
                .filter(d -> d.getPromptEn() != null && !d.getPromptEn().isEmpty())
                .count();
        long withCh = masterData.stream()
                .filter(d -> d.getPromptCh() != null && !d.getPromptCh().isEmpty())
                .count();
        
        System.out.println("   With English prompt: " + withEn);
        System.out.println("   With Chinese prompt: " + withCh);
        System.out.println("=".repeat(60));
    }

    public static class MergeResult {
        private int totalExisting;
        private int totalNew;
        private int added;
        private int updated;
        private int unchanged;
        private int totalAfterMerge;

        public int getTotalExisting() {
            return totalExisting;
        }

        public void setTotalExisting(int totalExisting) {
            this.totalExisting = totalExisting;
        }

        public int getTotalNew() {
            return totalNew;
        }

        public void setTotalNew(int totalNew) {
            this.totalNew = totalNew;
        }

        public int getAdded() {
            return added;
        }

        public void setAdded(int added) {
            this.added = added;
        }

        public int getUpdated() {
            return updated;
        }

        public void setUpdated(int updated) {
            this.updated = updated;
        }

        public int getUnchanged() {
            return unchanged;
        }

        public void setUnchanged(int unchanged) {
            this.unchanged = unchanged;
        }

        public int getTotalAfterMerge() {
            return totalAfterMerge;
        }

        public void setTotalAfterMerge(int totalAfterMerge) {
            this.totalAfterMerge = totalAfterMerge;
        }

        public void printSummary() {
            System.out.println("\n" + "=".repeat(60));
            System.out.println("📊 Merge Result");
            System.out.println("=".repeat(60));
            System.out.println("   Existing items: " + totalExisting);
            System.out.println("   New items processed: " + totalNew);
            System.out.println("   - Added: " + added);
            System.out.println("   - Updated: " + updated);
            System.out.println("   - Unchanged: " + unchanged);
            System.out.println("   Total after merge: " + totalAfterMerge);
            
            if (added > 0 || updated > 0) {
                System.out.println("\n   🎉 Changes detected!");
                if (added > 0) {
                    System.out.println("      + " + added + " new items added");
                }
                if (updated > 0) {
                    System.out.println("      ~ " + updated + " items updated");
                }
            } else {
                System.out.println("\n   ✅ No changes detected");
            }
            System.out.println("=".repeat(60));
        }
    }
}
