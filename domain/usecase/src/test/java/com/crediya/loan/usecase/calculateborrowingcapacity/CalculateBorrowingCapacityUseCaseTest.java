package com.crediya.loan.usecase.calculateborrowingcapacity;

import com.crediya.loan.model.application.Application;
import com.crediya.loan.model.application.ApplicationApproved;
import com.crediya.loan.model.application.gateways.ApplicationRepository;
import com.crediya.loan.model.application.gateways.BorrowingCapacitySender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CalculateBorrowingCapacityUseCaseTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private BorrowingCapacitySender borrowingCapacitySender;

    private CalculateBorrowingCapacityUseCase useCase;

    @BeforeEach
    void setup() {
        useCase = new CalculateBorrowingCapacityUseCase(applicationRepository, borrowingCapacitySender);
    }

    private Application buildApp(Long id, String doc) {
        return Application.builder()
                .id(id)
                .amount(BigDecimal.valueOf(5000))
                .term(LocalDate.now().plusMonths(12))
                .email("user@mail.com")
                .identityDocument(doc)
                .loanTypeId(1L)
                .stateId(10L)
                .build();
    }

    private ApplicationApproved buildApproved(BigDecimal amount) {
        return ApplicationApproved.builder()
                .amount(amount)
                .interestRate(BigDecimal.valueOf(5))
                .termMonths(12L)
                .build();
    }


    @Test
    void execute_shouldReturnApp_whenNoApprovedLoans() {
        var app = buildApp(1L, "DOC1");

        when(applicationRepository.approvedApplications("DOC1")).thenReturn(Flux.empty());
        // Agregar el mock para borrowingCapacitySender incluso cuando no hay préstamos aprobados
        when(borrowingCapacitySender.sendBorrowingCapacity(eq(app), eq(BigDecimal.valueOf(2000)), anyList(), anyList()))
                .thenReturn(Mono.just("msg-empty-loans"));

        StepVerifier.create(useCase.execute(app, BigDecimal.valueOf(2000)))
                .expectNext(app)
                .verifyComplete();

        verify(applicationRepository).approvedApplications("DOC1");
        // Cambiar verifyNoInteractions por verify ya que sí se llama al sender
        verify(borrowingCapacitySender).sendBorrowingCapacity(eq(app), eq(BigDecimal.valueOf(2000)), anyList(), anyList());
    }


    @Test
    void execute_shouldSendMessage_whenApprovedLoansExist() {
        var app = buildApp(2L, "DOC2");
        var approved1 = buildApproved(BigDecimal.valueOf(1000));
        var approved2 = buildApproved(BigDecimal.valueOf(2000));

        when(applicationRepository.approvedApplications("DOC2")).thenReturn(Flux.just(approved1, approved2));
        when(borrowingCapacitySender.sendBorrowingCapacity(eq(app), eq(BigDecimal.valueOf(3000)), anyList(), anyList()))
                .thenReturn(Mono.just("msg-123"));

        StepVerifier.create(useCase.execute(app, BigDecimal.valueOf(3000)))
                .expectNext(app)
                .verifyComplete();

        verify(applicationRepository).approvedApplications("DOC2");
        verify(borrowingCapacitySender).sendBorrowingCapacity(eq(app), eq(BigDecimal.valueOf(3000)), anyList(), anyList());
    }

    @Test
    void execute_shouldPropagateError_whenSenderFails() {
        var app = buildApp(3L, "DOC3");
        var approved = buildApproved(BigDecimal.valueOf(1500));

        when(applicationRepository.approvedApplications("DOC3")).thenReturn(Flux.just(approved));
        when(borrowingCapacitySender.sendBorrowingCapacity(any(), any(), anyList(), anyList()))
                .thenReturn(Mono.error(new RuntimeException("SQS error")));

        StepVerifier.create(useCase.execute(app, BigDecimal.valueOf(4000)))
                .expectErrorMatches(ex -> ex instanceof RuntimeException && ex.getMessage().equals("SQS error"))
                .verify();

        verify(applicationRepository).approvedApplications("DOC3");
        verify(borrowingCapacitySender).sendBorrowingCapacity(eq(app), eq(BigDecimal.valueOf(4000)), anyList(), anyList());
    }
}
