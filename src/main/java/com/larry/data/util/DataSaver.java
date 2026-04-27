package com.larry.data.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.larry.data.model.PromptData;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DataSaver {

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    public static void saveToJson(List<PromptData> dataList, String filePath) {
        try {
            objectMapper.writeValue(new File(filePath), dataList);
            System.out.println("[DataSaver] Successfully saved " + dataList.size() + " records to: " + filePath);
        } catch (IOException e) {
            System.err.println("[DataSaver] Error saving to JSON: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void saveToJson(PromptData data, String filePath) {
        try {
            objectMapper.writeValue(new File(filePath), data);
            System.out.println("[DataSaver] Successfully saved record to: " + filePath);
        } catch (IOException e) {
            System.err.println("[DataSaver] Error saving to JSON: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static String generateOutputFilePath(String baseName) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        String timestamp = LocalDateTime.now().format(formatter);
        return baseName + "_" + timestamp + ".json";
    }

    public static void ensureDirectoryExists(String directoryPath) {
        try {
            Files.createDirectories(Paths.get(directoryPath));
        } catch (IOException e) {
            System.err.println("[DataSaver] Error creating directory: " + e.getMessage());
        }
    }

    public static void saveToCsv(List<PromptData> dataList, String filePath) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            writer.println("title,imageUrl,promptEn,promptCh,sourceUrl");
            
            for (PromptData data : dataList) {
                String line = String.join(",",
                        escapeCsv(data.getTitle()),
                        escapeCsv(data.getImageUrl()),
                        escapeCsv(data.getPromptEn()),
                        escapeCsv(data.getPromptCh()),
                        escapeCsv(data.getSourceUrl())
                );
                writer.println(line);
            }
            
            System.out.println("[DataSaver] Successfully saved " + dataList.size() + " records to CSV: " + filePath);
        } catch (IOException e) {
            System.err.println("[DataSaver] Error saving to CSV: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    public static void printSummary(List<PromptData> dataList) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("📊 Data Extraction Summary");
        System.out.println("=".repeat(60));
        System.out.println("Total records extracted: " + dataList.size());
        
        for (int i = 0; i < dataList.size(); i++) {
            PromptData data = dataList.get(i);
            System.out.println("\n[" + (i + 1) + "] " + data.getTitle());
            System.out.println("    Image URL: " + (data.getImageUrl() != null ? "✓" : "✗"));
            System.out.println("    English Prompt: " + (data.getPromptEn() != null ? "✓" : "✗"));
            System.out.println("    Chinese Prompt: " + (data.getPromptCh() != null ? "✓" : "✗"));
        }
        
        System.out.println("\n" + "=".repeat(60) + "\n");
    }
}
