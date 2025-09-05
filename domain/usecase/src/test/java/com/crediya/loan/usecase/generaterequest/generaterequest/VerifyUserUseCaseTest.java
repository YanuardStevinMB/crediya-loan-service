package com.crediya.loan.usecase.generaterequest.generaterequest;

import com.crediya.loan.usecase.generaterequest.gateway.UserManagementGateway;
import com.crediya.loan.usecase.shared.Messages;
import com.crediya.loan.usecase.shared.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VerifyUserUseCaseTest {

    @Mock
    private UserManagementGateway gateway;

    private VerifyUserUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new VerifyUserUseCase(gateway);
    }

    @Test
    void execute_shouldReturnTrue_whenGatewayReturnsTrue() {
        when(gateway.verify("123", "a@b.c")).thenReturn(Mono.just(true));

        StepVerifier.create(useCase.execute("123", "a@b.c"))
                .expectNext(true)
                .verifyComplete();

        verify(gateway).verify("123", "a@b.c");
        verifyNoMoreInteractions(gateway);
    }

    @Test
    void execute_shouldErrorWithValidationException_whenGatewayReturnsFalse() {
        when(gateway.verify("999", "x@y.z")).thenReturn(Mono.just(false));

        StepVerifier.create(useCase.execute("999", "x@y.z"))
                .expectErrorSatisfies(err -> {
                    assertTrue(err instanceof ValidationException);
                    var ve = (ValidationException) err;
                    assertEquals("User", ve.getField());
                    // Mensaje exacto proveniente de Messages.USER_INVALID
                    assertEquals(Messages.USER_INVALID, ve.getMessage());
                })
                .verify();

        verify(gateway).verify("999", "x@y.z");
        verifyNoMoreInteractions(gateway);
    }

    @Test
    void execute_shouldPropagateError_whenGatewayErrors() {
        var boom = new RuntimeException("gateway-down");
        when(gateway.verify("doc", "mail")).thenReturn(Mono.error(boom));

        StepVerifier.create(useCase.execute("doc", "mail"))
                .expectErrorMatches(e -> e == boom)
                .verify();

        verify(gateway).verify("doc", "mail");
        verifyNoMoreInteractions(gateway);
    }
}
