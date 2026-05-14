package com.larry.api.opennana;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class OpenNanaApiClient {

    private static final String BASE_API_URL = "https://api.opennana.com/api/prompts";
    private static final String DEFAULT_MODEL = "ChatGPT";
    private static final String DEFAULT_SORT = "reviewed_at";
    private static final String DEFAULT_ORDER = "DESC";
    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_RETRIES = 3;
    private static final int ALL_ITEMS = Integer.MAX_VALUE;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private int totalCount = 0;
    private int fetchedCount = 0;

    public OpenNanaApiClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public static int allItems() {
        return ALL_ITEMS;
    }

    public List<OpenNanaApiResponse.Item> fetchAllPrompts() {
        return fetchAllPrompts(DEFAULT_MODEL, ALL_ITEMS);
    }

    public List<OpenNanaApiResponse.Item> fetchAllPrompts(int maxItems) {
        return fetchAllPrompts(DEFAULT_MODEL, maxItems);
    }

    public List<OpenNanaApiResponse.Item> fetchAllPrompts(String model, int maxItems) {
        List<OpenNanaApiResponse.Item> allItems = new ArrayList<>();
        int page = 1;
        boolean hasMore = true;
        this.totalCount = 0;
        this.fetchedCount = 0;

        String maxItemsDisplay = (maxItems == ALL_ITEMS) ? "ALL" : String.valueOf(maxItems);
        
        System.out.println("\n" + "=".repeat(70));
        System.out.println("📥 Fetching prompts from OpenNana API...");
        System.out.println("   Model: " + model);
        System.out.println("   Max items: " + maxItemsDisplay);
        System.out.println("=".repeat(70) + "\n");

        while (hasMore && allItems.size() < maxItems) {
            System.out.println("📄 Fetching page " + page + "...");
            
            OpenNanaApiResponse response = fetchPage(model, page, DEFAULT_LIMIT);
            
            if (response == null || response.getData() == null) {
                System.out.println("⚠️ No response data, stopping...");
                break;
            }

            OpenNanaApiResponse.Pagination pagination = response.getData().getPagination();
            if (pagination != null && this.totalCount == 0) {
                this.totalCount = pagination.getTotal();
                System.out.println("📊 Total available: " + pagination.getTotal() + 
                        " items in " + pagination.getTotalPages() + " pages");
            }

            List<OpenNanaApiResponse.Item> items = response.getData().getItems();
            if (items == null || items.isEmpty()) {
                System.out.println("⚠️ No items in response, stopping...");
                break;
            }

            int pageSponsorCount = 0;
            int pageAddedCount = 0;

            for (OpenNanaApiResponse.Item item : items) {
                if (item.isSponsor()) {
                    pageSponsorCount++;
                    System.out.println("  ⏭️ Skipping sponsor: " + item.getTitle());
                    continue;
                }
                
                allItems.add(item);
                pageAddedCount++;
                this.fetchedCount = allItems.size();
                
                String progress = (this.totalCount > 0) ? 
                    String.format("%.1f%%", (double) this.fetchedCount / this.totalCount * 100) : 
                    String.valueOf(this.fetchedCount);
                
                System.out.println("  ✅ [" + progress + "] " + this.fetchedCount + ". " + item.getTitle());
                
                if (allItems.size() >= maxItems) {
                    System.out.println("🎯 Reached target count: " + maxItems);
                    break;
                }
            }

            System.out.println("  📊 Page " + page + ": added " + pageAddedCount + 
                    (pageSponsorCount > 0 ? ", skipped " + pageSponsorCount + " sponsors" : ""));

            if (pagination != null) {
                hasMore = pagination.isHasMore();
                System.out.println("  📈 Progress: " + allItems.size() + "/" + pagination.getTotal() + 
                        ", Has more: " + hasMore);
            } else {
                hasMore = items.size() >= DEFAULT_LIMIT;
            }

            page++;
            
            if (hasMore && allItems.size() < maxItems) {
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println("⚠️ Interrupted, stopping...");
                    break;
                }
            }
        }

        System.out.println("\n" + "=".repeat(70));
        System.out.println("✅ API fetch complete!");
        System.out.println("   Total fetched: " + allItems.size() + " items");
        if (this.totalCount > 0) {
            System.out.println("   Total available: " + this.totalCount + " items");
            System.out.println("   Coverage: " + String.format("%.1f%%", 
                    (double) allItems.size() / this.totalCount * 100));
        }
        System.out.println("=".repeat(70));

        return allItems;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public int getFetchedCount() {
        return fetchedCount;
    }

    public OpenNanaApiResponse fetchPage(String model, int page, int limit) {
        String url = buildApiUrl(model, page, limit);
        System.out.println("  API URL: " + url);

        for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Accept", "application/json")
                        .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36")
                        .timeout(Duration.ofSeconds(30))
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    return objectMapper.readValue(response.body(), OpenNanaApiResponse.class);
                } else {
                    System.out.println("  ⚠️ HTTP status: " + response.statusCode() + ", attempt " + (attempt + 1) + "/" + MAX_RETRIES);
                }

            } catch (IOException | InterruptedException e) {
                System.out.println("  ⚠️ Request error (attempt " + (attempt + 1) + "/" + MAX_RETRIES + "): " + e.getMessage());
                
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }

            if (attempt < MAX_RETRIES - 1) {
                try {
                    Thread.sleep(1000 * (attempt + 1));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        return null;
    }

    private String buildApiUrl(String model, int page, int limit) {
        String encodedModel = URLEncoder.encode(model, StandardCharsets.UTF_8).replace("+", "%20");
        return String.format("%s?page=%d&limit=%d&sort=%s&order=%s&model=%s&media_type=image",
                BASE_API_URL, page, limit, DEFAULT_SORT, DEFAULT_ORDER, encodedModel);
    }
}
