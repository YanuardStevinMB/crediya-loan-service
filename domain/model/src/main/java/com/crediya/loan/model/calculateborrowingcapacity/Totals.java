package com.crediya.loan.model.calculateborrowingcapacity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public  class Totals {
    private BigDecimal totalIntereses;
    private BigDecimal totalPagado;
}