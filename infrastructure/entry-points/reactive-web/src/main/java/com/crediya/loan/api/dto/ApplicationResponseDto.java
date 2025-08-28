package com.crediya.loan.api.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ApplicationResponseDto {
    private Long id;
    private BigDecimal amount;
    private LocalDate term;
    private String email;
    private String identityDocument;
    private Long stateId;
    private Long loanTypeId;
}