package com.crediya.loan.usecase.shared.exception;

/**
 * Excepción para errores en integraciones con servicios externos.
 * Se usa cuando hay problemas de comunicación o errores de servicios externos.
 */
public class ExternalServiceException extends LoanServiceException {
    
    private static final String ERROR_CODE = "EXTERNAL_SERVICE_ERROR";
    
    private final String serviceName;
    private final String operation;
    private final Integer statusCode;
    
    public ExternalServiceException(String serviceName, String operation, String technicalMessage, String userMessage) {
        super(ERROR_CODE, technicalMessage, userMessage);
        this.serviceName = serviceName;
        this.operation = operation;
        this.statusCode = null;
    }
    
    public ExternalServiceException(String serviceName, String operation, Integer statusCode, String technicalMessage, String userMessage) {
        super(ERROR_CODE, technicalMessage, userMessage);
        this.serviceName = serviceName;
        this.operation = operation;
        this.statusCode = statusCode;
    }
    
    public ExternalServiceException(String serviceName, String operation, String technicalMessage, String userMessage, Throwable cause) {
        super(ERROR_CODE, technicalMessage, userMessage, cause);
        this.serviceName = serviceName;
        this.operation = operation;
        this.statusCode = null;
    }
    
    // Métodos de conveniencia para servicios específicos
    public static ExternalServiceException creditScoreService(String operation, String technicalMessage, Throwable cause) {
        return new ExternalServiceException(
            "CreditScoreService",
            operation,
            technicalMessage,
            "Error temporal al consultar información crediticia. Por favor intente más tarde.",
            cause
        );
    }
    
    public static ExternalServiceException userService(String operation, Integer statusCode, String technicalMessage) {
        return new ExternalServiceException(
            "UserService",
            operation,
            statusCode,
            technicalMessage,
            "Error al obtener información del usuario. Por favor intente más tarde."
        );
    }
    
    public static ExternalServiceException notificationService(String operation, String technicalMessage, Throwable cause) {
        return new ExternalServiceException(
            "NotificationService",
            operation,
            technicalMessage,
            "La solicitud se procesó correctamente, pero hubo un error al enviar notificaciones.",
            cause
        );
    }
    
    public static ExternalServiceException reportingService(String operation, String technicalMessage, Throwable cause) {
        return new ExternalServiceException(
            "ReportingService",
            operation,
            technicalMessage,
            "Error temporal al generar reportes. Por favor intente más tarde.",
            cause
        );
    }
    
    public static ExternalServiceException databaseTimeout(String operation, Throwable cause) {
        return new ExternalServiceException(
            "Database",
            operation,
            "Database operation timeout: " + operation,
            "El sistema está experimentando alta demanda. Por favor intente más tarde.",
            cause
        );
    }
    
    // Getters
    public String getServiceName() {
        return serviceName;
    }
    
    public String getOperation() {
        return operation;
    }
    
    public Integer getStatusCode() {
        return statusCode;
    }
}