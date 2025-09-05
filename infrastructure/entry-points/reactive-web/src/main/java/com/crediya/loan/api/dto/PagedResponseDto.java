package com.crediya.loan.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PagedResponseDto<T> {
    private int page_number;
    private int page_size;
    private long total_record_count;
    private List<T> records;

    public PagedResponseDto(int page_number, int page_size, long total_record_count, List<T> records) {
        this.page_number = page_number;
        this.page_size = page_size;
        this.total_record_count = total_record_count;
        this.records = records;
    }

    public int getPage_number() { return page_number; }
    public int getPage_size() { return page_size; }
    public long getTotal_record_count() { return total_record_count; }
    public List<T> getRecords() { return records; }
}