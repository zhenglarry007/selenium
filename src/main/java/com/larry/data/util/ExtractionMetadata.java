package com.larry.data.util;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

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

public class ExtractionMetadata {

    private static final String METADATA_FILE = ".extraction_metadata.json";
    private static final String SLUG_INDEX_FILE = ".slug_index.json";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static ExtractionMetadata instance;
    private final ObjectMapper objectMapper;
    private final String outputDirectory;

    private Metadata metadata;
    private Map<String, SlugInfo> slugIndex;

    private ExtractionMetadata(String outputDirectory) {
        this.outputDirectory = outputDirectory;
        this.objectMapper = new ObjectMapper()
                .enable(SerializationFeature.INDENT_OUTPUT);
        this.metadata = new Metadata();
        this.slugIndex = new HashMap<>();
    }

    public static synchronized ExtractionMetadata getInstance(String outputDirectory) {
        if (instance == null) {
            instance = new ExtractionMetadata(outputDirectory);
            instance.load();
        }
        return instance;
    }

    public void load() {
        DataSaver.ensureDirectoryExists(outputDirectory);
        
        Path metadataPath = Paths.get(outputDirectory, METADATA_FILE);
        Path slugIndexPath = Paths.get(outputDirectory, SLUG_INDEX_FILE);

        if (Files.exists(metadataPath)) {
            try {
                String json = Files.readString(metadataPath);
                this.metadata = objectMapper.readValue(json, Metadata.class);
                System.out.println("📂 Loaded metadata: " + this.metadata.getTotalExtracted() + " items extracted");
            } catch (IOException e) {
                System.out.println("⚠️ Failed to load metadata: " + e.getMessage());
                this.metadata = new Metadata();
            }
        } else {
            this.metadata = new Metadata();
        }

        if (Files.exists(slugIndexPath)) {
            try {
                String json = Files.readString(slugIndexPath);
                SlugIndexWrapper wrapper = objectMapper.readValue(json, SlugIndexWrapper.class);
                this.slugIndex = wrapper.toMap();
                System.out.println("📂 Loaded slug index: " + this.slugIndex.size() + " items");
            } catch (IOException e) {
                System.out.println("⚠️ Failed to load slug index: " + e.getMessage());
                this.slugIndex = new HashMap<>();
            }
        } else {
            this.slugIndex = new HashMap<>();
        }
    }

    public void save() {
        DataSaver.ensureDirectoryExists(outputDirectory);
        
        Path metadataPath = Paths.get(outputDirectory, METADATA_FILE);
        Path slugIndexPath = Paths.get(outputDirectory, SLUG_INDEX_FILE);

        try {
            String metadataJson = objectMapper.writeValueAsString(this.metadata);
            Files.writeString(metadataPath, metadataJson);
            
            SlugIndexWrapper wrapper = new SlugIndexWrapper(this.slugIndex);
            String slugIndexJson = objectMapper.writeValueAsString(wrapper);
            Files.writeString(slugIndexPath, slugIndexJson);
            
            System.out.println("💾 Saved metadata: " + this.metadata.getTotalExtracted() + " items");
        } catch (IOException e) {
            System.out.println("❌ Failed to save metadata: " + e.getMessage());
        }
    }

    public boolean isExtracted(String slug) {
        return slugIndex.containsKey(slug);
    }

    public SlugInfo getSlugInfo(String slug) {
        return slugIndex.get(slug);
    }

    public void markExtracted(String slug, String title, String sourceUrl) {
        SlugInfo info = new SlugInfo();
        info.setSlug(slug);
        info.setTitle(title);
        info.setSourceUrl(sourceUrl);
        info.setExtractedAt(LocalDateTime.now());
        info.setStatus("success");
        
        slugIndex.put(slug, info);
        metadata.setTotalExtracted(slugIndex.size());
        metadata.setLastExtractedAt(LocalDateTime.now());
    }

    public void markFailed(String slug, String title, String errorMessage) {
        SlugInfo info = slugIndex.getOrDefault(slug, new SlugInfo());
        info.setSlug(slug);
        info.setTitle(title);
        info.setStatus("failed");
        info.setErrorMessage(errorMessage);
        info.setFailedAt(LocalDateTime.now());
        info.setRetryCount(info.getRetryCount() + 1);
        
        slugIndex.put(slug, info);
    }

    public List<String> getNewSlugs(List<String> allSlugs) {
        List<String> newSlugs = new ArrayList<>();
        for (String slug : allSlugs) {
            if (!isExtracted(slug)) {
                newSlugs.add(slug);
            }
        }
        return newSlugs;
    }

    public int getNewItemCount(List<String> allSlugs) {
        return (int) allSlugs.stream()
                .filter(slug -> !isExtracted(slug))
                .count();
    }

    public Set<String> getAllSlugs() {
        return new HashSet<>(slugIndex.keySet());
    }

    public List<SlugInfo> getFailedItems() {
        return slugIndex.values().stream()
                .filter(info -> "failed".equals(info.getStatus()))
                .toList();
    }

    public void clearFailedStatus() {
        slugIndex.values().stream()
                .filter(info -> "failed".equals(info.getStatus()))
                .forEach(info -> info.setStatus(null));
        save();
    }

    public void printSummary() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("📊 Extraction Summary");
        System.out.println("=".repeat(60));
        System.out.println("   Total extracted: " + metadata.getTotalExtracted());
        System.out.println("   Last extraction: " + 
                (metadata.getLastExtractedAt() != null && !metadata.getLastExtractedAt().isEmpty() ? 
                 metadata.getLastExtractedAt() : "never"));
        System.out.println("   Failed items: " + getFailedItems().size());
        System.out.println("=".repeat(60));
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public static class Metadata {
        private int totalExtracted;
        
        private String lastExtractedAt;
        
        private String firstExtractedAt;
        
        private List<ExtractionSession> sessions = new ArrayList<>();

        public int getTotalExtracted() {
            return totalExtracted;
        }

        public void setTotalExtracted(int totalExtracted) {
            this.totalExtracted = totalExtracted;
        }

        public String getLastExtractedAt() {
            return lastExtractedAt;
        }

        public void setLastExtractedAt(String lastExtractedAt) {
            this.lastExtractedAt = lastExtractedAt;
        }

        public void setLastExtractedAt(LocalDateTime lastExtractedAt) {
            if (lastExtractedAt != null) {
                this.lastExtractedAt = lastExtractedAt.format(DATE_FORMATTER);
                if (this.firstExtractedAt == null) {
                    this.firstExtractedAt = this.lastExtractedAt;
                }
            }
        }

        public String getFirstExtractedAt() {
            return firstExtractedAt;
        }

        public void setFirstExtractedAt(String firstExtractedAt) {
            this.firstExtractedAt = firstExtractedAt;
        }

        public void setFirstExtractedAt(LocalDateTime firstExtractedAt) {
            if (firstExtractedAt != null) {
                this.firstExtractedAt = firstExtractedAt.format(DATE_FORMATTER);
            }
        }

        public List<ExtractionSession> getSessions() {
            return sessions;
        }

        public void setSessions(List<ExtractionSession> sessions) {
            this.sessions = sessions;
        }

        public void addSession(ExtractionSession session) {
            this.sessions.add(session);
            if (this.sessions.size() > 50) {
                this.sessions = this.sessions.subList(this.sessions.size() - 50, this.sessions.size());
            }
        }
    }

    public static class ExtractionSession {
        private String sessionId;
        
        private String startedAt;
        
        private String completedAt;
        
        private int itemsProcessed;
        private int itemsAdded;
        private int itemsFailed;
        private int durationSeconds;

        public String getSessionId() {
            return sessionId;
        }

        public void setSessionId(String sessionId) {
            this.sessionId = sessionId;
        }

        public String getStartedAt() {
            return startedAt;
        }

        public void setStartedAt(String startedAt) {
            this.startedAt = startedAt;
        }

        public void setStartedAt(LocalDateTime startedAt) {
            if (startedAt != null) {
                this.startedAt = startedAt.format(DATE_FORMATTER);
            }
        }

        public String getCompletedAt() {
            return completedAt;
        }

        public void setCompletedAt(String completedAt) {
            this.completedAt = completedAt;
        }

        public void setCompletedAt(LocalDateTime completedAt) {
            if (completedAt != null) {
                this.completedAt = completedAt.format(DATE_FORMATTER);
            }
        }

        public int getItemsProcessed() {
            return itemsProcessed;
        }

        public void setItemsProcessed(int itemsProcessed) {
            this.itemsProcessed = itemsProcessed;
        }

        public int getItemsAdded() {
            return itemsAdded;
        }

        public void setItemsAdded(int itemsAdded) {
            this.itemsAdded = itemsAdded;
        }

        public int getItemsFailed() {
            return itemsFailed;
        }

        public void setItemsFailed(int itemsFailed) {
            this.itemsFailed = itemsFailed;
        }

        public int getDurationSeconds() {
            return durationSeconds;
        }

        public void setDurationSeconds(int durationSeconds) {
            this.durationSeconds = durationSeconds;
        }
    }

    public static class SlugInfo {
        private String slug;
        private String title;
        private String sourceUrl;
        private String status;
        private String errorMessage;
        private int retryCount;
        
        private String extractedAt;
        
        private String failedAt;

        public String getSlug() {
            return slug;
        }

        public void setSlug(String slug) {
            this.slug = slug;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getSourceUrl() {
            return sourceUrl;
        }

        public void setSourceUrl(String sourceUrl) {
            this.sourceUrl = sourceUrl;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public int getRetryCount() {
            return retryCount;
        }

        public void setRetryCount(int retryCount) {
            this.retryCount = retryCount;
        }

        public String getExtractedAt() {
            return extractedAt;
        }

        public void setExtractedAt(String extractedAt) {
            this.extractedAt = extractedAt;
        }

        public void setExtractedAt(LocalDateTime extractedAt) {
            if (extractedAt != null) {
                this.extractedAt = extractedAt.format(DATE_FORMATTER);
            }
        }

        public String getFailedAt() {
            return failedAt;
        }

        public void setFailedAt(String failedAt) {
            this.failedAt = failedAt;
        }

        public void setFailedAt(LocalDateTime failedAt) {
            if (failedAt != null) {
                this.failedAt = failedAt.format(DATE_FORMATTER);
            }
        }
    }

    private static class SlugIndexWrapper {
        private List<SlugInfoEntry> entries = new ArrayList<>();

        public SlugIndexWrapper() {}

        public SlugIndexWrapper(Map<String, SlugInfo> map) {
            for (Map.Entry<String, SlugInfo> entry : map.entrySet()) {
                SlugInfoEntry e = new SlugInfoEntry();
                e.setKey(entry.getKey());
                e.setValue(entry.getValue());
                entries.add(e);
            }
        }

        public Map<String, SlugInfo> toMap() {
            Map<String, SlugInfo> map = new HashMap<>();
            for (SlugInfoEntry entry : entries) {
                map.put(entry.getKey(), entry.getValue());
            }
            return map;
        }

        public List<SlugInfoEntry> getEntries() {
            return entries;
        }

        public void setEntries(List<SlugInfoEntry> entries) {
            this.entries = entries;
        }
    }

    private static class SlugInfoEntry {
        private String key;
        private SlugInfo value;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public SlugInfo getValue() {
            return value;
        }

        public void setValue(SlugInfo value) {
            this.value = value;
        }
    }
}
