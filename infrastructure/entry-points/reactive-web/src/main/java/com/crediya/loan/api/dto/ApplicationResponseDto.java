package com.crediya.loan.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@SuperBuilder(toBuilder = true)  // 👈 usar solo este
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationResponseDto {
    private Long id;
    private BigDecimal amount;
    private LocalDate term;
    private String email;
    private String identityDocument;
    private Long stateId;
    private Long loanTypeId;
}
