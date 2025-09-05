package com.crediya.loan.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ApplicationPaginedDto (
    Long id,
    BigDecimal amount,
    LocalDate term,
    String email,
    String identityDocument,
   String state,
    String loan,
    Long stateId,
    Long loanTypeId,
   String fullName,
    BigDecimal baseSalary
){}
