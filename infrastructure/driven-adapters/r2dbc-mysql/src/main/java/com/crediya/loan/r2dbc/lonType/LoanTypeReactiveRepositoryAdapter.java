package com.crediya.loan.r2dbc.loantype; // 👈 corrige el paquete

import com.crediya.loan.model.loantype.LoanType;
import com.crediya.loan.model.loantype.gateways.LoanTypeRepository;
import com.crediya.loan.r2dbc.entity.LoanTypeEntity;
import com.crediya.loan.r2dbc.helper.ReactiveAdapterOperations;
import com.crediya.loan.r2dbc.lonType.LoanTypeReactiveRepository;
import lombok.extern.slf4j.Slf4j;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Slf4j
@Repository
public class LoanTypeReactiveRepositoryAdapter extends ReactiveAdapterOperations<
        LoanType,
        LoanTypeEntity,
        Long,
        LoanTypeReactiveRepository
        > implements LoanTypeRepository {

    private final LoanTypeReactiveRepository repository;
    private final ObjectMapper mapper; // 👈 guardamos el mapper para usarlo aquí

    public LoanTypeReactiveRepositoryAdapter(LoanTypeReactiveRepository repository,
                                             ObjectMapper mapper) {
        // mapper: de Entity → Domain
        super(repository, mapper, entity -> mapper.map(entity, LoanType.class));
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Mono<LoanType> findById(Long id) {
        return repository.findById(id)                   // Mono<LoanTypeEntity>
                .doOnSubscribe(sub -> log.debug("[loanType.findById] Suscrito para id={}", id))
                .switchIfEmpty(Mono.defer(() -> {        // 👈 mantiene el tipo, solo loggea
                    log.warn("[loanType.findById] No se encontró id={}", id);
                    return Mono.empty();
                }))
                .map(entity -> mapper.map(entity, LoanType.class)) // 👈 mapeo a dominio
                .doOnNext(lt -> log.debug("[loanType.findById] Se encontró: id={} nombre={}", id, lt.getName()))
                .doOnError(err -> log.error("[loanType.findById] Error al buscar id={}: {}", id, err.toString()));
    }
}
