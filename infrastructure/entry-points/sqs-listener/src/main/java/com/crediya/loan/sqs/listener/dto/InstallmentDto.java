package com.crediya.loan.sqs.listener.dto;

import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InstallmentDto {
    private Integer n;
    private BigDecimal cuota;
    private BigDecimal interes;
    private BigDecimal abonoCapital;
    private BigDecimal saldo;
}
