package com.crediya.loan.usecase.generaterequest.generaterequest;

import com.crediya.loan.model.application.Application;
import com.crediya.loan.usecase.shared.exception.BusinessValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class EnhancedApplicationValidatorTest {

    // Helper para crear Application base válida
    private Application validApplication() {
        return Application.builder()
                .identityDocument("12345678")
                .email("valid@example.com")
                .amount(new BigDecimal("5000.00"))
                .term(LocalDate.now().plusMonths(12))
                .loanTypeId(1L)
                .build();
    }

    // ===== Tests para validateAndNormalize =====

    @Test
    @DisplayName("validateAndNormalize debe funcionar correctamente con datos válidos")
    void validateAndNormalize_success_withValidData() {
        Application app = validApplication();
        
        assertDoesNotThrow(() -> EnhancedApplicationValidator.validateAndNormalize(app));
        
        // Verificar que el email se normalizó (lowercase, trim)
        assertEquals("valid@example.com", app.getEmail());
    }

    @Test
    @DisplayName("validateAndNormalize debe normalizar email a lowercase")
    void validateAndNormalize_shouldNormalizeEmailToLowercase() {
        Application app = validApplication();
        app.setEmail("  TEST@EXAMPLE.COM  ");
        
        assertDoesNotThrow(() -> EnhancedApplicationValidator.validateAndNormalize(app));
        
        assertEquals("test@example.com", app.getEmail());
    }

    @Test
    @DisplayName("validateAndNormalize debe fallar cuando Application es null")
    void validateAndNormalize_shouldFailWhenApplicationIsNull() {
        BusinessValidationException ex = assertThrows(
            BusinessValidationException.class,
            () -> EnhancedApplicationValidator.validateAndNormalize(null)
        );
        
        assertEquals("application", ex.getField());
        assertEquals("Los datos de la solicitud son requeridos", ex.getUserMessage());
    }

    // ===== Tests para Identity Document =====

    @Test
    @DisplayName("validateAndNormalize debe fallar cuando identityDocument es null")
    void validateAndNormalize_shouldFailWhenIdentityDocumentIsNull() {
        Application app = validApplication();
        app.setIdentityDocument(null);
        
        BusinessValidationException ex = assertThrows(
            BusinessValidationException.class,
            () -> EnhancedApplicationValidator.validateAndNormalize(app)
        );
        
        assertEquals("identityDocument", ex.getField());
        assertEquals("El documento de identidad es requerido", ex.getUserMessage());
    }

    @Test
    @DisplayName("validateAndNormalize debe fallar cuando identityDocument está vacío")
    void validateAndNormalize_shouldFailWhenIdentityDocumentIsBlank() {
        Application app = validApplication();
        app.setIdentityDocument("  ");
        
        BusinessValidationException ex = assertThrows(
            BusinessValidationException.class,
            () -> EnhancedApplicationValidator.validateAndNormalize(app)
        );
        
        assertEquals("identityDocument", ex.getField());
        assertEquals("El documento de identidad es requerido", ex.getUserMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"123abc", "12.34", "12-34", "abc123", "12 34"})
    @DisplayName("validateAndNormalize debe fallar cuando identityDocument contiene caracteres no numéricos")
    void validateAndNormalize_shouldFailWhenIdentityDocumentContainsNonNumericCharacters(String invalidDoc) {
        Application app = validApplication();
        app.setIdentityDocument(invalidDoc);
        
        BusinessValidationException ex = assertThrows(
            BusinessValidationException.class,
            () -> EnhancedApplicationValidator.validateAndNormalize(app)
        );
        
        assertEquals("identityDocument", ex.getField());
        assertEquals(invalidDoc, ex.getRejectedValue());
        assertEquals("El documento de identidad debe contener solo números", ex.getUserMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"12345", "123456789012345678901"})
    @DisplayName("validateAndNormalize debe fallar cuando identityDocument tiene longitud inválida")
    void validateAndNormalize_shouldFailWhenIdentityDocumentHasInvalidLength(String invalidDoc) {
        Application app = validApplication();
        app.setIdentityDocument(invalidDoc);
        
        BusinessValidationException ex = assertThrows(
            BusinessValidationException.class,
            () -> EnhancedApplicationValidator.validateAndNormalize(app)
        );
        
        assertEquals("identityDocument", ex.getField());
        assertEquals(invalidDoc, ex.getRejectedValue());
        assertEquals("El documento de identidad debe tener entre 6 y 20 dígitos", ex.getUserMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"123456", "12345678901234567890"})
    @DisplayName("validateAndNormalize debe aceptar identityDocument con longitud válida")
    void validateAndNormalize_shouldAcceptValidIdentityDocumentLength(String validDoc) {
        Application app = validApplication();
        app.setIdentityDocument(validDoc);
        
        assertDoesNotThrow(() -> EnhancedApplicationValidator.validateAndNormalize(app));
    }

    // ===== Tests para Email =====

    @Test
    @DisplayName("validateAndNormalize debe fallar cuando email es null")
    void validateAndNormalize_shouldFailWhenEmailIsNull() {
        Application app = validApplication();
        app.setEmail(null);
        
        BusinessValidationException ex = assertThrows(
            BusinessValidationException.class,
            () -> EnhancedApplicationValidator.validateAndNormalize(app)
        );
        
        assertEquals("email", ex.getField());
        assertEquals("El correo electrónico es requerido", ex.getUserMessage());
    }

    @Test
    @DisplayName("validateAndNormalize debe fallar cuando email está vacío")
    void validateAndNormalize_shouldFailWhenEmailIsBlank() {
        Application app = validApplication();
        app.setEmail("   ");
        
        BusinessValidationException ex = assertThrows(
            BusinessValidationException.class,
            () -> EnhancedApplicationValidator.validateAndNormalize(app)
        );
        
        assertEquals("email", ex.getField());
        assertEquals("El correo electrónico es requerido", ex.getUserMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid-email", "test@", "@domain.com", "test.domain.com"})
    @DisplayName("validateAndNormalize debe fallar cuando email tiene formato inválido")
    void validateAndNormalize_shouldFailWhenEmailHasInvalidFormat(String invalidEmail) {
        Application app = validApplication();
        app.setEmail(invalidEmail);
        
        BusinessValidationException ex = assertThrows(
            BusinessValidationException.class,
            () -> EnhancedApplicationValidator.validateAndNormalize(app)
        );
        
        assertEquals("email", ex.getField());
        assertEquals(invalidEmail, ex.getRejectedValue());
        assertEquals("El formato del correo electrónico no es válido", ex.getUserMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"test@example.com", "user.name@domain.co.uk", "test+tag@example.org", "123@456.com", "test@domain"})
    @DisplayName("validateAndNormalize debe aceptar emails con formato válido")
    void validateAndNormalize_shouldAcceptValidEmailFormats(String validEmail) {
        Application app = validApplication();
        app.setEmail(validEmail);
        
        assertDoesNotThrow(() -> EnhancedApplicationValidator.validateAndNormalize(app));
    }

    // ===== Tests para Amount =====

    @Test
    @DisplayName("validateAndNormalize debe fallar cuando amount es null")
    void validateAndNormalize_shouldFailWhenAmountIsNull() {
        Application app = validApplication();
        app.setAmount(null);
        
        BusinessValidationException ex = assertThrows(
            BusinessValidationException.class,
            () -> EnhancedApplicationValidator.validateAndNormalize(app)
        );
        
        assertEquals("amount", ex.getField());
        assertEquals("El monto del préstamo es requerido", ex.getUserMessage());
    }

    @Test
    @DisplayName("validateAndNormalize debe fallar cuando amount tiene más de 2 decimales")
    void validateAndNormalize_shouldFailWhenAmountHasMoreThanTwoDecimals() {
        Application app = validApplication();
        app.setAmount(new BigDecimal("1000.123"));
        
        BusinessValidationException ex = assertThrows(
            BusinessValidationException.class,
            () -> EnhancedApplicationValidator.validateAndNormalize(app)
        );
        
        assertEquals("amount", ex.getField());
        assertEquals(new BigDecimal("1000.123"), ex.getRejectedValue());
        assertEquals("El monto no puede tener más de 2 decimales", ex.getUserMessage());
    }

    @Test
    @DisplayName("validateAndNormalize debe aceptar amount con 2 decimales o menos")
    void validateAndNormalize_shouldAcceptAmountWithTwoDecimalsOrLess() {
        Application app1 = validApplication();
        app1.setAmount(new BigDecimal("1000.12"));
        assertDoesNotThrow(() -> EnhancedApplicationValidator.validateAndNormalize(app1));

        Application app2 = validApplication();
        app2.setAmount(new BigDecimal("1000.1"));
        assertDoesNotThrow(() -> EnhancedApplicationValidator.validateAndNormalize(app2));

        Application app3 = validApplication();
        app3.setAmount(new BigDecimal("1000"));
        assertDoesNotThrow(() -> EnhancedApplicationValidator.validateAndNormalize(app3));
    }

    @Test
    @DisplayName("validateAndNormalize debe fallar cuando amount es 0 o negativo")
    void validateAndNormalize_shouldFailWhenAmountIsZeroOrNegative() {
        Application app1 = validApplication();
        app1.setAmount(BigDecimal.ZERO);
        
        BusinessValidationException ex1 = assertThrows(
            BusinessValidationException.class,
            () -> EnhancedApplicationValidator.validateAndNormalize(app1)
        );
        
        assertEquals("loanAmount", ex1.getField());
        assertEquals(0.0, ex1.getRejectedValue());

        Application app2 = validApplication();
        app2.setAmount(new BigDecimal("-100"));
        
        BusinessValidationException ex2 = assertThrows(
            BusinessValidationException.class,
            () -> EnhancedApplicationValidator.validateAndNormalize(app2)
        );
        
        assertEquals("loanAmount", ex2.getField());
        assertEquals(-100.0, ex2.getRejectedValue());
    }

    @Test
    @DisplayName("validateAndNormalize debe fallar cuando amount excede el máximo")
    void validateAndNormalize_shouldFailWhenAmountExceedsMaximum() {
        Application app = validApplication();
        app.setAmount(new BigDecimal("15000001"));
        
        BusinessValidationException ex = assertThrows(
            BusinessValidationException.class,
            () -> EnhancedApplicationValidator.validateAndNormalize(app)
        );
        
        assertEquals("loanAmount", ex.getField());
        assertEquals(15000001.0, ex.getRejectedValue());
    }

    @Test
    @DisplayName("validateAndNormalize debe aceptar amount en rango válido")
    void validateAndNormalize_shouldAcceptAmountInValidRange() {
        Application app1 = validApplication();
        app1.setAmount(new BigDecimal("0.01"));
        assertDoesNotThrow(() -> EnhancedApplicationValidator.validateAndNormalize(app1));

        Application app2 = validApplication();
        app2.setAmount(new BigDecimal("15000000"));
        assertDoesNotThrow(() -> EnhancedApplicationValidator.validateAndNormalize(app2));

        Application app3 = validApplication();
        app3.setAmount(new BigDecimal("7500000"));
        assertDoesNotThrow(() -> EnhancedApplicationValidator.validateAndNormalize(app3));
    }

    // ===== Tests para Term =====

    @Test
    @DisplayName("validateAndNormalize debe fallar cuando term es null")
    void validateAndNormalize_shouldFailWhenTermIsNull() {
        Application app = validApplication();
        app.setTerm(null);
        
        BusinessValidationException ex = assertThrows(
            BusinessValidationException.class,
            () -> EnhancedApplicationValidator.validateAndNormalize(app)
        );
        
        assertEquals("term", ex.getField());
        assertEquals("La fecha de vencimiento es requerida", ex.getUserMessage());
    }

    @Test
    @DisplayName("validateAndNormalize debe fallar cuando term está en el pasado")
    void validateAndNormalize_shouldFailWhenTermIsInThePast() {
        Application app = validApplication();
        LocalDate pastDate = LocalDate.now().minusDays(1);
        app.setTerm(pastDate);
        
        BusinessValidationException ex = assertThrows(
            BusinessValidationException.class,
            () -> EnhancedApplicationValidator.validateAndNormalize(app)
        );
        
        assertEquals("term", ex.getField());
        assertEquals(pastDate, ex.getRejectedValue());
        assertEquals("La fecha de vencimiento debe ser posterior a la fecha actual", ex.getUserMessage());
    }

    @Test
    @DisplayName("validateAndNormalize debe fallar cuando term es hoy")
    void validateAndNormalize_shouldFailWhenTermIsToday() {
        Application app = validApplication();
        app.setTerm(LocalDate.now());
        
        BusinessValidationException ex = assertThrows(
            BusinessValidationException.class,
            () -> EnhancedApplicationValidator.validateAndNormalize(app)
        );
        
        assertEquals("term", ex.getField());
        assertEquals("La fecha de vencimiento debe ser posterior a la fecha actual", ex.getUserMessage());
    }

    @Test
    @DisplayName("validateAndNormalize debe aceptar term en el futuro")
    void validateAndNormalize_shouldAcceptFutureTerm() {
        Application app = validApplication();
        app.setTerm(LocalDate.now().plusDays(1));
        
        assertDoesNotThrow(() -> EnhancedApplicationValidator.validateAndNormalize(app));
    }

    // ===== Tests para LoanType =====

    @Test
    @DisplayName("validateAndNormalize debe fallar cuando loanTypeId es null")
    void validateAndNormalize_shouldFailWhenLoanTypeIdIsNull() {
        Application app = validApplication();
        app.setLoanTypeId(null);
        
        BusinessValidationException ex = assertThrows(
            BusinessValidationException.class,
            () -> EnhancedApplicationValidator.validateAndNormalize(app)
        );
        
        assertEquals("loanTypeId", ex.getField());
        assertNull(ex.getRejectedValue());
        assertEquals("Debe seleccionar un tipo de préstamo válido", ex.getUserMessage());
    }

    @ParameterizedTest
    @ValueSource(longs = {0L, -1L, -100L})
    @DisplayName("validateAndNormalize debe fallar cuando loanTypeId es 0 o negativo")
    void validateAndNormalize_shouldFailWhenLoanTypeIdIsZeroOrNegative(Long invalidId) {
        Application app = validApplication();
        app.setLoanTypeId(invalidId);
        
        BusinessValidationException ex = assertThrows(
            BusinessValidationException.class,
            () -> EnhancedApplicationValidator.validateAndNormalize(app)
        );
        
        assertEquals("loanTypeId", ex.getField());
        assertEquals(invalidId, ex.getRejectedValue());
        assertEquals("Debe seleccionar un tipo de préstamo válido", ex.getUserMessage());
    }

    @ParameterizedTest
    @ValueSource(longs = {1L, 2L, 100L, 999L})
    @DisplayName("validateAndNormalize debe aceptar loanTypeId positivo")
    void validateAndNormalize_shouldAcceptPositiveLoanTypeId(Long validId) {
        Application app = validApplication();
        app.setLoanTypeId(validId);
        
        assertDoesNotThrow(() -> EnhancedApplicationValidator.validateAndNormalize(app));
    }

    // ===== Tests de comportamiento completo =====

    @Test
    @DisplayName("validateAndNormalize debe validar todos los campos en orden")
    void validateAndNormalize_shouldValidateAllFieldsInOrder() {
        // Crear una aplicación con múltiples errores
        Application app = Application.builder()
                .identityDocument(null) // Primer error
                .email("invalid-email") 
                .amount(null)
                .term(null)
                .loanTypeId(null)
                .build();
        
        // Debería fallar en el primer error encontrado (identityDocument)
        BusinessValidationException ex = assertThrows(
            BusinessValidationException.class,
            () -> EnhancedApplicationValidator.validateAndNormalize(app)
        );
        
        assertEquals("identityDocument", ex.getField());
    }

    @Test
    @DisplayName("validateAndNormalize debe procesar aplicación completa válida sin errores")
    void validateAndNormalize_shouldProcessCompleteValidApplicationWithoutErrors() {
        Application app = Application.builder()
                .identityDocument("1234567890")
                .email("  TEST.User+Tag@EXAMPLE.COM  ")
                .amount(new BigDecimal("7500000.50"))
                .term(LocalDate.now().plusYears(2))
                .loanTypeId(5L)
                .build();
        
        assertDoesNotThrow(() -> EnhancedApplicationValidator.validateAndNormalize(app));
        
        // Verificar normalización del email
        assertEquals("test.user+tag@example.com", app.getEmail());
        
        // Verificar que otros campos se mantuvieron
        assertEquals("1234567890", app.getIdentityDocument());
        assertEquals(new BigDecimal("7500000.50"), app.getAmount());
        assertEquals(5L, app.getLoanTypeId());
    }
}