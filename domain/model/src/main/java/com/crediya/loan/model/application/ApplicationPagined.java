package com.crediya.loan.model.application;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class ApplicationPagined {

        private Long id;
        private BigDecimal amount;
        private LocalDate term;
        private String email;
        private String identityDocument;
        private String state;
        private String loan;
        private Long stateId;
        private Long loanTypeId;
    private String fullName;
    private BigDecimal baseSalary;

}
