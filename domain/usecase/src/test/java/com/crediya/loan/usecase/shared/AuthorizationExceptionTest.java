package com.crediya.loan.usecase.shared;


import com.crediya.loan.usecase.shared.exception.AuthorizationException;
import com.crediya.loan.usecase.shared.exception.LoanServiceException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AuthorizationException Tests")
class AuthorizationExceptionTest {

    @Test
    @DisplayName("Should create AuthorizationException with all parameters")
    void shouldCreateAuthorizationExceptionWithAllParameters() {
        // Given
        String action = "read";
        String resource = "applications";
        String userId = "user123";
        String technicalMessage = "Technical error message";
        String userMessage = "User friendly message";

        // When
        AuthorizationException exception = new AuthorizationException(
                action, resource, userId, technicalMessage, userMessage
        );

        // Then
        assertEquals("AUTHORIZATION_ERROR", exception.getErrorCode());
        assertEquals(technicalMessage, exception.getTechnicalMessage());
        assertEquals(userMessage, exception.getUserMessage());
        assertEquals(action, exception.getAction());
        assertEquals(resource, exception.getResource());
        assertEquals(userId, exception.getUserId());
    }

    @Test
    @DisplayName("Should create access denied exception using factory method")
    void shouldCreateAccessDeniedException() {
        // Given
        String userId = "user456";
        String action = "delete";
        String resource = "loan_application";

        // When
        AuthorizationException exception = AuthorizationException.accessDenied(userId, action, resource);

        // Then
        assertEquals("AUTHORIZATION_ERROR", exception.getErrorCode());
        assertEquals(action, exception.getAction());
        assertEquals(resource, exception.getResource());
        assertEquals(userId, exception.getUserId());
        assertEquals(
                "User user456 is not authorized to delete on loan_application",
                exception.getTechnicalMessage()
        );
        assertEquals(
                "No tiene permisos suficientes para realizar esta operación",
                exception.getUserMessage()
        );
    }

    @Test
    @DisplayName("Should create invalid token exception using factory method")
    void shouldCreateInvalidTokenException() {
        // Given
        String technicalMessage = "JWT token has expired";

        // When
        AuthorizationException exception = AuthorizationException.invalidToken(technicalMessage);

        // Then
        assertEquals("AUTHORIZATION_ERROR", exception.getErrorCode());
        assertEquals("authenticate", exception.getAction());
        assertEquals("system", exception.getResource());
        assertEquals("unknown", exception.getUserId());
        assertEquals(technicalMessage, exception.getTechnicalMessage());
        assertEquals("Token de autenticación inválido o expirado", exception.getUserMessage());
    }

    @Test
    @DisplayName("Should create application ownership exception using factory method")
    void shouldCreateApplicationOwnershipException() {
        // Given
        String userId = "user789";
        String applicationId = "app123";

        // When
        AuthorizationException exception = AuthorizationException.applicationOwnership(userId, applicationId);

        // Then
        assertEquals("AUTHORIZATION_ERROR", exception.getErrorCode());
        assertEquals("access", exception.getAction());
        assertEquals("application", exception.getResource());
        assertEquals(userId, exception.getUserId());
        assertEquals(
                "User user789 does not own application app123",
                exception.getTechnicalMessage()
        );
        assertEquals(
                "Solo puede acceder a sus propias solicitudes de préstamo",
                exception.getUserMessage()
        );
    }

    @Test
    @DisplayName("Should create admin required exception using factory method")
    void shouldCreateAdminRequiredException() {
        // Given
        String userId = "regularUser";
        String action = "approve_loan";

        // When
        AuthorizationException exception = AuthorizationException.adminRequired(userId, action);

        // Then
        assertEquals("AUTHORIZATION_ERROR", exception.getErrorCode());
        assertEquals(action, exception.getAction());
        assertEquals("admin_resource", exception.getResource());
        assertEquals(userId, exception.getUserId());
        assertEquals(
                "Admin privileges required for action approve_loan by user regularUser",
                exception.getTechnicalMessage()
        );
        assertEquals(
                "Esta operación requiere privilegios de administrador",
                exception.getUserMessage()
        );
    }

    @Test
    @DisplayName("Should create session expired exception using factory method")
    void shouldCreateSessionExpiredException() {
        // When
        AuthorizationException exception = AuthorizationException.sessionExpired();

        // Then
        assertEquals("AUTHORIZATION_ERROR", exception.getErrorCode());
        assertEquals("authenticate", exception.getAction());
        assertEquals("system", exception.getResource());
        assertEquals("unknown", exception.getUserId());
        assertEquals("User session has expired", exception.getTechnicalMessage());
        assertEquals("Su sesión ha expirado. Por favor inicie sesión nuevamente.", exception.getUserMessage());
    }

    @Test
    @DisplayName("Should handle null parameters gracefully")
    void shouldHandleNullParametersGracefully() {
        // When
        AuthorizationException exception = new AuthorizationException(
                null, null, null, "Technical message", "User message"
        );

        // Then
        assertEquals("AUTHORIZATION_ERROR", exception.getErrorCode());
        assertNull(exception.getAction());
        assertNull(exception.getResource());
        assertNull(exception.getUserId());
        assertEquals("Technical message", exception.getTechnicalMessage());
        assertEquals("User message", exception.getUserMessage());
    }

    @Test
    @DisplayName("Should handle empty string parameters")
    void shouldHandleEmptyStringParameters() {
        // Given
        String emptyString = "";

        // When
        AuthorizationException exception = new AuthorizationException(
                emptyString, emptyString, emptyString, "Technical message", "User message"
        );

        // Then
        assertEquals("AUTHORIZATION_ERROR", exception.getErrorCode());
        assertEquals("", exception.getAction());
        assertEquals("", exception.getResource());
        assertEquals("", exception.getUserId());
        assertEquals("Technical message", exception.getTechnicalMessage());
        assertEquals("User message", exception.getUserMessage());
    }

    @Test
    @DisplayName("Should inherit from LoanServiceException")
    void shouldInheritFromLoanServiceException() {
        // When
        AuthorizationException exception = AuthorizationException.sessionExpired();

        // Then
        assertTrue(exception instanceof LoanServiceException);
        assertNotNull(exception.getMessage()); // Inherited from parent exception
    }

    @Test
    @DisplayName("Should create different instances with factory methods")
    void shouldCreateDifferentInstancesWithFactoryMethods() {
        // When
        AuthorizationException accessDenied = AuthorizationException.accessDenied("user1", "read", "data");
        AuthorizationException invalidToken = AuthorizationException.invalidToken("Invalid JWT");
        AuthorizationException ownership = AuthorizationException.applicationOwnership("user2", "app1");
        AuthorizationException adminRequired = AuthorizationException.adminRequired("user3", "delete");
        AuthorizationException sessionExpired = AuthorizationException.sessionExpired();

        // Then
        assertNotSame(accessDenied, invalidToken);
        assertNotSame(invalidToken, ownership);
        assertNotSame(ownership, adminRequired);
        assertNotSame(adminRequired, sessionExpired);

        // All should have the same error code
        assertEquals("AUTHORIZATION_ERROR", accessDenied.getErrorCode());
        assertEquals("AUTHORIZATION_ERROR", invalidToken.getErrorCode());
        assertEquals("AUTHORIZATION_ERROR", ownership.getErrorCode());
        assertEquals("AUTHORIZATION_ERROR", adminRequired.getErrorCode());
        assertEquals("AUTHORIZATION_ERROR", sessionExpired.getErrorCode());
    }

    @Test
    @DisplayName("Should format technical messages correctly in factory methods")
    void shouldFormatTechnicalMessagesCorrectly() {
        // Given
        String userId = "testUser";
        String action = "update";
        String resource = "profile";
        String applicationId = "app456";

        // When
        AuthorizationException accessDenied = AuthorizationException.accessDenied(userId, action, resource);
        AuthorizationException ownership = AuthorizationException.applicationOwnership(userId, applicationId);
        AuthorizationException adminRequired = AuthorizationException.adminRequired(userId, action);

        // Then
        assertTrue(accessDenied.getTechnicalMessage().contains(userId));
        assertTrue(accessDenied.getTechnicalMessage().contains(action));
        assertTrue(accessDenied.getTechnicalMessage().contains(resource));

        assertTrue(ownership.getTechnicalMessage().contains(userId));
        assertTrue(ownership.getTechnicalMessage().contains(applicationId));

        assertTrue(adminRequired.getTechnicalMessage().contains(userId));
        assertTrue(adminRequired.getTechnicalMessage().contains(action));
    }
}