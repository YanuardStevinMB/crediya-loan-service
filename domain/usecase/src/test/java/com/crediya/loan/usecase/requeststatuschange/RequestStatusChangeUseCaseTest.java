package com.crediya.loan.usecase.requeststatuschange;

import com.crediya.loan.model.application.ApplicationDataCompleted;
import com.crediya.loan.model.application.RequestStatusUpdate;
import com.crediya.loan.model.application.gateways.ApplicationRepository;
import com.crediya.loan.model.application.gateways.ApplicationSenderSqs;
import com.crediya.loan.usecase.requeststatuschange.requeststatus.ValidateRequestStatusUseCase;
import com.crediya.loan.usecase.shared.Messages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequestStatusChangeUseCaseTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private ValidateRequestStatusUseCase validateRequestStatusUseCase;

    @Mock
    private ApplicationSenderSqs sqsSender;

    private RequestStatusChangeUseCase useCase;

    @BeforeEach
    void setup() {
        useCase = new RequestStatusChangeUseCase(applicationRepository, validateRequestStatusUseCase, sqsSender);
    }

    private ApplicationDataCompleted buildEntity(Long id, Long stateId) {
        return ApplicationDataCompleted.builder()
                .id(id)
                .amount(BigDecimal.valueOf(1000))
                .email("user@mail.com")
                .identityDocument("123")
                .state("APROBADO")
                .loan("PERSONAL")
                .stateId(stateId)
                .loanTypeId(1L)
                .build();
    }

    @Test
    void execute_shouldReturnUpdatedMessage_whenValidFlow() {
        var update = new RequestStatusUpdate(1L, 10L);
        var entity = buildEntity(1L, 10L);

        when(validateRequestStatusUseCase.execute(update)).thenReturn(Mono.empty());
        when(applicationRepository.requestStatusChange(update)).thenReturn(Mono.just(entity));
        when(sqsSender.sendStatusChange(entity)).thenReturn(Mono.just("msg-123"));

        StepVerifier.create(useCase.execute(update))
                .expectNext(Messages.APPLICATION_UPDATED)
                .verifyComplete();

        verify(validateRequestStatusUseCase).execute(update);
        verify(applicationRepository).requestStatusChange(update);
        verify(sqsSender).sendStatusChange(entity);
    }

    @Test
    void execute_shouldReturnUpdatedMessage_whenSqsFails() {
        var update = new RequestStatusUpdate(2L, 30L);
        var entity = buildEntity(2L, 30L);

        when(validateRequestStatusUseCase.execute(update)).thenReturn(Mono.empty());
        when(applicationRepository.requestStatusChange(update)).thenReturn(Mono.just(entity));
        when(sqsSender.sendStatusChange(entity)).thenReturn(Mono.error(new RuntimeException("SQS error")));

        StepVerifier.create(useCase.execute(update))
                .expectNext(Messages.APPLICATION_UPDATED)
                .verifyComplete();

        verify(sqsSender).sendStatusChange(entity);
    }
//
//    @Test
//    void execute_shouldPropagateError_whenValidationFails() {
//        var update = new RequestStatusUpdate(3L, 40L);
//
//        lenient().when(validateRequestStatusUseCase.execute(update))
//                .thenReturn(Mono.error(new RuntimeException("invalid")));
//
//        StepVerifier.create(useCase.execute(update))
//                .expectErrorMatches(err -> err instanceof RuntimeException &&
//                        err.getMessage().equals("invalid"))
//                .verify();
//
//        verify(validateRequestStatusUseCase).execute(update);
//        verifyNoInteractions(applicationRepository, sqsSender);
//    }

}
