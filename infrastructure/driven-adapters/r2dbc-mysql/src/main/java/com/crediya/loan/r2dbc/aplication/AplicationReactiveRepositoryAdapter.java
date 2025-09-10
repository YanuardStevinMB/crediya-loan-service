package com.crediya.loan.r2dbc.aplication;

import com.crediya.loan.model.application.*;
import com.crediya.loan.model.application.gateways.ApplicationRepository;
import com.crediya.loan.model.shared.Page;
import com.crediya.loan.r2dbc.dto.ApplicationDto;
import com.crediya.loan.r2dbc.entity.ApplicationEntity;
import com.crediya.loan.r2dbc.helper.ReactiveAdapterOperations;
import com.crediya.loan.r2dbc.mapper.AplicationEntityMapper;
import com.crediya.loan.r2dbc.mapper.ApplicationApprovedMapper;
import com.crediya.loan.r2dbc.mapper.ApplicationDataCompletedMapper;
import lombok.extern.slf4j.Slf4j;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Slf4j
@Repository
public class AplicationReactiveRepositoryAdapter extends ReactiveAdapterOperations<
        Application,
        ApplicationEntity,
        Long,
        AplicationReactiveRepository
        > implements ApplicationRepository {

    private final AplicationEntityMapper aplicationEntityMapper;
    private final AplicationReactiveRepository repository;
    private  final ApplicationDataCompletedMapper applicationDataCompletedMapper;
    private  final ApplicationApprovedMapper applicationApprovedMapper;

    @Autowired(required = false)
    private com.fasterxml.jackson.databind.ObjectMapper json;

    public AplicationReactiveRepositoryAdapter(AplicationReactiveRepository repository,
                                               AplicationEntityMapper aplicationEntityMapper,
                                               ObjectMapper mapper,
                                               ApplicationDataCompletedMapper applicationDataCompletedMapper,
                                               ApplicationApprovedMapper applicationApprovedMapper
                                               ) {
        super(repository, mapper, entity -> mapper.map(entity, Application.class));
        this.aplicationEntityMapper = aplicationEntityMapper;
        this.repository = repository;
        this.applicationDataCompletedMapper = applicationDataCompletedMapper;
        this.applicationApprovedMapper = applicationApprovedMapper;

    }

    @Override
    public Mono<Application> save(Application application) {
        ApplicationEntity entity = aplicationEntityMapper.toEntity(application);

        return repository.save(entity)
                // 3) Mapear de vuelta a dominio
                .map(aplicationEntityMapper::toDomain)
                .doOnSuccess(saved -> log.info("[application.save] id={} email={} stateId={}",
                        saved.getId(), saved.getEmail(), saved.getStateId()))
                .doOnError(err -> log.warn("[application.save] failed: {}", err.toString()));
    }

    @Override
    public Mono<Page<ApplicationPagined>> findApplicationsPaginated(PendingApplicationsCriteria criteria) {
        int offset = (criteria.page() - 1) * criteria.size();

        log.info("[findApplicationsPaginated] Ejecutando búsqueda con criterios: estado={}, documento={}, email={}, page={}, size={}, offset={}",
                criteria.state(), criteria.document(), criteria.email(), criteria.page(), criteria.size(), offset);

        Mono<List<ApplicationPagined>> data = repository.dataApplicationPagined(criteria.state(),criteria.document(),criteria.email(),criteria.size(),offset
                )
                .doOnNext(row -> log.debug("[findApplicationsPaginated] Fila obtenida: {}", row))
                .collectList()
                .doOnNext(list -> {
                    log.info("[findApplicationsPaginated] Se obtuvieron {} registros de la base de datos", list.size());
                    list.forEach(app -> log.info("➡ Registro completo: {}", app));
                });

        Mono<Long> total = repository.countApplications(criteria.state(), criteria.document(),criteria.email())
                .doOnNext(count -> log.info("[findApplicationsPaginated] Total de registros encontrados: {}", count))
                .map(val -> val != null ? val : 0L);

        return Mono.zip(data, total)
                .doOnNext(tuple -> log.info("[findApplicationsPaginated] Preparando Page con {} elementos y total {}",
                        tuple.getT1().size(), tuple.getT2()))
                .map(tuple -> Page.of(tuple.getT1(), criteria.page(), criteria.size(), tuple.getT2()));
    }





    @Override
    public Mono<ApplicationDataCompleted> requestStatusChange(RequestStatusUpdate requestStatusUpdate) {
        final Long id = requestStatusUpdate.getId();
        final Long newStateId = requestStatusUpdate.getStateId();

        return repository.requestStatusChange(id, newStateId)
                .doOnNext(rows -> log.info("[statusChange] UPDATE ejecutado. rowsUpdated={}", rows))
                // Trae la solicitud ya actualizada
                .then(repository.dataApplication(id)
                        .doOnSubscribe(s -> log.info("[statusChange] Buscando solicitud id={}...", id)))
                // Si no existe, lanza error (o usa Mono.empty() si prefieres 204/404 arriba)
                .switchIfEmpty(Mono.error(new IllegalStateException(
                        "No existe la solicitud con id=" + id)))
                // Valida que el estado en BD coincida con el solicitado (opcional pero útil)
                .map(dto -> {
                    if (dto.getStateId() == null || !dto.getStateId().equals(newStateId)) {
                        log.warn("[statusChange] Post-UPDATE: estado en BD ({}) != esperado ({}). id={}",
                                dto.getStateId(), newStateId, id);
                        throw new IllegalStateException("Estado no coincide después del UPDATE");
                    }
                    return dto;
                })
                // MapStruct/manual: DTO -> Domain
                .map(applicationDataCompletedMapper::toDomain);
    }

    @Override
    public Flux<ApplicationApproved> approvedApplications(String identityDocument) {
        return repository.applicationApproved(identityDocument)
                .map(applicationApprovedMapper::toDomain);
    }



}
