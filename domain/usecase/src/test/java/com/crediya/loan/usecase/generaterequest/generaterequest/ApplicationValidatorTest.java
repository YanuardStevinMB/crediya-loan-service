package com.crediya.loan.usecase.generaterequest.generaterequest;

import com.crediya.loan.model.application.Application;
import com.crediya.loan.usecase.shared.Messages;
import com.crediya.loan.usecase.shared.ValidationException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationValidatorTest {

    // ---------- Helper: App válido base ----------
    private Application validApp() {
        return Application.builder()
                .id(1L)
                .amount(new BigDecimal("1000.00"))
                .term(LocalDate.now().plusDays(10))
                .email("USER@Mail.Com  ") // para verificar normalización (trim + lower)
                .identityDocument("123456")
                .stateId(1L)
                .loanTypeId(1L)
                .build();
    }

    // ---------- Éxito ----------
    @Test
    void validateAndNormalize_shouldPass_andNormalizeEmail() {
        Application a = validApp();

        assertDoesNotThrow(() -> ApplicationValidator.validateAndNormalize(a));
        assertEquals("user@mail.com", a.getEmail(), "Debe normalizar email (trim + lower)");
    }

    // ---------- Cuerpo requerido ----------
    @Test
    void validateAndNormalize_shouldFail_whenBodyIsNull() {
        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> ApplicationValidator.validateAndNormalize(null)
        );
        assertNull(ex.getField());
        assertEquals(Messages.REQ_BODY_REQUIRED, ex.getMessage());
    }

    // ---------- Documento ----------
    @Test
    void validateAndNormalize_shouldFail_whenDocMissing() {
        Application a = validApp();
        a.setIdentityDocument(null);

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> ApplicationValidator.validateAndNormalize(a)
        );
        assertEquals("identityDocument", ex.getField());
        assertEquals(Messages.DOC_REQUIRED, ex.getMessage());
    }

    @Test
    void validateAndNormalize_shouldFail_whenDocNotNumeric() {
        Application a = validApp();
        a.setIdentityDocument("12A456");

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> ApplicationValidator.validateAndNormalize(a)
        );
        assertEquals("identityDocument", ex.getField());
        assertEquals(Messages.DOC_NUMERIC, ex.getMessage());
    }

    @Test
    void validateAndNormalize_shouldFail_whenDocTooShort() {
        Application a = validApp();
        a.setIdentityDocument("12345"); // < 6

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> ApplicationValidator.validateAndNormalize(a)
        );
        assertEquals("identityDocument", ex.getField());
        assertEquals(Messages.DOC_LENGTH, ex.getMessage());
    }

    @Test
    void validateAndNormalize_shouldFail_whenDocTooLong() {
        Application a = validApp();
        a.setIdentityDocument("123456789012345678901"); // > 20 (21 dígitos)

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> ApplicationValidator.validateAndNormalize(a)
        );
        assertEquals("identityDocument", ex.getField());
        assertEquals(Messages.DOC_LENGTH, ex.getMessage());
    }

    // ---------- Email ----------
    @Test
    void validateAndNormalize_shouldFail_whenEmailMissing() {
        Application a = validApp();
        a.setEmail("   ");

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> ApplicationValidator.validateAndNormalize(a)
        );
        assertEquals("email", ex.getField());
        assertEquals(Messages.EMAIL_REQUIRED, ex.getMessage());
    }

    @Test
    void validateAndNormalize_shouldFail_whenEmailInvalid() {
        Application a = validApp();
        a.setEmail("foo@"); // inválido para el regex

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> ApplicationValidator.validateAndNormalize(a)
        );
        assertEquals("email", ex.getField());
        assertEquals(Messages.EMAIL_INVALID, ex.getMessage());
    }

    // ---------- Monto ----------
    @Test
    void validateAndNormalize_shouldFail_whenAmountMissing() {
        Application a = validApp();
        a.setAmount(null);

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> ApplicationValidator.validateAndNormalize(a)
        );
        assertEquals("amount", ex.getField());
        assertEquals(Messages.AMOUNT_REQUIRED, ex.getMessage());
    }

    @Test
    void validateAndNormalize_shouldFail_whenAmountHasMoreThan2Decimals() {
        Application a = validApp();
        a.setAmount(new BigDecimal("10.123"));

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> ApplicationValidator.validateAndNormalize(a)
        );
        assertEquals("amount", ex.getField());
        assertEquals(Messages.AMOUNT_DECIMALS, ex.getMessage());
    }

    @Test
    void validateAndNormalize_shouldFail_whenAmountIsZeroOrNegative() {
        Application a = validApp();
        a.setAmount(new BigDecimal("0"));

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> ApplicationValidator.validateAndNormalize(a)
        );
        assertEquals("amount", ex.getField());
        assertEquals(Messages.AMOUNT_RANGE, ex.getMessage());
    }

    @Test
    void validateAndNormalize_shouldFail_whenAmountExceedsMax() {
        Application a = validApp();
        a.setAmount(new BigDecimal("15000001")); // > 15,000,000

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> ApplicationValidator.validateAndNormalize(a)
        );
        assertEquals("amount", ex.getField());
        assertEquals(Messages.AMOUNT_RANGE, ex.getMessage());
    }

    // ---------- Plazo (term) ----------
    @Test
    void validateAndNormalize_shouldFail_whenTermMissing() {
        Application a = validApp();
        a.setTerm(null);

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> ApplicationValidator.validateAndNormalize(a)
        );
        assertEquals("term", ex.getField());
        assertEquals(Messages.TERM_REQUIRED, ex.getMessage());
    }

    @Test
    void validateAndNormalize_shouldFail_whenTermNotFuture() {
        Application a = validApp();
        a.setTerm(LocalDate.now()); // no es estrictamente futuro

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> ApplicationValidator.validateAndNormalize(a)
        );
        assertEquals("term", ex.getField());
        assertEquals(Messages.TERM_POSITIVE, ex.getMessage());
    }

    // ---------- Tipo de préstamo ----------
    @Test
    void validateAndNormalize_shouldFail_whenLoanTypeIdMissing() {
        Application a = validApp();
        a.setLoanTypeId(null);

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> ApplicationValidator.validateAndNormalize(a)
        );
        assertEquals("loanTypeId", ex.getField());
        assertEquals(Messages.LOAN_TYPE_REQUIRED, ex.getMessage());
    }

    @Test
    void validateAndNormalize_shouldFail_whenLoanTypeIdNonPositive() {
        Application a = validApp();
        a.setLoanTypeId(0L);

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> ApplicationValidator.validateAndNormalize(a)
        );
        assertEquals("loanTypeId", ex.getField());
        assertEquals(Messages.LOAN_TYPE_REQUIRED, ex.getMessage());
    }
}
