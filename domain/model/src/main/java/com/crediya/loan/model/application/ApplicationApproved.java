package com.crediya.loan.model.application;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationApproved {
    private BigDecimal amount;
    private  BigDecimal interestRate;
    private  Long termMonths;

}
