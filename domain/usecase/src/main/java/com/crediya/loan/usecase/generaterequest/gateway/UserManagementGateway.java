package com.crediya.loan.usecase.generaterequest.gateway;

import com.crediya.loan.model.user.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserManagementGateway {
    Flux<User> loadUsers();
    Mono<Boolean> verify(String documentNumber, String email);

}
