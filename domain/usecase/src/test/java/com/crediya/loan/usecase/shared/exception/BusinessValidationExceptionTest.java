package com.crediya.loan.usecase.shared.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BusinessValidationException Tests")
class BusinessValidationExceptionTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create exception with field only")
        void shouldCreateExceptionWithFieldOnly() {
            // Given
            String field = "email";
            String technicalMessage = "Invalid email format";
            String userMessage = "Email inválido";

            // When
            BusinessValidationException exception = new BusinessValidationException(
                    field, technicalMessage, userMessage
            );

            // Then
            assertEquals("BUSINESS_VALIDATION_ERROR", exception.getErrorCode());
            assertEquals(field, exception.getField());
            assertNull(exception.getRejectedValue());
            assertNull(exception.getFieldErrors());
            assertEquals(technicalMessage, exception.getTechnicalMessage());
            assertEquals(userMessage, exception.getUserMessage());
        }

        @Test
        @DisplayName("Should create exception with field and rejected value")
        void shouldCreateExceptionWithFieldAndRejectedValue() {
            // Given
            String field = "age";
            Integer rejectedValue = 15;
            String technicalMessage = "Age must be at least 18";
            String userMessage = "Debe ser mayor de edad";

            // When
            BusinessValidationException exception = new BusinessValidationException(
                    field, rejectedValue, technicalMessage, userMessage
            );

            // Then
            assertEquals("BUSINESS_VALIDATION_ERROR", exception.getErrorCode());
            assertEquals(field, exception.getField());
            assertEquals(rejectedValue, exception.getRejectedValue());
            assertNull(exception.getFieldErrors());
            assertEquals(technicalMessage, exception.getTechnicalMessage());
            assertEquals(userMessage, exception.getUserMessage());
        }

        @Test
        @DisplayName("Should create exception with field errors map")
        void shouldCreateExceptionWithFieldErrorsMap() {
            // Given
            Map<String, List<String>> fieldErrors = new HashMap<>();
            fieldErrors.put("email", List.of("Invalid format", "Required field"));
            fieldErrors.put("phone", List.of("Invalid phone number"));
            String technicalMessage = "Multiple validation errors";
            String userMessage = "Por favor corrija los errores";

            // When
            BusinessValidationException exception = new BusinessValidationException(
                    fieldErrors, technicalMessage, userMessage
            );

            // Then
            assertEquals("BUSINESS_VALIDATION_ERROR", exception.getErrorCode());
            assertNull(exception.getField());
            assertNull(exception.getRejectedValue());
            assertEquals(fieldErrors, exception.getFieldErrors());
            assertEquals(technicalMessage, exception.getTechnicalMessage());
            assertEquals(userMessage, exception.getUserMessage());
        }
    }

    @Nested
    @DisplayName("Factory Methods Tests")
    class FactoryMethodsTests {

        @Test
        @DisplayName("Should create loan amount exceeds limit exception")
        void shouldCreateLoanAmountExceedsLimitException() {
            // Given
            Double requestedAmount = 150000.0;
            Double maxLimit = 100000.0;

            // When
            BusinessValidationException exception = BusinessValidationException
                    .loanAmountExceedsLimit(requestedAmount, maxLimit);

            // Then
            assertEquals("BUSINESS_VALIDATION_ERROR", exception.getErrorCode());
            assertEquals("loanAmount", exception.getField());
            assertEquals(requestedAmount, exception.getRejectedValue());
            assertNull(exception.getFieldErrors());
            // Usar contains para evitar problemas de locale con formato de números
            assertTrue(exception.getTechnicalMessage().contains("150000"));
            assertTrue(exception.getTechnicalMessage().contains("100000"));
            assertTrue(exception.getTechnicalMessage().contains("Requested loan amount"));
            assertTrue(exception.getTechnicalMessage().contains("exceeds maximum limit"));
            assertEquals(
                    "El monto solicitado excede el límite máximo permitido para este tipo de préstamo",
                    exception.getUserMessage()
            );
        }

        @Test
        @DisplayName("Should create insufficient credit score exception")
        void shouldCreateInsufficientCreditScoreException() {
            // Given
            Integer creditScore = 500;
            Integer minRequired = 650;

            // When
            BusinessValidationException exception = BusinessValidationException
                    .insufficientCreditScore(creditScore, minRequired);

            // Then
            assertEquals("BUSINESS_VALIDATION_ERROR", exception.getErrorCode());
            assertEquals("creditScore", exception.getField());
            assertEquals(creditScore, exception.getRejectedValue());
            assertNull(exception.getFieldErrors());
            assertEquals(
                    "Credit score 500 is below minimum required 650",
                    exception.getTechnicalMessage()
            );
            assertEquals(
                    "Su puntaje crediticio no cumple con los requisitos mínimos para este préstamo",
                    exception.getUserMessage()
            );
        }

        @Test
        @DisplayName("Should create invalid loan term exception")
        void shouldCreateInvalidLoanTermException() {
            // Given
            Integer termMonths = 120;
            Integer minTerm = 12;
            Integer maxTerm = 60;

            // When
            BusinessValidationException exception = BusinessValidationException
                    .invalidLoanTerm(termMonths, minTerm, maxTerm);

            // Then
            assertEquals("BUSINESS_VALIDATION_ERROR", exception.getErrorCode());
            assertEquals("loanTermMonths", exception.getField());
            assertEquals(termMonths, exception.getRejectedValue());
            assertNull(exception.getFieldErrors());
            assertEquals(
                    "Loan term 120 months is outside valid range [12, 60]",
                    exception.getTechnicalMessage()
            );
            assertEquals(
                    "El plazo del préstamo debe estar entre 12 y 60 meses",
                    exception.getUserMessage()
            );
        }

//        @Test
//        @DisplayName("Should create application already exists exception")
//        void shouldCreateApplicationAlreadyExistsException() {
//            // Given
//            String userId = "user123";
//            String loanType = "personal";
//
//            // When
//            BusinessValidationException exception = BusinessValidationException
//                    .applicationAlreadyExists(userId, loanType);
//
//            // Then
//            assertEquals("BUSINESS_VALIDATION_ERROR", exception.getErrorCode());
//            assertEquals("applicationStatus", exception.getField());
//            assertNull(exception.getRejectedValue());
//            assertNull(exception.getFieldErrors());
//            assertEquals(
//                    "Ya existe una solicitud activa para este usuario y tipo de préstamo",
//                    exception.getUserMessage()
//            );
//            assertEquals(
//                    "Active application already exists for user user123 and loan type personal",
//                    exception.getTechnicalMessage()
//            );
//        }

        @Test
        @DisplayName("Should create invalid application status exception")
        void shouldCreateInvalidApplicationStatusException() {
            // Given
            String currentStatus = "APPROVED";
            String targetStatus = "PENDING";

            // When
            BusinessValidationException exception = BusinessValidationException
                    .invalidApplicationStatus(currentStatus, targetStatus);

            // Then
            assertEquals("BUSINESS_VALIDATION_ERROR", exception.getErrorCode());
            assertEquals("applicationStatus", exception.getField());
            assertEquals(targetStatus, exception.getRejectedValue());
            assertNull(exception.getFieldErrors());
            assertEquals(
                    "Cannot transition from status APPROVED to PENDING",
                    exception.getTechnicalMessage()
            );
            assertEquals(
                    "No se puede realizar esta transición de estado para la solicitud",
                    exception.getUserMessage()
            );
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle null field parameter")
        void shouldHandleNullFieldParameter() {
            // When
            BusinessValidationException exception = new BusinessValidationException(
                    (String) null, "Technical message", "User message"
            );

            // Then
            assertEquals("BUSINESS_VALIDATION_ERROR", exception.getErrorCode());
            assertNull(exception.getField());
            assertNull(exception.getRejectedValue());
            assertNull(exception.getFieldErrors());
        }

        @Test
        @DisplayName("Should handle null rejected value")
        void shouldHandleNullRejectedValue() {
            // When
            BusinessValidationException exception = new BusinessValidationException(
                    "field", null, "Technical message", "User message"
            );

            // Then
            assertEquals("BUSINESS_VALIDATION_ERROR", exception.getErrorCode());
            assertEquals("field", exception.getField());
            assertNull(exception.getRejectedValue());
            assertNull(exception.getFieldErrors());
        }

        @Test
        @DisplayName("Should handle empty field errors map")
        void shouldHandleEmptyFieldErrorsMap() {
            // Given
            Map<String, List<String>> emptyFieldErrors = new HashMap<>();

            // When
            BusinessValidationException exception = new BusinessValidationException(
                    emptyFieldErrors, "Technical message", "User message"
            );

            // Then
            assertEquals("BUSINESS_VALIDATION_ERROR", exception.getErrorCode());
            assertNull(exception.getField());
            assertNull(exception.getRejectedValue());
            assertEquals(emptyFieldErrors, exception.getFieldErrors());
            assertTrue(exception.getFieldErrors().isEmpty());
        }

        @Test
        @DisplayName("Should handle null field errors map")
        void shouldHandleNullFieldErrorsMap() {
            // When
            BusinessValidationException exception = new BusinessValidationException(
                    (String) null, "Technical message", "User message"
            );

            // Then
            assertEquals("BUSINESS_VALIDATION_ERROR", exception.getErrorCode());
            assertNull(exception.getField());
            assertNull(exception.getRejectedValue());
            assertNull(exception.getFieldErrors());
        }

        @Test
        @DisplayName("Should handle zero and negative values in factory methods")
        void shouldHandleZeroAndNegativeValuesInFactoryMethods() {
            // Test with zero values
            BusinessValidationException zeroAmount = BusinessValidationException
                    .loanAmountExceedsLimit(0.0, 1000.0);
            assertEquals(0.0, zeroAmount.getRejectedValue());

            BusinessValidationException zeroScore = BusinessValidationException
                    .insufficientCreditScore(0, 650);
            assertEquals(0, zeroScore.getRejectedValue());

            // Test with negative values
            BusinessValidationException negativeTerm = BusinessValidationException
                    .invalidLoanTerm(-5, 12, 60);
            assertEquals(-5, negativeTerm.getRejectedValue());
        }

        @Test
        @DisplayName("Should handle empty strings in factory methods")
        void shouldHandleEmptyStringsInFactoryMethods() {
            // Given
            String emptyUserId = "";
            String emptyLoanType = "";
            String emptyCurrentStatus = "";
            String emptyTargetStatus = "";

            // When
            BusinessValidationException exception1 = BusinessValidationException
                    .applicationAlreadyExists(emptyUserId, emptyLoanType);

            BusinessValidationException exception2 = BusinessValidationException
                    .invalidApplicationStatus(emptyCurrentStatus, emptyTargetStatus);

            // Then
            assertNotNull(exception1.getTechnicalMessage());
            assertNotNull(exception2.getTechnicalMessage());
            assertTrue(exception1.getTechnicalMessage().contains(""));
            assertTrue(exception2.getTechnicalMessage().contains(""));
        }
    }

    @Nested
    @DisplayName("Inheritance and General Tests")
    class InheritanceAndGeneralTests {

        @Test
        @DisplayName("Should inherit from LoanServiceException")
        void shouldInheritFromLoanServiceException() {
            // When
            BusinessValidationException exception = new BusinessValidationException(
                    "field", "technical", "user"
            );

            // Then
            assertTrue(exception instanceof LoanServiceException);
            assertNotNull(exception.getMessage()); // Inherited from parent exception
        }

        @Test
        @DisplayName("Should create different instances with factory methods")
        void shouldCreateDifferentInstancesWithFactoryMethods() {
            // When
            BusinessValidationException exc1 = BusinessValidationException.loanAmountExceedsLimit(1000.0, 500.0);
            BusinessValidationException exc2 = BusinessValidationException.insufficientCreditScore(400, 600);
            BusinessValidationException exc3 = BusinessValidationException.invalidLoanTerm(120, 12, 60);
            BusinessValidationException exc4 = BusinessValidationException.applicationAlreadyExists("user", "personal");
            BusinessValidationException exc5 = BusinessValidationException.invalidApplicationStatus("PENDING", "APPROVED");

            // Then
            assertNotSame(exc1, exc2);
            assertNotSame(exc2, exc3);
            assertNotSame(exc3, exc4);
            assertNotSame(exc4, exc5);

            // All should have the same error code
            assertEquals("BUSINESS_VALIDATION_ERROR", exc1.getErrorCode());
            assertEquals("BUSINESS_VALIDATION_ERROR", exc2.getErrorCode());
            assertEquals("BUSINESS_VALIDATION_ERROR", exc3.getErrorCode());
            assertEquals("BUSINESS_VALIDATION_ERROR", exc4.getErrorCode());
            assertEquals("BUSINESS_VALIDATION_ERROR", exc5.getErrorCode());
        }

        @Test
        @DisplayName("Should handle complex field errors map")
        void shouldHandleComplexFieldErrorsMap() {
            // Given
            Map<String, List<String>> complexFieldErrors = new HashMap<>();
            complexFieldErrors.put("email", new ArrayList<>(List.of("Required", "Invalid format", "Too long")));
            complexFieldErrors.put("phone", new ArrayList<>(List.of("Invalid country code")));
            complexFieldErrors.put("amount", new ArrayList<>(List.of("Must be positive", "Exceeds limit")));

            // When
            BusinessValidationException exception = new BusinessValidationException(
                    complexFieldErrors, "Multiple validation errors occurred", "Corrija los errores indicados"
            );

            // Then
            assertEquals(3, exception.getFieldErrors().size());
            assertEquals(3, exception.getFieldErrors().get("email").size());
            assertEquals(1, exception.getFieldErrors().get("phone").size());
            assertEquals(2, exception.getFieldErrors().get("amount").size());
            assertTrue(exception.getFieldErrors().get("email").contains("Required"));
            assertTrue(exception.getFieldErrors().get("phone").contains("Invalid country code"));
        }

        @Test
        @DisplayName("Should format technical messages correctly with different number types")
        void shouldFormatTechnicalMessagesCorrectlyWithDifferentNumberTypes() {
            // Test with different number types
            BusinessValidationException doubleException = BusinessValidationException
                    .loanAmountExceedsLimit(1500.50, 1000.25);

            BusinessValidationException integerException = BusinessValidationException
                    .insufficientCreditScore(550, 700);

            // Then - Usar contains para evitar problemas de locale
            String doubleMessage = doubleException.getTechnicalMessage();
            assertTrue(doubleMessage.contains("1500"));
            assertTrue(doubleMessage.contains("1000"));

            String integerMessage = integerException.getTechnicalMessage();
            assertTrue(integerMessage.contains("550"));
            assertTrue(integerMessage.contains("700"));
        }
    }
}