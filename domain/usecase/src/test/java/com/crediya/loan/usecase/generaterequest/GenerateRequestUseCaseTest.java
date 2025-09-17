package com.crediya.loan.usecase.generaterequest;

import com.crediya.loan.model.application.Application;
import com.crediya.loan.model.application.gateways.ApplicationRepository;
import com.crediya.loan.model.loantype.LoanType;
import com.crediya.loan.model.loantype.gateways.LoanTypeRepository;
import com.crediya.loan.model.states.States;
import com.crediya.loan.model.states.gateways.StatesRepository;
import com.crediya.loan.usecase.calculateborrowingcapacity.CalculateBorrowingCapacityUseCase;
import com.crediya.loan.usecase.generaterequest.generaterequest.VerifyUserUseCase;
import com.crediya.loan.usecase.shared.ConfigurationException;
import com.crediya.loan.usecase.shared.DataValidation;
import com.crediya.loan.usecase.shared.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GenerateRequestUseCaseTest {

    @Mock
    ApplicationRepository applicationRepository;

    @Mock
    StatesRepository statesRepository;

    @Mock
    LoanTypeRepository loanTypeRepository;

    @Mock
    VerifyUserUseCase verifyUserUseCase;

    @Mock
    CalculateBorrowingCapacityUseCase calculateBorrowingCapacityUseCase;

    GenerateRequestUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GenerateRequestUseCase(
                applicationRepository,
                statesRepository,
                loanTypeRepository,
                verifyUserUseCase,
                calculateBorrowingCapacityUseCase
        );
    }

    // ===== Helpers =====
    private Application buildApplication(String email, BigDecimal amount, Long loanTypeId, LocalDate term) {
        return Application.builder()
                .email(email)
                .amount(amount)
                .loanTypeId(loanTypeId)
                .term(term)
                .identityDocument("12345678")
                .build();
    }

    private LoanType buildLoanType(Long id, BigDecimal min, BigDecimal max) {
        return LoanType.builder()
                .id(id)
                .name("Personal Loan")
                .amountMin(min)
                .amountMax(max)
                .interestRate(BigDecimal.valueOf(12.5))
                .automaticValidation(true)
                .riskLevel(3L)
                .build();
    }

    private States buildState(Long id, String code) {
        return States.builder()
                .id(id)
                .name("Pending Review")
                .description("Application pending for review")
                .code(code)
                .build();
    }

    // ===== Validation in-memory =====

    @Test
    void errorWhenApplicationIsNull() {
        StepVerifier.create(useCase.execute(null))
                .expectError(ValidationException.class)
                .verify();

        verifyNoInteractions(applicationRepository, statesRepository, loanTypeRepository, verifyUserUseCase, calculateBorrowingCapacityUseCase);
    }

    @Test
    void errorWhenIdentityDocumentIsNull() {
        var app = buildApplication("test@example.com", BigDecimal.valueOf(5000), 1L, LocalDate.now().plusMonths(6));
        app.setIdentityDocument(null);

        StepVerifier.create(useCase.execute(app))
                .expectError(ValidationException.class)
                .verify();

        verifyNoInteractions(applicationRepository, statesRepository, loanTypeRepository, calculateBorrowingCapacityUseCase);
    }

    @Test
    void errorWhenEmailIsInvalid() {
        var app = buildApplication("invalid-email", BigDecimal.valueOf(5000), 1L, LocalDate.now().plusMonths(6));

        StepVerifier.create(useCase.execute(app))
                .expectError(ValidationException.class)
                .verify();

        verifyNoInteractions(applicationRepository, statesRepository, loanTypeRepository, calculateBorrowingCapacityUseCase);
    }

    @Test
    void errorWhenAmountIsNull() {
        var app = buildApplication("test@example.com", null, 1L, LocalDate.now().plusMonths(6));

        StepVerifier.create(useCase.execute(app))
                .expectError(ValidationException.class)
                .verify();

        verifyNoInteractions(applicationRepository, statesRepository, loanTypeRepository, calculateBorrowingCapacityUseCase);
    }

    @Test
    void errorWhenTermIsInThePast() {
        var app = buildApplication("test@example.com", BigDecimal.valueOf(5000), 1L, LocalDate.now().minusDays(1));

        StepVerifier.create(useCase.execute(app))
                .expectError(ValidationException.class)
                .verify();

        verifyNoInteractions(applicationRepository, statesRepository, loanTypeRepository, calculateBorrowingCapacityUseCase);
    }

    @Test
    void errorWhenLoanTypeIdIsNull() {
        var app = buildApplication("test@example.com", BigDecimal.valueOf(5000), null, LocalDate.now().plusMonths(6));

        StepVerifier.create(useCase.execute(app))
                .expectError(ValidationException.class)
                .verify();

        verifyNoInteractions(applicationRepository, statesRepository, loanTypeRepository, calculateBorrowingCapacityUseCase);
    }

    // ===== Success Flows =====

//    @Test
//    void successFlow_whenLoanTypeHighRisk_shouldNotInvokeCalculateCapacity() {
//        var app = buildApplication("ok@mail.com", BigDecimal.valueOf(8000), 1L, LocalDate.now().plusMonths(6));
//        var state = buildState(100L, DataValidation.PENDING_STATUS_CODE);
//        var loanType = buildLoanType(1L, BigDecimal.valueOf(1000), BigDecimal.valueOf(20000));
//        loanType.setRiskLevel(10L); // riesgo alto
//
//        when(verifyUserUseCase.execute(any(), any())).thenReturn(Mono.just(BigDecimal.valueOf(3000)));
//        when(loanTypeRepository.findById(1L)).thenReturn(Mono.just(loanType));
//        when(statesRepository.findByCode(DataValidation.PENDING_STATUS_CODE)).thenReturn(Mono.just(state));
//        when(applicationRepository.save(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
//
//        StepVerifier.create(useCase.execute(app))
//                .expectNextMatches(saved -> saved.getStateId().equals(100L))
//                .verifyComplete();
//
//        verify(calculateBorrowingCapacityUseCase, never()).execute(any(), any());
//    }

    @Test
    void successFlow_whenLoanTypeLowRisk_shouldInvokeCalculateCapacity() {
        var app = buildApplication("ok@mail.com", BigDecimal.valueOf(8000), 1L, LocalDate.now().plusMonths(6));
        var state = buildState(200L, DataValidation.PENDING_STATUS_CODE);
        var loanType = buildLoanType(1L, BigDecimal.valueOf(1000), BigDecimal.valueOf(20000));
        loanType.setRiskLevel(3L); // riesgo bajo

        when(verifyUserUseCase.execute(any(), any())).thenReturn(Mono.just(BigDecimal.valueOf(5000)));
        when(loanTypeRepository.findById(1L)).thenReturn(Mono.just(loanType));
        when(statesRepository.findByCode(DataValidation.PENDING_STATUS_CODE)).thenReturn(Mono.just(state));
        when(applicationRepository.save(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        when(calculateBorrowingCapacityUseCase.execute(any(), any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(useCase.execute(app))
                .expectNextMatches(saved -> saved.getStateId().equals(200L))
                .verifyComplete();

        verify(calculateBorrowingCapacityUseCase).execute(any(), eq(BigDecimal.valueOf(5000)));
    }

    // ===== Errors in dependencies =====

    @Test
    void errorWhenUserVerificationFails() {
        var app = buildApplication("user@mail.com", BigDecimal.valueOf(7000), 1L, LocalDate.now().plusMonths(6));
        when(verifyUserUseCase.execute(any(), any())).thenReturn(Mono.error(new RuntimeException("gateway down")));

        StepVerifier.create(useCase.execute(app))
                .expectErrorMatches(e -> e instanceof RuntimeException && e.getMessage().equals("gateway down"))
                .verify();

        verifyNoInteractions(applicationRepository, statesRepository, calculateBorrowingCapacityUseCase);
    }
//
//    @Test
//    void errorWhenLoanTypeNotFound() {
//        var app = buildApplication("user@mail.com", BigDecimal.valueOf(7000), 1L, LocalDate.now().plusMonths(6));
//        when(verifyUserUseCase.execute(any(), any())).thenReturn(Mono.just(BigDecimal.valueOf(4000)));
//        when(loanTypeRepository.findById(1L)).thenReturn(Mono.empty());
//
//        StepVerifier.create(useCase.execute(app))
//                .expectError(ConfigurationException.class)
//                .verify();
//    }

    @Test
    void errorWhenPendingStateNotFound() {
        var app = buildApplication("user@mail.com", BigDecimal.valueOf(7000), 1L, LocalDate.now().plusMonths(6));
        var loanType = buildLoanType(1L, BigDecimal.valueOf(1000), BigDecimal.valueOf(20000));

        when(verifyUserUseCase.execute(any(), any())).thenReturn(Mono.just(BigDecimal.valueOf(4000)));
        when(loanTypeRepository.findById(1L)).thenReturn(Mono.just(loanType));
        when(statesRepository.findByCode(DataValidation.PENDING_STATUS_CODE)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(app))
                .expectError(ConfigurationException.class)
                .verify();
    }
}
