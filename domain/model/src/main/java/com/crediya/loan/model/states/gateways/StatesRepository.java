package com.crediya.loan.model.states.gateways;

import com.crediya.loan.model.states.States;
import reactor.core.publisher.Mono;

public interface StatesRepository {

        Mono<States> findByCode(String code);
}
