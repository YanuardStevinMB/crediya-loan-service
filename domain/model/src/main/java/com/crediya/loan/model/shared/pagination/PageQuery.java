package com.crediya.loan.model.shared.pagination;


import java.util.Collections;
import java.util.List;

public final class PageQuery {
    private final int page; private final int size; private final List<SortOrder> sort;
    public PageQuery(int page, int size, List<SortOrder> sort) {
        this.page = Math.max(0, page); this.size = Math.max(1, size);
        this.sort = sort == null ? Collections.emptyList() : List.copyOf(sort);
    }
    public int getPage() { return page; }
    public int getSize() { return size; }
    public List<SortOrder> getSort() { return sort; }
    public int offset() { return page * size; }
    public int limit() { return size; }
}