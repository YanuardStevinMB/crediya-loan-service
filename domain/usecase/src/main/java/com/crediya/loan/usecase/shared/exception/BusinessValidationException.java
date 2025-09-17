package com.crediya.loan.usecase.shared.exception;

import java.util.List;
import java.util.Map;

/**
 * Excepción para errores de validación de reglas de negocio.
 * Se usa cuando se violan reglas específicas del dominio de préstamos.
 */
public class BusinessValidationException extends LoanServiceException {
    
    private static final String ERROR_CODE = "BUSINESS_VALIDATION_ERROR";
    
    private final String field;
    private final Object rejectedValue;
    private final Map<String, List<String>> fieldErrors;
    
    public BusinessValidationException(String field, String technicalMessage, String userMessage) {
        super(ERROR_CODE, technicalMessage, userMessage);
        this.field = field;
        this.rejectedValue = null;
        this.fieldErrors = null;
    }
    
    public BusinessValidationException(String field, Object rejectedValue, String technicalMessage, String userMessage) {
        super(ERROR_CODE, technicalMessage, userMessage);
        this.field = field;
        this.rejectedValue = rejectedValue;
        this.fieldErrors = null;
    }
    
    public BusinessValidationException(Map<String, List<String>> fieldErrors, String technicalMessage, String userMessage) {
        super(ERROR_CODE, technicalMessage, userMessage);
        this.field = null;
        this.rejectedValue = null;
        this.fieldErrors = fieldErrors;
    }
    
    // Métodos de conveniencia para errores específicos del negocio de préstamos
    public static BusinessValidationException loanAmountExceedsLimit(Double requestedAmount, Double maxLimit) {
        return new BusinessValidationException(
            "loanAmount",
            requestedAmount,
            String.format("Requested loan amount %.2f exceeds maximum limit %.2f", requestedAmount, maxLimit),
            "El monto solicitado excede el límite máximo permitido para este tipo de préstamo"
        );
    }
    
    public static BusinessValidationException insufficientCreditScore(Integer creditScore, Integer minRequired) {
        return new BusinessValidationException(
            "creditScore",
            creditScore,
            String.format("Credit score %d is below minimum required %d", creditScore, minRequired),
            "Su puntaje crediticio no cumple con los requisitos mínimos para este préstamo"
        );
    }
    
    public static BusinessValidationException invalidLoanTerm(Integer termMonths, Integer minTerm, Integer maxTerm) {
        return new BusinessValidationException(
            "loanTermMonths",
            termMonths,
            String.format("Loan term %d months is outside valid range [%d, %d]", termMonths, minTerm, maxTerm),
            String.format("El plazo del préstamo debe estar entre %d y %d meses", minTerm, maxTerm)
        );
    }
    
    public static BusinessValidationException applicationAlreadyExists(String userId, String loanType) {
        return new BusinessValidationException(
            "applicationStatus",
            "Ya existe una solicitud activa para este usuario y tipo de préstamo",
            String.format("Active application already exists for user %s and loan type %s", userId, loanType)
        );
    }
    
    public static BusinessValidationException invalidApplicationStatus(String currentStatus, String targetStatus) {
        return new BusinessValidationException(
            "applicationStatus",
            targetStatus,
            String.format("Cannot transition from status %s to %s", currentStatus, targetStatus),
            "No se puede realizar esta transición de estado para la solicitud"
        );
    }
    
    // Getters
    public String getField() {
        return field;
    }
    
    public Object getRejectedValue() {
        return rejectedValue;
    }
    
    public Map<String, List<String>> getFieldErrors() {
        return fieldErrors;
    }
}