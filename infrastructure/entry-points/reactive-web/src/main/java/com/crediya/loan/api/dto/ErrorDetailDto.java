package com.crediya.loan.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * DTO para detalles específicos de errores.
 * Proporciona información granular sobre errores específicos de validación o negocio.
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "ErrorDetailDto", description = "Detalle específico de un error")
public class ErrorDetailDto {

    @Schema(description = "Campo que causó el error", example = "identityDocument")
    private String field;

    @Schema(description = "Valor que fue rechazado", example = "123abc")
    private Object rejectedValue;

    @Schema(description = "Mensaje técnico del error")
    private String technicalMessage;

    @Schema(description = "Mensaje para el usuario final")
    private String userMessage;

    @Schema(description = "Código específico del error", example = "INVALID_FORMAT")
    private String errorCode;

    @Schema(description = "Tipo de recurso afectado", example = "Application")
    private String resourceType;

    @Schema(description = "ID del recurso afectado", example = "123456")
    private String resourceId;

    @Schema(description = "Información adicional contextual")
    private Map<String, Object> additionalInfo;

    /**
     * Crea un detalle de error para validación de campo.
     */
    public static ErrorDetailDto fieldValidation(String field, Object rejectedValue, 
                                                 String technicalMessage, String userMessage) {
        return ErrorDetailDto.builder()
                .field(field)
                .rejectedValue(rejectedValue)
                .technicalMessage(technicalMessage)
                .userMessage(userMessage)
                .errorCode("FIELD_VALIDATION")
                .build();
    }

    /**
     * Crea un detalle de error para recurso no encontrado.
     */
    public static ErrorDetailDto resourceNotFound(String resourceType, String resourceId, 
                                                  String technicalMessage, String userMessage) {
        return ErrorDetailDto.builder()
                .resourceType(resourceType)
                .resourceId(resourceId)
                .technicalMessage(technicalMessage)
                .userMessage(userMessage)
                .errorCode("RESOURCE_NOT_FOUND")
                .build();
    }

    /**
     * Crea un detalle de error para errores de autorización.
     */
    public static ErrorDetailDto authorization(String action, String resource, 
                                               String technicalMessage, String userMessage) {
        return ErrorDetailDto.builder()
                .technicalMessage(technicalMessage)
                .userMessage(userMessage)
                .errorCode("AUTHORIZATION_ERROR")
                .additionalInfo(Map.of(
                    "action", action,
                    "resource", resource
                ))
                .build();
    }

    /**
     * Crea un detalle de error para servicios externos.
     */
    public static ErrorDetailDto externalService(String serviceName, String operation, 
                                                 String technicalMessage, String userMessage) {
        return ErrorDetailDto.builder()
                .technicalMessage(technicalMessage)
                .userMessage(userMessage)
                .errorCode("EXTERNAL_SERVICE_ERROR")
                .additionalInfo(Map.of(
                    "serviceName", serviceName,
                    "operation", operation
                ))
                .build();
    }
}