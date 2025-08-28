package com.crediya.loan.usecase.generaterequest;

import com.crediya.loan.model.application.Application;
import com.crediya.loan.model.application.gateways.ApplicationRepository;
import com.crediya.loan.model.loantype.LoanType;
import com.crediya.loan.model.loantype.gateways.LoanTypeRepository;
import com.crediya.loan.model.states.States;
import com.crediya.loan.model.states.gateways.StatesRepository;
import com.crediya.loan.usecase.generaterequest.shared.ConfigurationException;
import com.crediya.loan.usecase.generaterequest.shared.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GenerateRequestUseCaseTest {

    @Mock
    ApplicationRepository applicationRepository;

    @Mock
    StatesRepository statesRepository;

    @Mock
    LoanTypeRepository loanTypeRepository;

    GenerateRequestUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GenerateRequestUseCase(applicationRepository, statesRepository, loanTypeRepository);
    }

    // ===== Helper Methods =====
    
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

    // ===== Validation Tests =====

    @Test
    void errorWhenApplicationIsNull() {
        StepVerifier.create(useCase.execute(null))
                .expectError(ValidationException.class)
                .verify();
        
        verifyNoInteractions(applicationRepository, statesRepository, loanTypeRepository);
    }

    @Test
    void errorWhenIdentityDocumentIsNull() {
        var app = buildApplication("test@example.com", BigDecimal.valueOf(5000), 1L, LocalDate.now().plusMonths(6));
        app.setIdentityDocument(null);

        StepVerifier.create(useCase.execute(app))
                .expectError(ValidationException.class)
                .verify();

        verifyNoInteractions(applicationRepository, statesRepository, loanTypeRepository);
    }

    @Test
    void errorWhenEmailIsInvalid() {
        var app = buildApplication("invalid-email", BigDecimal.valueOf(5000), 1L, LocalDate.now().plusMonths(6));

        StepVerifier.create(useCase.execute(app))
                .expectError(ValidationException.class)
                .verify();

        verifyNoInteractions(applicationRepository, statesRepository, loanTypeRepository);
    }

    @Test
    void errorWhenAmountIsNull() {
        var app = buildApplication("test@example.com", null, 1L, LocalDate.now().plusMonths(6));

        StepVerifier.create(useCase.execute(app))
                .expectError(ValidationException.class)
                .verify();

        verifyNoInteractions(applicationRepository, statesRepository, loanTypeRepository);
    }

    @Test
    void errorWhenTermIsInThePast() {
        var app = buildApplication("test@example.com", BigDecimal.valueOf(5000), 1L, LocalDate.now().minusDays(1));

        StepVerifier.create(useCase.execute(app))
                .expectError(ValidationException.class)
                .verify();

        verifyNoInteractions(applicationRepository, statesRepository, loanTypeRepository);
    }

    @Test
    void errorWhenLoanTypeIdIsNull() {
        var app = buildApplication("test@example.com", BigDecimal.valueOf(5000), null, LocalDate.now().plusMonths(6));

        StepVerifier.create(useCase.execute(app))
                .expectError(ValidationException.class)
                .verify();

        verifyNoInteractions(applicationRepository, statesRepository, loanTypeRepository);
    }

    // ===== Business Logic Tests =====

    @Test
    void errorWhenLoanTypeNotExists() {
        var app = buildApplication("test@example.com", BigDecimal.valueOf(5000), 1L, LocalDate.now().plusMonths(6));
        
        when(loanTypeRepository.findById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(app))
                .expectError(ConfigurationException.class)
                .verify();

        verify(loanTypeRepository, times(1)).findById(1L);
        verifyNoInteractions(statesRepository, applicationRepository);
    }

    @Test
    void errorWhenAmountBelowMinimum() {
        var app = buildApplication("test@example.com", BigDecimal.valueOf(500), 1L, LocalDate.now().plusMonths(6));
        var loanType = buildLoanType(1L, BigDecimal.valueOf(1000), BigDecimal.valueOf(50000));

        when(loanTypeRepository.findById(1L)).thenReturn(Mono.just(loanType));

        StepVerifier.create(useCase.execute(app))
                .expectError(ValidationException.class)
                .verify();

        verify(loanTypeRepository, times(1)).findById(1L);
        verifyNoInteractions(statesRepository, applicationRepository);
    }

    @Test
    void errorWhenAmountAboveMaximum() {
        var app = buildApplication("test@example.com", BigDecimal.valueOf(60000), 1L, LocalDate.now().plusMonths(6));
        var loanType = buildLoanType(1L, BigDecimal.valueOf(1000), BigDecimal.valueOf(50000));

        when(loanTypeRepository.findById(1L)).thenReturn(Mono.just(loanType));

        StepVerifier.create(useCase.execute(app))
                .expectError(ValidationException.class)
                .verify();

        verify(loanTypeRepository, times(1)).findById(1L);
        verifyNoInteractions(statesRepository, applicationRepository);
    }

    @Test
    void errorWhenDefaultStateNotExists() {
        var app = buildApplication("test@example.com", BigDecimal.valueOf(5000), 1L, LocalDate.now().plusMonths(6));
        var loanType = buildLoanType(1L, BigDecimal.valueOf(1000), BigDecimal.valueOf(50000));

        when(loanTypeRepository.findById(1L)).thenReturn(Mono.just(loanType));
        when(statesRepository.findByCode("PEN")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(app))
                .expectError(ConfigurationException.class)
                .verify();

        verify(loanTypeRepository, times(1)).findById(1L);
        verify(statesRepository, times(1)).findByCode("PEN");
        verifyNoInteractions(applicationRepository);
    }

    @Nested
    class EmailNormalization {

        @Test
        void emailNormalizesToLowercaseAndTrim() {
            var app = buildApplication("  Test@EXAMPLE.COM  ", BigDecimal.valueOf(5000), 1L, LocalDate.now().plusMonths(6));
            var loanType = buildLoanType(1L, BigDecimal.valueOf(1000), BigDecimal.valueOf(50000));
            var state = buildState(100L, "PEN");
            var savedApp = Application.builder()
                    .id(1L)
                    .email("test@example.com")
                    .amount(BigDecimal.valueOf(5000))
                    .loanTypeId(1L)
                    .term(LocalDate.now().plusMonths(6))
                    .identityDocument("12345678")
                    .stateId(100L)
                    .build();

            when(loanTypeRepository.findById(1L)).thenReturn(Mono.just(loanType));
            when(statesRepository.findByCode("PEN")).thenReturn(Mono.just(state));
            when(applicationRepository.save(any())).thenReturn(Mono.just(savedApp));

            StepVerifier.create(useCase.execute(app))
                    .expectNextMatches(result -> 
                        "test@example.com".equals(result.getEmail()) &&
                        result.getStateId().equals(100L))
                    .verifyComplete();

            verify(loanTypeRepository, times(1)).findById(1L);
            verify(statesRepository, times(1)).findByCode("PEN");
            verify(applicationRepository, times(1)).save(any());
        }
    }

    @Test
    void successfulExecutionFlow() {
        var app = buildApplication("test@example.com", BigDecimal.valueOf(5000), 1L, LocalDate.now().plusMonths(6));
        var loanType = buildLoanType(1L, BigDecimal.valueOf(1000), BigDecimal.valueOf(50000));
        var state = buildState(100L, "PEN");
        var savedApp = Application.builder()
                .id(1L)
                .email("test@example.com")
                .amount(BigDecimal.valueOf(5000))
                .loanTypeId(1L)
                .term(LocalDate.now().plusMonths(6))
                .identityDocument("12345678")
                .stateId(100L)
                .build();

        when(loanTypeRepository.findById(1L)).thenReturn(Mono.just(loanType));
        when(statesRepository.findByCode("PEN")).thenReturn(Mono.just(state));
        when(applicationRepository.save(any())).thenReturn(Mono.just(savedApp));

        StepVerifier.create(useCase.execute(app))
                .expectNextMatches(result -> 
                    result.getId() != null && 
                    result.getId().equals(1L) &&
                    result.getStateId().equals(100L))
                .verifyComplete();

        verify(loanTypeRepository, times(1)).findById(1L);
        verify(statesRepository, times(1)).findByCode("PEN");
        verify(applicationRepository, times(1)).save(any());
    }
}
