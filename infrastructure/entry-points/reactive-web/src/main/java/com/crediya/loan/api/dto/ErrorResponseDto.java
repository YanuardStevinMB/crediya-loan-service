package com.crediya.loan.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * DTO mejorado para respuestas de error.
 * Mantiene compatibilidad con ApiErrorResponse existente pero agrega más información estructurada.
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "ErrorResponseDto", description = "Respuesta estructurada de error del sistema")
public class ErrorResponseDto {

    @Schema(description = "Timestamp del error", example = "2025-01-17T06:01:50Z")
    private String timestamp;

    @Schema(description = "Código HTTP de estado", example = "400")
    private Integer status;

    @Schema(description = "Descripción del estado HTTP", example = "Bad Request")
    private String error;

    @Schema(description = "Mensaje principal del error", example = "Error de validación en los datos de entrada")
    private String message;

    @Schema(description = "Ruta del endpoint donde ocurrió el error", example = "/api/v1/application")
    private String path;

    @Schema(description = "Método HTTP utilizado", example = "POST")
    private String method;

    @Schema(description = "Código específico del error de negocio", example = "BUSINESS_VALIDATION_ERROR")
    private String errorCode;

    @Schema(description = "Información técnica del error para desarrolladores")
    private String technicalMessage;

    @Schema(description = "Mensaje amigable para el usuario final")
    private String userMessage;

    @Schema(description = "Detalles específicos del error")
    private List<ErrorDetailDto> details;

    @Schema(description = "Información adicional contextual")
    private Map<String, Object> additionalInfo;

    @Schema(description = "ID único para rastreo del error")
    private String traceId;

    /**
     * Crea una respuesta de error básica manteniendo compatibilidad con ApiErrorResponse.
     */
    public static ErrorResponseDto basic(String timestamp, int status, String error, 
                                        String message, String path, String method) {
        return ErrorResponseDto.builder()
                .timestamp(timestamp)
                .status(status)
                .error(error)
                .message(message)
                .path(path)
                .method(method)
                .build();
    }

    /**
     * Crea una respuesta de error enriquecida con información del dominio.
     */
    public static ErrorResponseDto enriched(String timestamp, int status, String error,
                                           String message, String path, String method,
                                           String errorCode, String technicalMessage, 
                                           String userMessage) {
        return ErrorResponseDto.builder()
                .timestamp(timestamp)
                .status(status)
                .error(error)
                .message(message)
                .path(path)
                .method(method)
                .errorCode(errorCode)
                .technicalMessage(technicalMessage)
                .userMessage(userMessage)
                .traceId(java.util.UUID.randomUUID().toString().substring(0, 8))
                .build();
    }
}