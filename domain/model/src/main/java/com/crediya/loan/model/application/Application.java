package com.crediya.loan.model.application;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)

public class Application {
    private Long id;
    private BigDecimal amount;
    private LocalDate term;
    private String email;
    private String identityDocument;
    private Long stateId;
    private Long loanTypeId;


}
