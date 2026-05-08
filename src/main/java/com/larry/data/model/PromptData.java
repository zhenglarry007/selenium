package com.larry.data.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PromptData {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private String title;
    private String imageUrl;
    private String promptEn;
    private String promptCh;
    private String sourceUrl;
    
    private String createdAt;
    private String updatedAt;

    public PromptData() {
    }

    public PromptData(String title, String imageUrl, String promptEn, String promptCh) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.promptEn = promptEn;
        this.promptCh = promptCh;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getPromptEn() {
        return promptEn;
    }

    public void setPromptEn(String promptEn) {
        this.promptEn = promptEn;
    }

    public String getPromptCh() {
        return promptCh;
    }

    public void setPromptCh(String promptCh) {
        this.promptCh = promptCh;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        if (createdAt != null) {
            this.createdAt = createdAt.format(DATE_FORMATTER);
        }
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        if (updatedAt != null) {
            this.updatedAt = updatedAt.format(DATE_FORMATTER);
        }
    }

    @Override
    public String toString() {
        return "PromptData{" +
                "title='" + title + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", promptEn='" + (promptEn != null ? promptEn.substring(0, Math.min(50, promptEn.length())) : "null") + "...'" +
                ", promptCh='" + (promptCh != null ? promptCh.substring(0, Math.min(50, promptCh.length())) : "null") + "...'" +
                ", createdAt='" + createdAt + '\'' +
                ", updatedAt='" + updatedAt + '\'' +
                '}';
    }
}
