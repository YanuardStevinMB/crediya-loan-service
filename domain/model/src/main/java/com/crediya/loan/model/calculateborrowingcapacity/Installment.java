package com.crediya.loan.model.calculateborrowingcapacity;

import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Installment {
    private Integer n;
    private BigDecimal cuota;
    private BigDecimal interes;
    private BigDecimal abonoCapital;
    private BigDecimal saldo;
}
