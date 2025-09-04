package com.crediya.loan.usecase.generaterequest;

import com.crediya.loan.model.application.Application;
import com.crediya.loan.model.application.gateways.ApplicationRepository;
import com.crediya.loan.model.loantype.LoanType;
import com.crediya.loan.model.loantype.gateways.LoanTypeRepository;
import com.crediya.loan.model.states.States;
import com.crediya.loan.model.states.gateways.StatesRepository;
import com.crediya.loan.usecase.generaterequest.generaterequest.VerifyUserUseCase;
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

    @Mock
    VerifyUserUseCase verifyUserUseCase;

    GenerateRequestUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GenerateRequestUseCase(
                applicationRepository,
                statesRepository,
                loanTypeRepository,
                verifyUserUseCase

        );

        // configuración por defecto: verificar usuario siempre pasa

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



}
