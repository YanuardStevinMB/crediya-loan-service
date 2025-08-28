package com.crediya.loan.r2dbc.entity;

import org.springframework.data.annotation.Id;
import lombok.*;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

@Table("solicitud")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder(toBuilder = true)
public class ApplicationEntity {
    @Id
    @Column("id_solicitud")
    private Long id;

    @Column("monto")

    private BigDecimal amount;
    @Column("plazo")
    private LocalDate term;
    @Column("email")
    private String email;
    @Column("documento_identidad")
    private String identityDocument;
    @Column("id_estado")
    private Long stateId;
    @Column("id_tipo_prestamo")
    private Long loanTypeId;

}
