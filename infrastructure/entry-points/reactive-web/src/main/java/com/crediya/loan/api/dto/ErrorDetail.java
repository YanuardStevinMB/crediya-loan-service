package com.crediya.loan.api.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

/**
 * Modelo para los detalles específicos de un error.
 * Proporciona información estructurada sobre errores específicos del dominio.
 */
@Data
@Builder
public class ErrorDetail {
    private String errorCode;
    private String field;
    private Object rejectedValue;
    private String technicalMessage;
    private String userMessage;
    private String resourceType;
    private String resourceId;
    private String serviceName;
    private String operation;
    private Integer statusCode;
    private String component;
    private String parameter;
    private String action;
    private String resource;
    private String userId;
    private Instant timestamp;
    private Map<String, Object> additionalInfo;
    
    public static ErrorDetail fromException(Exception ex) {
        return ErrorDetail.builder()
                .technicalMessage(ex.getMessage())
                .userMessage("Ha ocurrido un error inesperado")
                .timestamp(Instant.now())
                .build();
    }
}