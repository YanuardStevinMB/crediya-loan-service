package com.crediya.loan.api.dto;

import com.crediya.loan.model.shared.pagination.ApplicationSummary;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDate;

public class AdminApplicationItemDto {
    @JsonProperty("monto") public BigDecimal amount;
    @JsonProperty("plazo") public LocalDate term;
    @JsonProperty("email") public String email;
    @JsonProperty("nombre") public String name;
    @JsonProperty("tipo_prestamo") public String loanType;
    @JsonProperty("tasa_interes") public BigDecimal interestRate;
    @JsonProperty("estado_solicitud") public String state;
    @JsonProperty("salario_base") public BigDecimal baseSalary;
    @JsonProperty("deuda_total_mensual_solicitudes_aprobadas") public BigDecimal totalMonthlyDebtApproved;

    public static AdminApplicationItemDto from(ApplicationSummary s) {
        var dto = new AdminApplicationItemDto();
        dto.amount = s.getAmount();
        dto.term = s.getTerm();
        dto.email = s.getEmail();
        dto.name = s.getFullName();
        dto.loanType = s.getLoanTypeName();
        dto.interestRate = s.getInterestRate();
        dto.state = s.getStateName(); // viene de la relación "estados"
        dto.baseSalary = s.getBaseSalary();
        dto.totalMonthlyDebtApproved = s.getTotalMonthlyDebtApproved();
        return dto;
    }
}
