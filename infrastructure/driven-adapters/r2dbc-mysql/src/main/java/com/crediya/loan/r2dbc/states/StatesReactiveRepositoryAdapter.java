package com.crediya.loan.r2dbc.states;

import com.crediya.loan.model.loantype.LoanType;
import com.crediya.loan.model.states.States;
import com.crediya.loan.model.states.gateways.StatesRepository;
import com.crediya.loan.r2dbc.entity.StatesEntity;
import com.crediya.loan.r2dbc.helper.ReactiveAdapterOperations;
import lombok.extern.slf4j.Slf4j;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Slf4j
@Repository
public class  StatesReactiveRepositoryAdapter extends ReactiveAdapterOperations<
        States,
        StatesEntity,
        Long,
        StatesReactiveRepository
        > implements StatesRepository {

    private final StatesReactiveRepository repository;

    public StatesReactiveRepositoryAdapter(StatesReactiveRepository repository,
                                             ObjectMapper mapper) {
        // mapper: de Entity → Domain
        super(repository, mapper, entity -> mapper.map(entity, States.class));
        this.repository = repository;
    }



    @Override
    public Mono<States> findByCode(String code) {
        return  repository.findByCode(code)
                .map(entity -> mapper.map(entity, States.class));
    }
}
