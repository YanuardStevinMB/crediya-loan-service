package com.crediya.loan.usecase.generaterequest.generaterequest;

import com.crediya.loan.model.application.Application;
import com.crediya.loan.model.loantype.LoanType;
import com.crediya.loan.usecase.shared.ValidationException;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class LoanTypeValidatorTest {

    private static LoanType loanType(BigDecimal min, BigDecimal max) {
        return LoanType.builder()
                .id(1L)
                .name("Personal")
                .amountMin(min)
                .amountMax(max)
                .interestRate(new BigDecimal("0.02"))
                .automaticValidation(Boolean.TRUE)
                .build();
    }

    private static Application appWithAmount(BigDecimal amount) {
        return Application.builder()
                .id(10L)
                .amount(amount)
                .term(LocalDate.now().plusMonths(6))
                .email("user@test.com")
                .identityDocument("123")
                .stateId(1L)
                .loanTypeId(1L)
                .build();
    }

    // ----- Casos válidos -----

    @Test
    void validateAmount_shouldPass_whenAmountEqualsMin() {
        var lt = loanType(new BigDecimal("1000"), new BigDecimal("5000"));
        var app = appWithAmount(new BigDecimal("1000")); // == min

        StepVerifier.create(LoanTypeValidator.validateAmount(app, lt))
                .expectNext(app) // mismo objeto (Mono.just(app) pasa el mismo ref)
                .verifyComplete();
    }

    @Test
    void validateAmount_shouldPass_whenAmountEqualsMax() {
        var lt = loanType(new BigDecimal("1000"), new BigDecimal("5000"));
        var app = appWithAmount(new BigDecimal("5000")); // == max

        StepVerifier.create(LoanTypeValidator.validateAmount(app, lt))
                .expectNext(app)
                .verifyComplete();
    }

    @Test
    void validateAmount_shouldPass_whenAmountIsBetweenMinAndMax() {
        var lt = loanType(new BigDecimal("1000"), new BigDecimal("5000"));
        var app = appWithAmount(new BigDecimal("2500"));

        StepVerifier.create(LoanTypeValidator.validateAmount(app, lt))
                .expectNextMatches(a ->
                        a == app && a.getAmount().compareTo(new BigDecimal("2500")) == 0)
                .verifyComplete();
    }

    // ----- Casos inválidos -----

    @Test
    void validateAmount_shouldError_whenAmountIsNull() {
        var lt = loanType(new BigDecimal("1000"), new BigDecimal("5000"));
        var app = appWithAmount(null);

        StepVerifier.create(LoanTypeValidator.validateAmount(app, lt))
                .expectErrorSatisfies(err -> {
                    assertTrue(err instanceof ValidationException);
                    var ve = (ValidationException) err;
                    // Campo y mensaje
                    assertEquals("amount", ve.getField());
                    assertNotNull(ve.getMessage());
                    assertTrue(ve.getMessage().contains("1000"));
                    assertTrue(ve.getMessage().contains("5000"));
                })
                .verify();
    }

    @Test
    void validateAmount_shouldError_whenAmountBelowMin() {
        var lt = loanType(new BigDecimal("1000"), new BigDecimal("5000"));
        var app = appWithAmount(new BigDecimal("999"));

        StepVerifier.create(LoanTypeValidator.validateAmount(app, lt))
                .expectErrorSatisfies(err -> {
                    assertTrue(err instanceof ValidationException);
                    var ve = (ValidationException) err;
                    assertEquals("amount", ve.getField());
                    assertNotNull(ve.getMessage());
                    assertTrue(ve.getMessage().contains("1000"));
                    assertTrue(ve.getMessage().contains("5000"));
                })
                .verify();
    }

    @Test
    void validateAmount_shouldError_whenAmountAboveMax() {
        var lt = loanType(new BigDecimal("1000"), new BigDecimal("5000"));
        var app = appWithAmount(new BigDecimal("5001"));

        StepVerifier.create(LoanTypeValidator.validateAmount(app, lt))
                .expectErrorSatisfies(err -> {
                    assertTrue(err instanceof ValidationException);
                    var ve = (ValidationException) err;
                    assertEquals("amount", ve.getField());
                    assertNotNull(ve.getMessage());
                    assertTrue(ve.getMessage().contains("1000"));
                    assertTrue(ve.getMessage().contains("5000"));
                })
                .verify();
    }
}
