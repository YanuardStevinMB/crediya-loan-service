package com.crediya.loan.r2dbc.states;

import com.crediya.loan.r2dbc.entity.LoanTypeEntity;
import com.crediya.loan.r2dbc.entity.StatesEntity;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import javax.swing.plaf.nimbus.State;

public interface StatesReactiveRepository extends ReactiveCrudRepository<StatesEntity, Long>, ReactiveQueryByExampleExecutor<StatesEntity> {

    Mono<StatesEntity> findByCode(String code);


}
