package com.larry.data.model;

public class PromptData {
    private String title;
    private String imageUrl;
    private String promptEn;
    private String promptCh;
    private String sourceUrl;

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

    @Override
    public String toString() {
        return "PromptData{" +
                "title='" + title + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", promptEn='" + (promptEn != null ? promptEn.substring(0, Math.min(50, promptEn.length())) : "null") + "...'" +
                ", promptCh='" + (promptCh != null ? promptCh.substring(0, Math.min(50, promptCh.length())) : "null") + "...'" +
                '}';
    }
}
