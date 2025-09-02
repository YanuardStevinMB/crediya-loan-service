package com.crediya.loan.r2dbc.aplication;

import com.crediya.loan.model.application.Application;
import com.crediya.loan.model.application.gateways.ApplicationRepository;
import com.crediya.loan.r2dbc.entity.ApplicationEntity;
import com.crediya.loan.r2dbc.helper.ReactiveAdapterOperations;
import com.crediya.loan.r2dbc.mapper.AplicationEntityMapper;
import lombok.extern.slf4j.Slf4j;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

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

    public AplicationReactiveRepositoryAdapter(AplicationReactiveRepository repository,
                                               AplicationEntityMapper aplicationEntityMapper,
                                               ObjectMapper mapper) {
        super(repository, mapper, entity -> mapper.map(entity, Application.class));
        this.aplicationEntityMapper = aplicationEntityMapper;
        this.repository = repository;
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
    public Flux<Application> findPendingApplications(int page, int size, String filter) {
        return repository.findByStateId(1L) // estado pendiente
                .filter(entity -> filter == null || filter.isBlank()
                        || entity.getEmail().toLowerCase().contains(filter.toLowerCase()))
                .skip((long) page * size)  // emulación de paginación
                .take(size)
                .map(aplicationEntityMapper::toDomain);
    }
}
