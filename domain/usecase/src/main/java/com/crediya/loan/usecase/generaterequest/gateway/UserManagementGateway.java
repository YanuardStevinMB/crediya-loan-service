package com.crediya.loan.usecase.generaterequest.gateway;

import com.crediya.loan.model.user.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface UserManagementGateway {
    Flux<User> loadUsers();
    Mono<BigDecimal> verify(String documentNumber, String email);

}
