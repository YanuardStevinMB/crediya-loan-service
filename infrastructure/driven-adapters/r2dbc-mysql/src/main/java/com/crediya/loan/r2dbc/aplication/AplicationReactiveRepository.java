package com.crediya.loan.r2dbc.aplication;

import com.crediya.loan.model.application.ApplicationPagined;
import com.crediya.loan.r2dbc.entity.ApplicationEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AplicationReactiveRepository  extends ReactiveCrudRepository<ApplicationEntity, Long> ,ReactiveQueryByExampleExecutor<ApplicationEntity> {
    Flux<ApplicationEntity> findByStateId(Long stateId);


    @Query("""
    SELECT 
        solicitud.id_solicitud         ,
        solicitud.monto                ,
        solicitud.plazo                ,
        solicitud.email                ,
        solicitud.documento_identidad  ,
        estados.nombre         AS state       ,
        tipo_prestamo.nombre        AS  loan  ,
        solicitud.id_estado          ,
        solicitud.id_tipo_prestamo    
       
    FROM solicitud 
    INNER JOIN tipo_prestamo 
        ON solicitud.id_tipo_prestamo = tipo_prestamo.id_tipo_prestamo
    INNER JOIN estados  
        ON estados.id_estado = solicitud.id_estado
    WHERE 
        (:estado IS NULL OR estados.nombre = :estado)
        AND (:documento IS NULL OR solicitud.documento_identidad = :documento)
        AND (:email IS NULL OR solicitud.email LIKE CONCAT('%', :email, '%'))
    ORDER BY solicitud.id_solicitud DESC
    
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
