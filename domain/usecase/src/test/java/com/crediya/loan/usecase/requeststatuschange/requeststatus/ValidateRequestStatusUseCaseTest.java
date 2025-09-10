package com.crediya.loan.usecase.requeststatuschange.requeststatus;

import com.crediya.loan.model.application.Application;
import com.crediya.loan.model.application.RequestStatusUpdate;
import com.crediya.loan.model.application.gateways.ApplicationRepository;
import com.crediya.loan.model.states.States;
import com.crediya.loan.model.states.gateways.StatesRepository;
import com.crediya.loan.usecase.shared.ConfigurationException;
import com.crediya.loan.usecase.shared.DataValidation;
import com.crediya.loan.usecase.shared.Messages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValidateRequestStatusUseCaseTest {

    @Mock
    ApplicationRepository applicationRepository;

    @Mock
    StatesRepository statesRepository;

    ValidateRequestStatusUseCase useCase;

    @BeforeEach
    void setup() {
        useCase = new ValidateRequestStatusUseCase(applicationRepository, statesRepository);
    }

    @Test
    void shouldError_whenRequestIsNull() {
        StepVerifier.create(useCase.execute(null))
                .expectErrorMatches(err -> err instanceof ConfigurationException &&
                        err.getMessage().equals(Messages.INVALID_IDENTIFICADOR_APPLICATION))
                .verify();
    }

    @Test
    void shouldError_whenRequestIdIsNull() {
        var request = new RequestStatusUpdate(null, 10L);

        StepVerifier.create(useCase.execute(request))
                .expectErrorMatches(err -> err instanceof ConfigurationException &&
                        err.getMessage().equals(Messages.INVALID_IDENTIFICADOR_APPLICATION))
                .verify();
    }

    @Test
    void shouldError_whenStateIdIsNull() {
        var request = new RequestStatusUpdate(1L, null);

        StepVerifier.create(useCase.execute(request))
                .expectErrorMatches(err -> err instanceof ConfigurationException &&
                        err.getMessage().equals(Messages.INVALID_STATE))
                .verify();
    }

    @Test
    void shouldError_whenApplicationNotFound() {
        var request = new RequestStatusUpdate(1L, 10L);

        when(applicationRepository.findById(1L)).thenReturn(Mono.empty());
        when(statesRepository.findById(10L))
                .thenReturn(Mono.just(States.builder().id(10L).code(DataValidation.APROB_STATUS_CODE).build()));

        StepVerifier.create(useCase.execute(request))
                .expectErrorMatches(err -> err instanceof ConfigurationException &&
                        err.getMessage().equals(Messages.INVALID_IDENTIFICADOR_APPLICATION))
                .verify();
    }

    @Test
    void shouldError_whenStateNotFound() {
        var request = new RequestStatusUpdate(1L, 10L);

        var dummyApp = Application.builder()
                .id(1L)
                .email("test@mail.com")
                .identityDocument("123")
                .loanTypeId(1L)
                .stateId(5L)
                .build();

        when(applicationRepository.findById(1L)).thenReturn(Mono.just(dummyApp));
        when(statesRepository.findById(10L)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(request))
                .expectErrorMatches(err -> err instanceof ConfigurationException &&
                        err.getMessage().equals(Messages.INVALID_STATE))
                .verify();
    }

    @Test
    void shouldError_whenStateCodeNotAllowed() {
        var request = new RequestStatusUpdate(1L, 10L);

        var dummyApp = Application.builder().id(1L).build();

        when(applicationRepository.findById(1L)).thenReturn(Mono.just(dummyApp));
        when(statesRepository.findById(10L))
                .thenReturn(Mono.just(States.builder().id(10L).code("PEN").build())); // no válido

        StepVerifier.create(useCase.execute(request))
                .expectErrorMatches(err -> err instanceof ConfigurationException &&
                        err.getMessage().equals(Messages.INVALID_STATE))
                .verify();
    }

    @Test
    void shouldReturnTrue_whenValidFlow_APROBADO() {
        var request = new RequestStatusUpdate(1L, 10L);

        var dummyApp = Application.builder().id(1L).build();
        var validState = States.builder().id(10L).code(DataValidation.APROB_STATUS_CODE).build();

        when(applicationRepository.findById(1L)).thenReturn(Mono.just(dummyApp));
        when(statesRepository.findById(10L)).thenReturn(Mono.just(validState));

        StepVerifier.create(useCase.execute(request))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void shouldReturnTrue_whenValidFlow_RECHAZADO() {
        var request = new RequestStatusUpdate(2L, 20L);

        var dummyApp = Application.builder().id(2L).build();
        var validState = States.builder().id(20L).code(DataValidation.RECH_STATUS_CODE).build();

        when(applicationRepository.findById(2L)).thenReturn(Mono.just(dummyApp));
        when(statesRepository.findById(20L)).thenReturn(Mono.just(validState));

        StepVerifier.create(useCase.execute(request))
                .expectNext(true)
                .verifyComplete();
    }
}
