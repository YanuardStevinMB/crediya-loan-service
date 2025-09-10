package com.crediya.loan.model.application;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class ApplicationDataCompleted {
    private Long id;
    private BigDecimal amount;
    private String email;
    private String identityDocument;
    private String state;
    private String loan;
    private Long stateId;
    private Long loanTypeId;
}
