package com.crediya.loan.r2dbc.entity;

import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("estados")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder(toBuilder = true)
public class StatesEntity {
    @Id
    @Column("id_estado")
    private Long id;
    @Column("nombre")
    private String name;
    @Column("descripcion")
    private String description;
    @Column("codigo")
    private String code;
}
