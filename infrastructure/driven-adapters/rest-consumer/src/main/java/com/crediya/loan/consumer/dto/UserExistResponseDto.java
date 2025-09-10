package com.crediya.loan.consumer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserExistResponseDto {
    private boolean success;
    private String message;
    private Data data;
    private Object errors;
    private String path;
    private String timestamp;

    @lombok.Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Data {
        private boolean exists;
        private BigDecimal baseSalary;
    }
}
