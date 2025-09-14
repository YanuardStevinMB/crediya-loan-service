package com.crediya.loan.sqs.listener.dto;

import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TotalsDto {
    private BigDecimal totalIntereses;
    private BigDecimal totalPagado;
}
