package com.crediya.loan.model.loantype;

import lombok.*;

import java.math.BigDecimal;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanType {

    private Long id;
    private String name;
    private BigDecimal amountMin;
    private BigDecimal amountMax;
    private BigDecimal interestRate;
    private Boolean automaticValidation;


}
