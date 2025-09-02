package com.crediya.loan.r2dbc.aplication;

import com.crediya.loan.r2dbc.entity.ApplicationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.awt.print.Pageable;

public interface AplicationReactiveRepository  extends ReactiveCrudRepository<ApplicationEntity, Long>, ReactiveQueryByExampleExecutor<ApplicationEntity> {
    Flux<ApplicationEntity> findByStateId(Long stateId);
}
