package com.crediya.loan.usecase.generaterequest.gateway;

import reactor.core.publisher.Mono;

    public interface DocumentVerificationGateway {
    Mono<Boolean> verify(String documentNumber, String email);

}
