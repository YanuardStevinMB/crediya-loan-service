package com.crediya.loan.model.shared;

import java.util.List;

public class Page<T> {
    private final List<T> items;
    private final long total;
    private final int page;
    private final int size;

    public Page(List<T> items, long total, int page, int size) {
        this.items = List.copyOf(items);
        this.total = total;
        this.page = page;
        this.size = size;
    }
    public List<T> items() { return items; }
    public long total() { return total; }
    public int page() { return page; }
    public int size() { return size; }
    public int totalPages() { return size == 0 ? 0 : (int) Math.ceil((double) total / (double) size); }
}
