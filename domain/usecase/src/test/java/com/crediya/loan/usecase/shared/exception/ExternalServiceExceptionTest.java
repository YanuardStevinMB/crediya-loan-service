package com.crediya.loan.usecase.shared.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ExternalServiceException Tests")
class ExternalServiceExceptionTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create exception with service name and operation")
        void shouldCreateExceptionWithServiceNameAndOperation() {
            // Given
            String serviceName = "UserService";
            String operation = "getUserById";
            String technicalMessage = "Connection refused";
            String userMessage = "Service temporarily unavailable";

            // When
            ExternalServiceException exception = new ExternalServiceException(
                    serviceName, operation, technicalMessage, userMessage
            );

            // Then
            assertEquals("EXTERNAL_SERVICE_ERROR", exception.getErrorCode());
            assertEquals(serviceName, exception.getServiceName());
            assertEquals(operation, exception.getOperation());
            assertNull(exception.getStatusCode());
            assertEquals(technicalMessage, exception.getTechnicalMessage());
            assertEquals(userMessage, exception.getUserMessage());
            assertNull(exception.getCause());
        }

        @Test
        @DisplayName("Should create exception with status code")
        void shouldCreateExceptionWithStatusCode() {
            // Given
            String serviceName = "PaymentService";
            String operation = "processPayment";
            Integer statusCode = 500;
            String technicalMessage = "Internal server error";
            String userMessage = "Payment processing failed";

            // When
            ExternalServiceException exception = new ExternalServiceException(
                    serviceName, operation, statusCode, technicalMessage, userMessage
            );

            // Then
            assertEquals("EXTERNAL_SERVICE_ERROR", exception.getErrorCode());
            assertEquals(serviceName, exception.getServiceName());
            assertEquals(operation, exception.getOperation());
            assertEquals(statusCode, exception.getStatusCode());
            assertEquals(technicalMessage, exception.getTechnicalMessage());
            assertEquals(userMessage, exception.getUserMessage());
            assertNull(exception.getCause());
        }

        @Test
        @DisplayName("Should create exception with cause")
        void shouldCreateExceptionWithCause() {
            // Given
            String serviceName = "NotificationService";
            String operation = "sendEmail";
            String technicalMessage = "SMTP connection failed";
            String userMessage = "Email could not be sent";
            RuntimeException cause = new RuntimeException("Connection timeout");

            // When
            ExternalServiceException exception = new ExternalServiceException(
                    serviceName, operation, technicalMessage, userMessage, cause
            );

            // Then
            assertEquals("EXTERNAL_SERVICE_ERROR", exception.getErrorCode());
            assertEquals(serviceName, exception.getServiceName());
            assertEquals(operation, exception.getOperation());
            assertNull(exception.getStatusCode());
            assertEquals(technicalMessage, exception.getTechnicalMessage());
            assertEquals(userMessage, exception.getUserMessage());
            assertEquals(cause, exception.getCause());
        }
    }

    @Nested
    @DisplayName("Factory Methods Tests")
    class FactoryMethodsTests {

        @Test
        @DisplayName("Should create credit score service exception")
        void shouldCreateCreditScoreServiceException() {
            // Given
            String operation = "getCreditScore";
            String technicalMessage = "API rate limit exceeded";
            RuntimeException cause = new RuntimeException("HTTP 429");

            // When
            ExternalServiceException exception = ExternalServiceException
                    .creditScoreService(operation, technicalMessage, cause);

            // Then
            assertEquals("EXTERNAL_SERVICE_ERROR", exception.getErrorCode());
            assertEquals("CreditScoreService", exception.getServiceName());
            assertEquals(operation, exception.getOperation());
            assertNull(exception.getStatusCode());
            assertEquals(technicalMessage, exception.getTechnicalMessage());
            assertEquals(
                    "Error temporal al consultar información crediticia. Por favor intente más tarde.",
                    exception.getUserMessage()
            );
            assertEquals(cause, exception.getCause());
        }

        @Test
        @DisplayName("Should create user service exception")
        void shouldCreateUserServiceException() {
            // Given
            String operation = "validateUser";
            Integer statusCode = 404;
            String technicalMessage = "User not found in external system";

            // When
            ExternalServiceException exception = ExternalServiceException
                    .userService(operation, statusCode, technicalMessage);

            // Then
            assertEquals("EXTERNAL_SERVICE_ERROR", exception.getErrorCode());
            assertEquals("UserService", exception.getServiceName());
            assertEquals(operation, exception.getOperation());
            assertEquals(statusCode, exception.getStatusCode());
            assertEquals(technicalMessage, exception.getTechnicalMessage());
            assertEquals(
                    "Error al obtener información del usuario. Por favor intente más tarde.",
                    exception.getUserMessage()
            );
            assertNull(exception.getCause());
        }

        @Test
        @DisplayName("Should create notification service exception")
        void shouldCreateNotificationServiceException() {
            // Given
            String operation = "sendSMS";
            String technicalMessage = "SMS provider service unavailable";
            Exception cause = new Exception("Service timeout");

            // When
            ExternalServiceException exception = ExternalServiceException
                    .notificationService(operation, technicalMessage, cause);

            // Then
            assertEquals("EXTERNAL_SERVICE_ERROR", exception.getErrorCode());
            assertEquals("NotificationService", exception.getServiceName());
            assertEquals(operation, exception.getOperation());
            assertNull(exception.getStatusCode());
            assertEquals(technicalMessage, exception.getTechnicalMessage());
            assertEquals(
                    "La solicitud se procesó correctamente, pero hubo un error al enviar notificaciones.",
                    exception.getUserMessage()
            );
            assertEquals(cause, exception.getCause());
        }

        @Test
        @DisplayName("Should create reporting service exception")
        void shouldCreateReportingServiceException() {
            // Given
            String operation = "generateReport";
            String technicalMessage = "Report generation service crashed";
            Throwable cause = new IllegalStateException("Invalid state");

            // When
            ExternalServiceException exception = ExternalServiceException
                    .reportingService(operation, technicalMessage, cause);

            // Then
            assertEquals("EXTERNAL_SERVICE_ERROR", exception.getErrorCode());
            assertEquals("ReportingService", exception.getServiceName());
            assertEquals(operation, exception.getOperation());
            assertNull(exception.getStatusCode());
            assertEquals(technicalMessage, exception.getTechnicalMessage());
            assertEquals(
                    "Error temporal al generar reportes. Por favor intente más tarde.",
                    exception.getUserMessage()
            );
            assertEquals(cause, exception.getCause());
        }

        @Test
        @DisplayName("Should create database timeout exception")
        void shouldCreateDatabaseTimeoutException() {
            // Given
            String operation = "selectUserApplications";
            Throwable cause = new RuntimeException("Query timeout after 30 seconds");

            // When
            ExternalServiceException exception = ExternalServiceException
                    .databaseTimeout(operation, cause);

            // Then
            assertEquals("EXTERNAL_SERVICE_ERROR", exception.getErrorCode());
            assertEquals("Database", exception.getServiceName());
            assertEquals(operation, exception.getOperation());
            assertNull(exception.getStatusCode());
            assertEquals(
                    "Database operation timeout: " + operation,
                    exception.getTechnicalMessage()
            );
            assertEquals(
                    "El sistema está experimentando alta demanda. Por favor intente más tarde.",
                    exception.getUserMessage()
            );
            assertEquals(cause, exception.getCause());
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle null service name")
        void shouldHandleNullServiceName() {
            // When
            ExternalServiceException exception = new ExternalServiceException(
                    null, "operation", "technical", "user"
            );

            // Then
            assertEquals("EXTERNAL_SERVICE_ERROR", exception.getErrorCode());
            assertNull(exception.getServiceName());
            assertEquals("operation", exception.getOperation());
            assertNull(exception.getStatusCode());
        }

        @Test
        @DisplayName("Should handle null operation")
        void shouldHandleNullOperation() {
            // When
            ExternalServiceException exception = new ExternalServiceException(
                    "Service", null, "technical", "user"
            );

            // Then
            assertEquals("EXTERNAL_SERVICE_ERROR", exception.getErrorCode());
            assertEquals("Service", exception.getServiceName());
            assertNull(exception.getOperation());
            assertNull(exception.getStatusCode());
        }

        @Test
        @DisplayName("Should handle null status code")
        void shouldHandleNullStatusCode() {
            // When
            ExternalServiceException exception = new ExternalServiceException(
                    "Service", "operation", null, "technical", "user"
            );

            // Then
            assertEquals("EXTERNAL_SERVICE_ERROR", exception.getErrorCode());
            assertEquals("Service", exception.getServiceName());
            assertEquals("operation", exception.getOperation());
            assertNull(exception.getStatusCode());
        }

        @Test
        @DisplayName("Should handle null cause")
        void shouldHandleNullCause() {
            // When
            ExternalServiceException exception = new ExternalServiceException(
                    "Service", "operation", "technical", "user", null
            );

            // Then
            assertEquals("EXTERNAL_SERVICE_ERROR", exception.getErrorCode());
            assertEquals("Service", exception.getServiceName());
            assertEquals("operation", exception.getOperation());
            assertNull(exception.getStatusCode());
            assertNull(exception.getCause());
        }

        @Test
        @DisplayName("Should handle empty strings")
        void shouldHandleEmptyStrings() {
            // Given
            String emptyString = "";

            // When
            ExternalServiceException exception = new ExternalServiceException(
                    emptyString, emptyString, "technical", "user"
            );

            // Then
            assertEquals("EXTERNAL_SERVICE_ERROR", exception.getErrorCode());
            assertEquals("", exception.getServiceName());
            assertEquals("", exception.getOperation());
            assertNull(exception.getStatusCode());
        }

        @Test
        @DisplayName("Should handle zero and negative status codes")
        void shouldHandleZeroAndNegativeStatusCodes() {
            // Test zero status code
            ExternalServiceException zeroStatusException = new ExternalServiceException(
                    "Service", "operation", 0, "technical", "user"
            );
            assertEquals(0, zeroStatusException.getStatusCode());

            // Test negative status code
            ExternalServiceException negativeStatusException = new ExternalServiceException(
                    "Service", "operation", -1, "technical", "user"
            );
            assertEquals(-1, negativeStatusException.getStatusCode());
        }

        @Test
        @DisplayName("Should handle factory methods with null parameters")
        void shouldHandleFactoryMethodsWithNullParameters() {
            // Test creditScoreService with nulls
            ExternalServiceException creditException = ExternalServiceException
                    .creditScoreService(null, null, null);
            assertEquals("CreditScoreService", creditException.getServiceName());
            assertNull(creditException.getOperation());
            assertNull(creditException.getTechnicalMessage());
            assertNull(creditException.getCause());

            // Test userService with nulls
            ExternalServiceException userException = ExternalServiceException
                    .userService(null, null, null);
            assertEquals("UserService", userException.getServiceName());
            assertNull(userException.getOperation());
            assertNull(userException.getStatusCode());
            assertNull(userException.getTechnicalMessage());
        }
    }

    @Nested
    @DisplayName("Inheritance and General Tests")
    class InheritanceAndGeneralTests {

        @Test
        @DisplayName("Should inherit from LoanServiceException")
        void shouldInheritFromLoanServiceException() {
            // When
            ExternalServiceException exception = new ExternalServiceException(
                    "Service", "operation", "technical", "user"
            );

            // Then
            assertTrue(exception instanceof LoanServiceException);
            assertNotNull(exception.getMessage()); // Inherited from parent
        }

        @Test
        @DisplayName("Should create different instances with factory methods")
        void shouldCreateDifferentInstancesWithFactoryMethods() {
            // When
            ExternalServiceException creditScore = ExternalServiceException
                    .creditScoreService("getScore", "Error", null);
            ExternalServiceException userService = ExternalServiceException
                    .userService("getUser", 500, "Error");
            ExternalServiceException notification = ExternalServiceException
                    .notificationService("send", "Error", null);
            ExternalServiceException reporting = ExternalServiceException
                    .reportingService("generate", "Error", null);
            ExternalServiceException database = ExternalServiceException
                    .databaseTimeout("select", null);

            // Then
            assertNotSame(creditScore, userService);
            assertNotSame(userService, notification);
            assertNotSame(notification, reporting);
            assertNotSame(reporting, database);

            // All should have the same error code
            assertEquals("EXTERNAL_SERVICE_ERROR", creditScore.getErrorCode());
            assertEquals("EXTERNAL_SERVICE_ERROR", userService.getErrorCode());
            assertEquals("EXTERNAL_SERVICE_ERROR", notification.getErrorCode());
            assertEquals("EXTERNAL_SERVICE_ERROR", reporting.getErrorCode());
            assertEquals("EXTERNAL_SERVICE_ERROR", database.getErrorCode());
        }

        @Test
        @DisplayName("Should maintain service names in factory methods")
        void shouldMaintainServiceNamesInFactoryMethods() {
            // When
            ExternalServiceException creditScore = ExternalServiceException
                    .creditScoreService("op", "msg", null);
            ExternalServiceException userService = ExternalServiceException
                    .userService("op", 200, "msg");
            ExternalServiceException notification = ExternalServiceException
                    .notificationService("op", "msg", null);
            ExternalServiceException reporting = ExternalServiceException
                    .reportingService("op", "msg", null);
            ExternalServiceException database = ExternalServiceException
                    .databaseTimeout("op", null);

            // Then
            assertEquals("CreditScoreService", creditScore.getServiceName());
            assertEquals("UserService", userService.getServiceName());
            assertEquals("NotificationService", notification.getServiceName());
            assertEquals("ReportingService", reporting.getServiceName());
            assertEquals("Database", database.getServiceName());
        }

        @Test
        @DisplayName("Should handle cause chain correctly")
        void shouldHandleCauseChainCorrectly() {
            // Given
            RuntimeException rootCause = new RuntimeException("Root cause");
            IllegalStateException intermediateCause = new IllegalStateException("Intermediate", rootCause);

            // When
            ExternalServiceException exception = new ExternalServiceException(
                    "Service", "operation", "technical", "user", intermediateCause
            );

            // Then
            assertEquals(intermediateCause, exception.getCause());
            assertEquals(rootCause, exception.getCause().getCause());
        }

        @Test
        @DisplayName("Should format database timeout message correctly")
        void shouldFormatDatabaseTimeoutMessageCorrectly() {
            // Given
            String operation = "complex_query_operation";

            // When
            ExternalServiceException exception = ExternalServiceException
                    .databaseTimeout(operation, null);

            // Then
            assertEquals("Database operation timeout: " + operation, exception.getTechnicalMessage());
            assertTrue(exception.getTechnicalMessage().contains(operation));
        }

        @Test
        @DisplayName("Should handle different HTTP status codes")
        void shouldHandleDifferentHttpStatusCodes() {
            // Test common HTTP status codes
            int[] statusCodes = {200, 400, 401, 403, 404, 500, 502, 503, 504};

            for (int statusCode : statusCodes) {
                ExternalServiceException exception = ExternalServiceException
                        .userService("operation", statusCode, "Error " + statusCode);

                assertEquals(statusCode, exception.getStatusCode());
                assertEquals("UserService", exception.getServiceName());
            }
        }
    }
}