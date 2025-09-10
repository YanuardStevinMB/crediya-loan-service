package com.crediya.loan.r2dbc.dto;

import lombok.*;
import org.springframework.data.relational.core.mapping.Column;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationApprovedDto {
    @Column("monto")
    private BigDecimal amount;
    @Column("tasa_interes")
    private  BigDecimal interestRate;
    @Column("plazo")
    private  Long termMonths;
}
