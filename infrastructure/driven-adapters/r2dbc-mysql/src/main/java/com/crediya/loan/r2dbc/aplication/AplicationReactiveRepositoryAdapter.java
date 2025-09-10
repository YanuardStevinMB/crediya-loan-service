package com.crediya.loan.r2dbc.aplication;

import com.crediya.loan.model.application.*;
import com.crediya.loan.model.application.gateways.ApplicationRepository;
import com.crediya.loan.model.shared.Page;
import com.crediya.loan.r2dbc.dto.ApplicationDto;
import com.crediya.loan.r2dbc.entity.ApplicationEntity;
import com.crediya.loan.r2dbc.helper.ReactiveAdapterOperations;
import com.crediya.loan.r2dbc.mapper.AplicationEntityMapper;
import com.crediya.loan.r2dbc.mapper.ApplicationDataCompletedMapper;
import com.crediya.loan.sqs.sender.SQSSender;
import lombok.extern.slf4j.Slf4j;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

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

    @Lazy
    @Autowired
    private SQSSender sqsSender;

    @Autowired(required = false)
    private com.fasterxml.jackson.databind.ObjectMapper json;

    public AplicationReactiveRepositoryAdapter(AplicationReactiveRepository repository,
                                               AplicationEntityMapper aplicationEntityMapper,
                                               ObjectMapper mapper,
                                               ApplicationDataCompletedMapper applicationDataCompletedMapper) {
        super(repository, mapper, entity -> mapper.map(entity, Application.class));
        this.aplicationEntityMapper = aplicationEntityMapper;
        this.repository = repository;
        this.applicationDataCompletedMapper = applicationDataCompletedMapper;

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


//    @Override
//    public Mono<ApplicationDataCompleted> requestStatusChange(RequestStatusUpdate requestStatusUpdate) {
//        final Long id = requestStatusUpdate.getId();
//        final Long newStateId = requestStatusUpdate.getStateId();
//
//        final com.fasterxml.jackson.databind.ObjectMapper mapper = resolveMapper();
//
//        return repository.requestStatusChange(id, newStateId)
//                .doOnNext(rows -> log.info("[statusChange] UPDATE ejecutado. rowsUpdated={}", rows))
//                .then(repository.dataApplication(id)
//                        .doOnSubscribe(s -> log.info("[statusChange] Buscando solicitud id={}...", id)))
//                .flatMap(entity ->    processEntity(entity, id, newStateId, mapper))
//                .switchIfEmpty(Mono.fromCallable(() -> {
//                    log.warn("[statusChange] No existe la solicitud con id={}", id);
//                    return false;
//                }));
//    }


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



    // ===== Helpers  =====
    private com.fasterxml.jackson.databind.ObjectMapper resolveMapper() {
        return (this.json != null) ? this.json : new com.fasterxml.jackson.databind.ObjectMapper();
    }

    private Mono<Boolean> processEntity(
            ApplicationDto entity,
            Long id,
            Long newStateId,
            com.fasterxml.jackson.databind.ObjectMapper mapper
    ) {
        log.info("[statusChange] Solicitud cargada id={}, stateId={}, email={}, doc={}",
                entity.getId(), entity.getStateId(), entity.getEmail(), entity.getIdentityDocument());

        Long dbState = entity.getStateId();
        if (dbState == null || !dbState.equals(newStateId)) {
            log.warn("[statusChange] Post-UPDATE: estado en BD ({}) != esperado ({}). id={}",
                    dbState, newStateId, id);
            return Mono.just(false);
        }

        // Construcción de campos para el payload
        final String requestIdStr = String.format("SOL-%d-%06d",
                java.time.Year.now().getValue(),
                entity.getId() == null ? 0 : entity.getId());

        // Status en español (usa el nombre que viene del JOIN si existe)
        final String status = (entity.getState() != null)
                ? entity.getState().toUpperCase(java.util.Locale.ROOT)
                : "APROBADA"; // fallback, si deseas otro valor por defecto cámbialo

        // Mensaje por defecto según estado
        final String customMessage =
                "APROBADO".equals(status)
                        ? "Su desembolso estará disponible en las próximas 24 horas."
                        : ("RECHAZADA".equals(status)
                        ? "Su solicitud fue rechazada. Puede volver a aplicar en 30 días."
                        : "El estado de su solicitud ha sido actualizado.");

        // Usamos LinkedHashMap para preservar el orden del JSON
        java.util.Map<String, Object> payload = new java.util.LinkedHashMap<>();
        payload.put("requestId", requestIdStr);
        payload.put("status", status);
        payload.put("emailClient", entity.getEmail());
        payload.put("identityDocument", entity.getIdentityDocument());
        if (entity.getAmount() != null) payload.put("loanAmount", entity.getAmount()); // BigDecimal -> número JSON
        if (entity.getLoan() != null)   payload.put("loanType", entity.getLoan());
        payload.put("customMessage", customMessage);

        return Mono.fromCallable(() -> mapper.writeValueAsString(payload))
                .flatMap(json -> sqsSender.send(json)
                        .doOnNext(mid -> log.info("[statusChange] ✅ Enviado a SQS id={}, stateId={}, messageId={}",
                                id, newStateId, mid))
                        .thenReturn(true))
                .onErrorResume(ex -> {
                    log.warn("[statusChange] Error serializando/enviando SQS (continuo true). id={}, causa={}",
                            id, ex.toString());
                    return Mono.just(true);
                });
    }




}
