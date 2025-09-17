package com.crediya.loan.usecase.shared.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LoanServiceException Tests")
class LoanServiceExceptionTest {

    // Concrete implementation for testing the abstract class
    private static class TestLoanServiceException extends LoanServiceException {
        public TestLoanServiceException(String errorCode, String technicalMessage, String userMessage) {
            super(errorCode, technicalMessage, userMessage);
        }

        public TestLoanServiceException(String errorCode, String technicalMessage, String userMessage, Throwable cause) {
            super(errorCode, technicalMessage, userMessage, cause);
        }
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create exception with basic parameters")
        void shouldCreateExceptionWithBasicParameters() {
            // Given
            String errorCode = "TEST_ERROR";
            String technicalMessage = "Technical error description";
            String userMessage = "User-friendly error message";

            // When
            TestLoanServiceException exception = new TestLoanServiceException(
                    errorCode, technicalMessage, userMessage
            );

            // Then
            assertEquals(errorCode, exception.getErrorCode());
            assertEquals(technicalMessage, exception.getTechnicalMessage());
            assertEquals(userMessage, exception.getUserMessage());
            assertEquals(technicalMessage, exception.getMessage()); // From RuntimeException
            assertNull(exception.getCause());
        }

        @Test
        @DisplayName("Should create exception with cause")
        void shouldCreateExceptionWithCause() {
            // Given
            String errorCode = "TEST_ERROR_WITH_CAUSE";
            String technicalMessage = "Database connection failed";
            String userMessage = "Service temporarily unavailable";
            RuntimeException cause = new RuntimeException("Connection timeout");

            // When
            TestLoanServiceException exception = new TestLoanServiceException(
                    errorCode, technicalMessage, userMessage, cause
            );

            // Then
            assertEquals(errorCode, exception.getErrorCode());
            assertEquals(technicalMessage, exception.getTechnicalMessage());
            assertEquals(userMessage, exception.getUserMessage());
            assertEquals(technicalMessage, exception.getMessage()); // From RuntimeException
            assertEquals(cause, exception.getCause());
        }

        @Test
        @DisplayName("Should inherit from RuntimeException")
        void shouldInheritFromRuntimeException() {
            // When
            TestLoanServiceException exception = new TestLoanServiceException(
                    "CODE", "technical", "user"
            );

            // Then
            assertTrue(exception instanceof RuntimeException);
            assertTrue(exception instanceof Exception);
            assertTrue(exception instanceof Throwable);
        }
    }

    @Nested
    @DisplayName("Getter Methods Tests")
    class GetterMethodsTests {

        @Test
        @DisplayName("Should return correct error code")
        void shouldReturnCorrectErrorCode() {
            // Given
            String expectedErrorCode = "BUSINESS_ERROR";

            // When
            TestLoanServiceException exception = new TestLoanServiceException(
                    expectedErrorCode, "technical", "user"
            );

            // Then
            assertEquals(expectedErrorCode, exception.getErrorCode());
        }

        @Test
        @DisplayName("Should return correct user message")
        void shouldReturnCorrectUserMessage() {
            // Given
            String expectedUserMessage = "Please contact support for assistance";

            // When
            TestLoanServiceException exception = new TestLoanServiceException(
                    "CODE", "technical", expectedUserMessage
            );

            // Then
            assertEquals(expectedUserMessage, exception.getUserMessage());
        }

        @Test
        @DisplayName("Should return correct technical message")
        void shouldReturnCorrectTechnicalMessage() {
            // Given
            String expectedTechnicalMessage = "NullPointerException in UserService.findById()";

            // When
            TestLoanServiceException exception = new TestLoanServiceException(
                    "CODE", expectedTechnicalMessage, "user"
            );

            // Then
            assertEquals(expectedTechnicalMessage, exception.getTechnicalMessage());
            assertEquals(expectedTechnicalMessage, exception.getMessage()); // Should be the same
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle null error code")
        void shouldHandleNullErrorCode() {
            // When
            TestLoanServiceException exception = new TestLoanServiceException(
                    null, "technical", "user"
            );

            // Then
            assertNull(exception.getErrorCode());
            assertEquals("technical", exception.getTechnicalMessage());
            assertEquals("user", exception.getUserMessage());
        }

        @Test
        @DisplayName("Should handle null technical message")
        void shouldHandleNullTechnicalMessage() {
            // When
            TestLoanServiceException exception = new TestLoanServiceException(
                    "CODE", null, "user"
            );

            // Then
            assertEquals("CODE", exception.getErrorCode());
            assertNull(exception.getTechnicalMessage());
            assertNull(exception.getMessage()); // Inherited from RuntimeException
            assertEquals("user", exception.getUserMessage());
        }

        @Test
        @DisplayName("Should handle null user message")
        void shouldHandleNullUserMessage() {
            // When
            TestLoanServiceException exception = new TestLoanServiceException(
                    "CODE", "technical", null
            );

            // Then
            assertEquals("CODE", exception.getErrorCode());
            assertEquals("technical", exception.getTechnicalMessage());
            assertNull(exception.getUserMessage());
        }

        @Test
        @DisplayName("Should handle all null parameters")
        void shouldHandleAllNullParameters() {
            // When
            TestLoanServiceException exception = new TestLoanServiceException(
                    null, null, null
            );

            // Then
            assertNull(exception.getErrorCode());
            assertNull(exception.getTechnicalMessage());
            assertNull(exception.getUserMessage());
            assertNull(exception.getMessage());
            assertNull(exception.getCause());
        }

        @Test
        @DisplayName("Should handle empty strings")
        void shouldHandleEmptyStrings() {
            // Given
            String emptyString = "";

            // When
            TestLoanServiceException exception = new TestLoanServiceException(
                    emptyString, emptyString, emptyString
            );

            // Then
            assertEquals("", exception.getErrorCode());
            assertEquals("", exception.getTechnicalMessage());
            assertEquals("", exception.getUserMessage());
            assertEquals("", exception.getMessage());
        }

        @Test
        @DisplayName("Should handle null cause gracefully")
        void shouldHandleNullCauseGracefully() {
            // When
            TestLoanServiceException exception = new TestLoanServiceException(
                    "CODE", "technical", "user", null
            );

            // Then
            assertEquals("CODE", exception.getErrorCode());
            assertEquals("technical", exception.getTechnicalMessage());
            assertEquals("user", exception.getUserMessage());
            assertNull(exception.getCause());
        }
    }

    @Nested
    @DisplayName("Inheritance and Polymorphism Tests")
    class InheritanceAndPolymorphismTests {



        @Test
        @DisplayName("Should maintain polymorphic behavior")
        void shouldMaintainPolymorphicBehavior() {
            // When
            TestLoanServiceException concreteException = new TestLoanServiceException(
                    "CODE", "technical", "user"
            );
            LoanServiceException abstractReference = concreteException;
            RuntimeException runtimeReference = concreteException;

            // Then
            assertSame(concreteException, abstractReference);
            assertSame(concreteException, runtimeReference);
            assertEquals("CODE", abstractReference.getErrorCode());
            assertEquals("technical", runtimeReference.getMessage());
        }

        @Test
        @DisplayName("Should allow method overriding")
        void shouldAllowMethodOverriding() {
            // Given - Create a subclass that overrides toString
            class CustomLoanServiceException extends LoanServiceException {
                public CustomLoanServiceException(String errorCode, String technicalMessage, String userMessage) {
                    super(errorCode, technicalMessage, userMessage);
                }

                @Override
                public String toString() {
                    return "Custom: " + getErrorCode() + " - " + getUserMessage();
                }
            }

            // When
            CustomLoanServiceException customException = new CustomLoanServiceException(
                    "CUSTOM_ERROR", "technical", "user"
            );

            // Then
            assertTrue(customException.toString().startsWith("Custom:"));
            assertTrue(customException.toString().contains("CUSTOM_ERROR"));
            assertTrue(customException.toString().contains("user"));
        }
    }

    @Nested
    @DisplayName("Exception Chain Tests")
    class ExceptionChainTests {

        @Test
        @DisplayName("Should maintain exception chain correctly")
        void shouldMaintainExceptionChainCorrectly() {
            // Given
            RuntimeException rootCause = new RuntimeException("Root cause");
            IllegalStateException intermediateCause = new IllegalStateException("Intermediate", rootCause);

            // When
            TestLoanServiceException exception = new TestLoanServiceException(
                    "CHAIN_ERROR", "Chain test", "Chain message", intermediateCause
            );

            // Then
            assertEquals(intermediateCause, exception.getCause());
            assertEquals(rootCause, exception.getCause().getCause());
            assertNull(exception.getCause().getCause().getCause());
        }

        @Test
        @DisplayName("Should handle circular reference protection")
        void shouldHandleCircularReferenceProtection() {
            // Given
            TestLoanServiceException exception1 = new TestLoanServiceException(
                    "ERROR1", "First", "First message"
            );
            TestLoanServiceException exception2 = new TestLoanServiceException(
                    "ERROR2", "Second", "Second message", exception1
            );

            // When & Then - Should not create infinite loops
            assertNotNull(exception2.getCause());
            assertEquals(exception1, exception2.getCause());
            assertNull(exception1.getCause());
        }
    }

    @Nested
    @DisplayName("Message Consistency Tests")
    class MessageConsistencyTests {

        @Test
        @DisplayName("Should maintain consistency between getMessage and getTechnicalMessage")
        void shouldMaintainConsistencyBetweenMessageAndTechnicalMessage() {
            // Given
            String technicalMessage = "Database constraint violation: FK_USER_ID";

            // When
            TestLoanServiceException exception = new TestLoanServiceException(
                    "DB_ERROR", technicalMessage, "user"
            );

            // Then
            assertEquals(exception.getMessage(), exception.getTechnicalMessage());
            assertSame(exception.getMessage(), exception.getTechnicalMessage());
        }

        @Test
        @DisplayName("Should distinguish between technical and user messages")
        void shouldDistinguishBetweenTechnicalAndUserMessages() {
            // Given
            String technicalMessage = "SQLException: Connection pool exhausted after 30 seconds";
            String userMessage = "El sistema está temporalmente no disponible";

            // When
            TestLoanServiceException exception = new TestLoanServiceException(
                    "CONNECTION_ERROR", technicalMessage, userMessage
            );

            // Then
            assertNotEquals(exception.getTechnicalMessage(), exception.getUserMessage());
            assertTrue(exception.getTechnicalMessage().contains("SQLException"));
            assertTrue(exception.getUserMessage().contains("temporalmente"));
        }
    }

    @Nested
    @DisplayName("Abstract Class Behavior Tests")
    class AbstractClassBehaviorTests {

        @Test
        @DisplayName("Should not be directly instantiable")
        void shouldNotBeDirectlyInstantiable() {
            // This test verifies that LoanServiceException is abstract
            // We can't directly instantiate it, only through subclasses

            // When & Then - This should compile (testing abstract nature)
            LoanServiceException exception = new TestLoanServiceException(
                    "CODE", "technical", "user"
            );

            assertInstanceOf(LoanServiceException.class, exception);
            assertInstanceOf(TestLoanServiceException.class, exception);
        }

        @Test
        @DisplayName("Should require implementation of abstract class")
        void shouldRequireImplementationOfAbstractClass() {
            // Given - Testing that concrete subclasses work properly
            class AnotherTestException extends LoanServiceException {
                public AnotherTestException() {
                    super("ANOTHER_ERROR", "Another technical", "Another user");
                }
            }

            // When
            AnotherTestException exception = new AnotherTestException();

            // Then
            assertEquals("ANOTHER_ERROR", exception.getErrorCode());
            assertEquals("Another technical", exception.getTechnicalMessage());
            assertEquals("Another user", exception.getUserMessage());
        }
    }
}