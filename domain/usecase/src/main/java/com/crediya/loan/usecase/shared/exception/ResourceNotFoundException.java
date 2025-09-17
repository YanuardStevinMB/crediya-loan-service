package com.crediya.loan.usecase.shared.exception;

/**
 * Excepción para casos donde un recurso requerido no existe.
 * Típicamente resulta en respuestas HTTP 404.
 */
public class ResourceNotFoundException extends LoanServiceException {
    
    private static final String ERROR_CODE = "RESOURCE_NOT_FOUND";
    
    private final String resourceType;
    private final String resourceId;
    
    public ResourceNotFoundException(String resourceType, String resourceId, String technicalMessage, String userMessage) {
        super(ERROR_CODE, technicalMessage, userMessage);
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }
    
    // Métodos de conveniencia para recursos específicos del dominio
    public static ResourceNotFoundException application(String applicationId) {
        return new ResourceNotFoundException(
            "Application",
            applicationId,
            String.format("Application with ID %s not found", applicationId),
            "La solicitud de préstamo no fue encontrada"
        );
    }
    
    public static ResourceNotFoundException user(String userId) {
        return new ResourceNotFoundException(
            "User",
            userId,
            String.format("User with ID %s not found", userId),
            "El usuario no fue encontrado en el sistema"
        );
    }
    
    public static ResourceNotFoundException loanType(String loanTypeId) {
        return new ResourceNotFoundException(
            "LoanType",
            loanTypeId,
            String.format("Loan type with ID %s not found", loanTypeId),
            "El tipo de préstamo solicitado no existe"
        );
    }
    
    public static ResourceNotFoundException borrowingCapacity(String userId) {
        return new ResourceNotFoundException(
            "BorrowingCapacity",
            userId,
            String.format("Borrowing capacity not found for user %s", userId),
            "No se encontró información de capacidad de endeudamiento para este usuario"
        );
    }
    
    public static ResourceNotFoundException state(String stateId) {
        return new ResourceNotFoundException(
            "State",
            stateId,
            String.format("State with ID %s not found", stateId),
            "El estado especificado no existe en el sistema"
        );
    }
    
    // Getters
    public String getResourceType() {
        return resourceType;
    }
    
    public String getResourceId() {
        return resourceId;
    }
}