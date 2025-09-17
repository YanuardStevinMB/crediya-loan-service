package com.crediya.loan.usecase.shared.exception;

/**
 * Excepción base para todas las excepciones del dominio del servicio de préstamos.
 * Proporciona funcionalidad común y permite categorizar todas las excepciones del negocio.
 */
public abstract class LoanServiceException extends RuntimeException {
    
    private final String errorCode;
    private final String userMessage;
    
    protected LoanServiceException(String errorCode, String technicalMessage, String userMessage) {
        super(technicalMessage);
        this.errorCode = errorCode;
        this.userMessage = userMessage;
    }
    
    protected LoanServiceException(String errorCode, String technicalMessage, String userMessage, Throwable cause) {
        super(technicalMessage, cause);
        this.errorCode = errorCode;
        this.userMessage = userMessage;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public String getUserMessage() {
        return userMessage;
    }
    
    public String getTechnicalMessage() {
        return getMessage();
    }
}