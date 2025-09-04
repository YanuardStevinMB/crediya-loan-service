package com.crediya.loan.usecase.generaterequest.generaterequest;
import com.crediya.loan.usecase.generaterequest.gateway.UserManagementGateway;
import com.crediya.loan.usecase.shared.Messages;
import com.crediya.loan.usecase.shared.ValidationException;
import reactor.core.publisher.Mono;

public class VerifyUserUseCase {

    private final UserManagementGateway gateway;

    public VerifyUserUseCase(UserManagementGateway gateway) {
        this.gateway = gateway;
    }

    public Mono<Boolean> execute(String documentNumber, String email) {
        return gateway.verify(documentNumber, email)
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new ValidationException("User",Messages.USER_INVALID));
                    }
                    return Mono.just(true);
                });
    }
}
