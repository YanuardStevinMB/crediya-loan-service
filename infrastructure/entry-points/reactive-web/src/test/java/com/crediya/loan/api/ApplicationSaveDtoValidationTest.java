package com.crediya.loan.api;

import com.crediya.loan.api.dto.ApplicationSaveDto;
import com.crediya.loan.usecase.generaterequest.shared.Messages;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.Email;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ApplicationSaveDtoValidationTest {
    private static Validator validator;

    @BeforeAll
    static void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // ---------- Happy path ----------
    @Test
    @DisplayName("DTO válido: no debe tener violaciones")
    void validDto_shouldHaveNoViolations() {
        ApplicationSaveDto dto = valid();

        Set<ConstraintViolation<ApplicationSaveDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), () -> "Violaciones inesperadas: " + violations);
    }

    // ---------- amount ----------
    @Test
    @DisplayName("amount null → NotNull con mensaje AMOUNT_REQUIRED")
    void amount_null_shouldFailWithMessage() {
        ApplicationSaveDto dto = validWith(amount(null));

        ConstraintViolation<ApplicationSaveDto> v = single(validator.validate(dto));
        assertEquals("amount", v.getPropertyPath().toString());
        assertEquals(Messages.AMOUNT_REQUIRED, v.getMessage());
    }

    // ---------- term ----------
    @Test
    @DisplayName("term null → NotNull con mensaje TERM_REQUIRED")
    void term_null_shouldFailWithMessage() {
        ApplicationSaveDto dto = validWith(term(null));

        ConstraintViolation<ApplicationSaveDto> v = single(validator.validate(dto));
        assertEquals("term", v.getPropertyPath().toString());
        assertEquals(Messages.TERM_REQUIRED, v.getMessage());
    }

    // ---------- email ----------
    @Test
    @DisplayName("email en blanco → NotBlank (EMAIL_REQUIRED)")
    void email_blank_shouldFail() {
        var v = validator.validate(email("   "));
        assertTrue(v.stream().anyMatch(cv ->
                "email".equals(cv.getPropertyPath().toString())
                        && Messages.EMAIL_REQUIRED.equals(cv.getMessage())
        ));
    }

    @Test
    @DisplayName("email formato inválido → @Email")
    void email_invalidFormat_shouldFailByEmailAnnotation() {
        ApplicationSaveDto dto = validWith(email("not-an-email"));

        ConstraintViolation<ApplicationSaveDto> v = single(validator.validate(dto));
        assertEquals("email", v.getPropertyPath().toString());
        // Confirmamos que la violación proviene de @Email
        assertEquals(Email.class, v.getConstraintDescriptor().getAnnotation().annotationType());
    }

    // ---------- identityDocument ----------
    @Test
    @DisplayName("identityDocument en blanco → NotBlank (DOC_REQUIRED)")
    void identityDocument_blank_shouldFail() {
        var v = validator.validate(identity(""));
        assertTrue(v.stream().anyMatch(cv ->
                "identityDocument".equals(cv.getPropertyPath().toString())
                        && Messages.DOC_REQUIRED.equals(cv.getMessage())
        ));
    }


    @Test
    @DisplayName("identityDocument no numérico → Pattern con mensaje DOC_NUMERIC")
    void identityDocument_nonNumeric_shouldFailWithMessage() {
        ApplicationSaveDto dto = validWith(identity("12ab34"));

        ConstraintViolation<ApplicationSaveDto> v = single(validator.validate(dto));
        assertEquals("identityDocument", v.getPropertyPath().toString());
        assertEquals(Messages.DOC_NUMERIC, v.getMessage());
    }

    @Test
    @DisplayName("identityDocument muy corto → Size con mensaje DOC_LENGTH")
    void identityDocument_tooShort_shouldFailWithMessage() {
        ApplicationSaveDto dto = validWith(identity("12345")); // min = 6

        ConstraintViolation<ApplicationSaveDto> v = single(validator.validate(dto));
        assertEquals("identityDocument", v.getPropertyPath().toString());
        assertEquals(Messages.DOC_LENGTH, v.getMessage());
    }

    @Test
    @DisplayName("identityDocument muy largo → Size con mensaje DOC_LENGTH")
    void identityDocument_tooLong_shouldFailWithMessage() {
        ApplicationSaveDto dto = validWith(identity("123456789012345678901")); // 21, max = 20

        ConstraintViolation<ApplicationSaveDto> v = single(validator.validate(dto));
        assertEquals("identityDocument", v.getPropertyPath().toString());
        assertEquals(Messages.DOC_LENGTH, v.getMessage());
    }

    // ---------- loanTypeId ----------
    @Test
    @DisplayName("loanTypeId null → NotNull con mensaje LOAN_TYPE_REQUIRED")
    void loanTypeId_null_shouldFailWithMessage() {
        ApplicationSaveDto dto = validWith(loanTypeId(null));

        ConstraintViolation<ApplicationSaveDto> v = single(validator.validate(dto));
        assertEquals("loanTypeId", v.getPropertyPath().toString());
        assertEquals(Messages.LOAN_TYPE_REQUIRED, v.getMessage());
    }

    // ---------- Extras de record ----------
    @Test
    @DisplayName("Record: equals/hashCode para componentes idénticos")
    void record_equalsAndHashCode() {
        ApplicationSaveDto a = valid();
        ApplicationSaveDto b = valid();

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    @DisplayName("Record: toString() no nulo y con nombre del tipo")
    void record_toStringNotNull() {
        ApplicationSaveDto dto = valid();
        String s = dto.toString();
        assertNotNull(s);
        assertTrue(s.contains("ApplicationSaveDto"));
    }

    // ===== Helpers =====

    private static ConstraintViolation<ApplicationSaveDto> single(Set<ConstraintViolation<ApplicationSaveDto>> v) {
        assertEquals(1, v.size(), () -> "Se esperaban 1 violación, pero fueron: " + v);
        return v.iterator().next();
    }

    private static ApplicationSaveDto valid() {
        return new ApplicationSaveDto(
                null,
                new BigDecimal("1000.00"),
                LocalDate.of(2026, 1, 31),
                "john.doe@example.com",
                "123456789",
                1L
        );
    }

    private static ApplicationSaveDto validWith(ApplicationSaveDto v) { return v; }

    private static ApplicationSaveDto amount(BigDecimal amount) {
        return new ApplicationSaveDto(null, amount, LocalDate.of(2026,1,31),
                "john.doe@example.com", "123456", 1L);
    }

    private static ApplicationSaveDto term(LocalDate term) {
        return new ApplicationSaveDto(null, new BigDecimal("1000.00"), term,
                "john.doe@example.com", "123456", 1L);
    }

    private static ApplicationSaveDto email(String email) {
        return new ApplicationSaveDto(null, new BigDecimal("1000.00"), LocalDate.of(2026,1,31),
                email, "123456", 1L);
    }

    private static ApplicationSaveDto identity(String idDoc) {
        return new ApplicationSaveDto(null, new BigDecimal("1000.00"), LocalDate.of(2026,1,31),
                "john.doe@example.com", idDoc, 1L);
    }

    private static ApplicationSaveDto loanTypeId(Long loanTypeId) {
        return new ApplicationSaveDto(null, new BigDecimal("1000.00"), LocalDate.of(2026,1,31),
                "john.doe@example.com", "123456", loanTypeId);
    }
}
