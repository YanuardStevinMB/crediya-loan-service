package com.crediya.loan.r2dbc.aplication;

import com.crediya.loan.model.application.Application;
import com.crediya.loan.model.application.ApplicationPagined;
import com.crediya.loan.model.application.PendingApplicationsCriteria;
import com.crediya.loan.model.application.gateways.ApplicationRepository;
import com.crediya.loan.model.shared.Page;
import com.crediya.loan.r2dbc.entity.ApplicationEntity;
import com.crediya.loan.r2dbc.helper.ReactiveAdapterOperations;
import com.crediya.loan.r2dbc.mapper.AplicationEntityMapper;
import lombok.extern.slf4j.Slf4j;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

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
    private final DatabaseClient db;

    private  final Long PENDING_STATE_ID = 1L;


    public AplicationReactiveRepositoryAdapter(AplicationReactiveRepository repository,
                                               AplicationEntityMapper aplicationEntityMapper,
                                               ObjectMapper mapper,
                                               DatabaseClient db) {
        super(repository, mapper, entity -> mapper.map(entity, Application.class));
        this.aplicationEntityMapper = aplicationEntityMapper;
        this.repository = repository;
        this.db = db;
    }

    @Override
    public Mono<Application> save(Application application) {
        // 1) Mapear el dominio a entidad
        ApplicationEntity entity = aplicationEntityMapper.toEntity(application);

        // 2) Guardar en BD
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

        Mono<List<ApplicationPagined>> data = repository.dataApplicationPagined(
                        criteria.state(),
                        criteria.document(),
                        criteria.email(),
                        criteria.size(),
                        offset
                )
                .doOnNext(row -> log.debug("[findApplicationsPaginated] Fila obtenida: {}", row))
                .collectList()
                .doOnNext(list -> log.info("[findApplicationsPaginated] Se obtuvieron {} registros de la base de datos", list.size()));

        Mono<Long> total = repository.countApplications(
                        criteria.state(),
                        criteria.document(),
                        criteria.email()
                )
                .doOnNext(count -> log.info("[findApplicationsPaginated] Total de registros encontrados: {}", count))
                .map(val -> val != null ? val : 0L);

        return Mono.zip(data, total)
                .doOnNext(tuple -> log.info("[findApplicationsPaginated] Preparando Page con {} elementos y total {}",
                        tuple.getT1().size(), tuple.getT2()))
                .map(tuple -> Page.of(tuple.getT1(), criteria.page(), criteria.size(), tuple.getT2()));
    }




}
