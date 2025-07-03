package com.webstore.util;

import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class PaginationUtil {

    private static final int DEFAULT_ITEMS_PER_PAGE = 7;

    public static class PaginationResult<T> {
        private final List<T> items;
        private final int currentPage;
        private final int totalPages;
        private final int totalItems;
        private final boolean hasNext;
        private final boolean hasPrevious;

        public PaginationResult(List<T> items, int currentPage, int totalPages, int totalItems) {
            this.items = items;
            this.currentPage = currentPage;
            this.totalPages = totalPages;
            this.totalItems = totalItems;
            this.hasNext = currentPage < totalPages;
            this.hasPrevious = currentPage > 1;
        }

        public List<T> getItems() { return items; }
        public int getCurrentPage() { return currentPage; }
        public int getTotalPages() { return totalPages; }
        public int getTotalItems() { return totalItems; }
        public boolean hasNext() { return hasNext; }
        public boolean hasPrevious() { return hasPrevious; }
    }

    public <T> PaginationResult<T> paginate(List<T> allItems, int pageNumber) {
        return paginate(allItems, pageNumber, DEFAULT_ITEMS_PER_PAGE);
    }

    public <T> PaginationResult<T> paginate(List<T> allItems, int pageNumber, int itemsPerPage) {
        if (allItems.isEmpty()) {
            return new PaginationResult<>(allItems, 1, 1, 0);
        }

        int totalPages = (int) Math.ceil((double) allItems.size() / itemsPerPage);

        if (pageNumber < 1) pageNumber = 1;
        if (pageNumber > totalPages) pageNumber = totalPages;

        int startIndex = (pageNumber - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, allItems.size());

        List<T> pageItems = allItems.subList(startIndex, endIndex);
        return new PaginationResult<>(pageItems, pageNumber, totalPages, allItems.size());
    }

    public String encodeToBase64(String data) {
        return java.util.Base64.getEncoder().encodeToString(data.getBytes());
    }

    public String decodeFromBase64(String encodedData) {
        return new String(java.util.Base64.getDecoder().decode(encodedData));
    }
}