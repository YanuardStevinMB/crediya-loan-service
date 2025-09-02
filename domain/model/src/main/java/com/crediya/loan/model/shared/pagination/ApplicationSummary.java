package com.crediya.loan.model.shared.pagination;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
@Getter
@Setter
@AllArgsConstructor
public final class ApplicationSummary {
    private final Long id;
    private final BigDecimal amount;
    private final LocalDate term;
    private final String email;

    // Relaciones: se exponen como valores provenientes de JOIN
    private final Long stateId;
    private final String stateCode;   // s.codigo (ej. PENDIENTE_REVISION)
    private final String stateName;   // s.nombre (ej. "Pendiente de revisión")

    private final Long loanTypeId;
    private final String loanTypeName; // lt.nombre
    private final BigDecimal interestRate; // lt.tasa_interes

    private final String fullName;    // c.nombre (si aplica)
    private final BigDecimal baseSalary; // c.salario_base (si aplica)
    private final BigDecimal totalMonthlyDebtApproved;



}