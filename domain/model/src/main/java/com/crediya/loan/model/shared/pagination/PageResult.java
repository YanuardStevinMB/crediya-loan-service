package com.crediya.loan.model.shared.pagination;


import java.util.List;

public final class PageResult<T> {
    private final List<T> items; private final int page; private final int size; private final long totalElements; private final int totalPages;
    public PageResult(List<T> items, int page, int size, long totalElements) {
        this.items = items; this.page = page; this.size = size; this.totalElements = totalElements;
        this.totalPages = (int) Math.max(1, Math.ceil(totalElements / (double) size));
    }
    public List<T> getItems() { return items; }
    public int getPage() { return page; }
    public int getSize() { return size; }
    public long getTotalElements() { return totalElements; }
    public int getTotalPages() { return totalPages; }
}