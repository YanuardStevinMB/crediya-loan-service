package com.crediya.loan.model.requestsandusers;

import com.crediya.loan.model.application.ApplicationPagined;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class RequestsAndUsers extends ApplicationPagined {
    private String fullName;
    private BigDecimal baseSalary;
}
