package com.crediya.loan.usecase.shared.exception;

/**
 * Excepción para errores de autorización y autenticación.
 * Se usa cuando un usuario no tiene permisos o su token es inválido.
 */
public class AuthorizationException extends LoanServiceException {
    
    private static final String ERROR_CODE = "AUTHORIZATION_ERROR";
    
    private final String action;
    private final String resource;
    private final String userId;
    
    public AuthorizationException(String action, String resource, String userId, String technicalMessage, String userMessage) {
        super(ERROR_CODE, technicalMessage, userMessage);
        this.action = action;
        this.resource = resource;
        this.userId = userId;
    }
    
    // Métodos de conveniencia para casos específicos de autorización
    public static AuthorizationException accessDenied(String userId, String action, String resource) {
        return new AuthorizationException(
            action,
            resource,
            userId,
            String.format("User %s is not authorized to %s on %s", userId, action, resource),
            "No tiene permisos suficientes para realizar esta operación"
        );
    }
    
    public static AuthorizationException invalidToken(String technicalMessage) {
        return new AuthorizationException(
            "authenticate",
            "system",
            "unknown",
            technicalMessage,
            "Token de autenticación inválido o expirado"
        );
    }
    
    public static AuthorizationException applicationOwnership(String userId, String applicationId) {
        return new AuthorizationException(
            "access",
            "application",
            userId,
            String.format("User %s does not own application %s", userId, applicationId),
            "Solo puede acceder a sus propias solicitudes de préstamo"
        );
    }
    
    public static AuthorizationException adminRequired(String userId, String action) {
        return new AuthorizationException(
            action,
            "admin_resource",
            userId,
            String.format("Admin privileges required for action %s by user %s", action, userId),
            "Esta operación requiere privilegios de administrador"
        );
    }
    
    public static AuthorizationException sessionExpired() {
        return new AuthorizationException(
            "authenticate",
            "system",
            "unknown",
            "User session has expired",
            "Su sesión ha expirado. Por favor inicie sesión nuevamente."
        );
    }
    
    // Getters
    public String getAction() {
        return action;
    }
    
    public String getResource() {
        return resource;
    }
    
    public String getUserId() {
        return userId;
    }
}