package com.crediya.loan.r2dbc.entity;

import org.springframework.data.annotation.Id;
import lombok.*;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Table("tipo_prestamo")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder(toBuilder = true)
public class LoanTypeEntity {

    @Id
    @Column("id_tipo_prestamo")
    private Long id;

    @Column("nombre")
    private String name;

    @Column("monto_minimo")
    private BigDecimal amountMin;

    @Column("monto_maximo")
    private BigDecimal amountMax;

    @Column("tasa_interes")
    private BigDecimal interestRate;

    @Column("validacion_automatica")
    private Boolean automaticValidation;
}
