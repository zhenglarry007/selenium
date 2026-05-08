package com.larry.api.opennana;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenNanaApiResponse {

    private int status;
    private String msg;
    private Data data;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Data {
        private List<Item> items;
        private Pagination pagination;

        public List<Item> getItems() {
            return items;
        }

        public void setItems(List<Item> items) {
            this.items = items;
        }

        public Pagination getPagination() {
            return pagination;
        }

        public void setPagination(Pagination pagination) {
            this.pagination = pagination;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Item {
        private Long id;
        private String slug;
        private String title;
        
        @JsonProperty("media_type")
        private String mediaType;
        
        @JsonProperty("cover_image")
        private String coverImage;
        
        @JsonProperty("_is_sponsor")
        private Boolean isSponsor;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

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

        public String getMediaType() {
            return mediaType;
        }

        public void setMediaType(String mediaType) {
            this.mediaType = mediaType;
        }

        public String getCoverImage() {
            return coverImage;
        }

        public void setCoverImage(String coverImage) {
            this.coverImage = coverImage;
        }

        public Boolean getIsSponsor() {
            return isSponsor;
        }

        public void setIsSponsor(Boolean isSponsor) {
            this.isSponsor = isSponsor;
        }

        public boolean isSponsor() {
            return Boolean.TRUE.equals(isSponsor);
        }

        public String getDetailUrl() {
            if (slug == null || slug.isEmpty()) {
                return null;
            }
            return "https://opennana.com/awesome-prompt-gallery/" + slug;
        }

        @Override
        public String toString() {
            return "Item{" +
                    "id=" + id +
                    ", slug='" + slug + '\'' +
                    ", title='" + title + '\'' +
                    ", isSponsor=" + isSponsor +
                    '}';
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Pagination {
        private int page;
        private int limit;
        private int total;
        
        @JsonProperty("total_pages")
        private int totalPages;
        
        @JsonProperty("has_more")
        private boolean hasMore;
        
        @JsonProperty("items_count")
        private int itemsCount;

        public int getPage() {
            return page;
        }

        public void setPage(int page) {
            this.page = page;
        }

        public int getLimit() {
            return limit;
        }

        public void setLimit(int limit) {
            this.limit = limit;
        }

        public int getTotal() {
            return total;
        }

        public void setTotal(int total) {
            this.total = total;
        }

        public int getTotalPages() {
            return totalPages;
        }

        public void setTotalPages(int totalPages) {
            this.totalPages = totalPages;
        }

        public boolean isHasMore() {
            return hasMore;
        }

        public void setHasMore(boolean hasMore) {
            this.hasMore = hasMore;
        }

        public int getItemsCount() {
            return itemsCount;
        }

        public void setItemsCount(int itemsCount) {
            this.itemsCount = itemsCount;
        }
    }
}
