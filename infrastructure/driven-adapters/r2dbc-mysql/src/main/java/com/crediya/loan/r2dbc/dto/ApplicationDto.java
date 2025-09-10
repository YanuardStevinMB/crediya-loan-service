package com.crediya.loan.r2dbc.dto;

import lombok.*;
import org.springframework.data.relational.core.mapping.Column;

import java.math.BigDecimal;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ApplicationDto {

    @Column("id_solicitud")
    private Long id;

    @Column("monto")
    private BigDecimal amount;

    @Column("email")
    private String email;

    @Column("documento_identidad")
    private String identityDocument;

    @Column("state")
    private String state;

    @Column("loan")
    private String loan;

    @Column("id_estado")
    private Long stateId;

    @Column("id_tipo_prestamo")
    private Long loanTypeId;
}
