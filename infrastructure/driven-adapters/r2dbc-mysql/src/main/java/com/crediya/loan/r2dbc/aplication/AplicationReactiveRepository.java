package com.crediya.loan.r2dbc.aplication;

import com.crediya.loan.model.application.ApplicationPagined;
import com.crediya.loan.r2dbc.entity.ApplicationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.awt.print.Pageable;

public interface AplicationReactiveRepository  extends ReactiveCrudRepository<ApplicationEntity, Long>, ReactiveQueryByExampleExecutor<ApplicationEntity> {
    Flux<ApplicationEntity> findByStateId(Long stateId);


    @Query("""
    SELECT 
        s.id_solicitud         AS id,
        s.monto                AS amount,
        s.plazo                AS term,
        s.email                AS email,
        s.documento_identidad  AS identityDocument,
        e.nombre               AS state,
        tp.nombre              AS loanType,
        s.id_estado            AS stateId,
        s.id_tipo_prestamo     AS loanTypeId
       
    FROM crediya_loan.solicitud s
    INNER JOIN crediya_loan.tipo_prestamo tp 
        ON s.id_tipo_prestamo = tp.id_tipo_prestamo
    INNER JOIN crediya_loan.estados e 
        ON s.id_estado = e.id_estado
    WHERE 
        (:estado IS NULL OR e.nombre = :estado)
        AND (:documento IS NULL OR s.documento_identidad = :documento)
        AND (:email IS NULL OR s.email LIKE CONCAT('%', :email, '%'))
    ORDER BY s.id_solicitud DESC
    LIMIT :pageSize OFFSET :offset
    """)
    Flux<ApplicationPagined> dataApplicationPagined(
            @Param("estado") String estado,
            @Param("documento") String documento,
            @Param("email") String email,
            @Param("pageSize") int pageSize,
            @Param("offset") int offset
    );

    @Query("""
    SELECT COUNT(*) 
    FROM crediya_loan.solicitud s
    INNER JOIN crediya_loan.tipo_prestamo tp 
        ON s.id_tipo_prestamo = tp.id_tipo_prestamo
    INNER JOIN crediya_loan.estados e 
        ON s.id_estado = e.id_estado
    WHERE 
        (:estado IS NULL OR e.nombre = :estado)
        AND (:documento IS NULL OR s.documento_identidad = :documento)
        AND (:email IS NULL OR s.email LIKE CONCAT('%', :email, '%'))
    """)
    Mono<Long> countApplications(
            @Param("estado") String estado,
            @Param("documento") String documento,
            @Param("email") String email
    );




}
