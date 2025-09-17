package com.crediya.loan.api.mapper;

import com.crediya.loan.api.dto.ErrorDetailDto;
import com.crediya.loan.api.dto.ErrorResponseDto;
import com.crediya.loan.usecase.shared.ValidationException;
import com.crediya.loan.usecase.shared.exception.*;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Mapper para convertir excepciones de dominio en respuestas HTTP apropiadas.
 * Mantiene la separación de responsabilidades y centraliza la lógica de mapeo.
 */
@Component
public class ExceptionMapper {

    /**
     * Mapea BusinessValidationException a ErrorResponseDto con status 400.
     */
    public ErrorResponseDto mapBusinessValidation(BusinessValidationException ex, ServerRequest request) {
        ErrorResponseDto response = ErrorResponseDto.enriched(
                OffsetDateTime.now().toString(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getUserMessage() != null ? ex.getUserMessage() : "Error de validación de negocio",
                request.path(),
                request.methodName(),
                ex.getErrorCode(),
                ex.getTechnicalMessage(),
                ex.getUserMessage()
        );

        // Agregar detalles específicos si existen
        if (ex.getField() != null) {
            ErrorDetailDto detail = ErrorDetailDto.fieldValidation(
                    ex.getField(),
                    ex.getRejectedValue(),
                    ex.getTechnicalMessage(),
                    ex.getUserMessage()
            );
            response.setDetails(Collections.singletonList(detail));
        }

        // Agregar errores de múltiples campos si existen
        if (ex.getFieldErrors() != null && !ex.getFieldErrors().isEmpty()) {
            List<ErrorDetailDto> details = ex.getFieldErrors().entrySet().stream()
                    .flatMap(entry -> entry.getValue().stream().map(errorMsg ->
                            ErrorDetailDto.fieldValidation(entry.getKey(), null, errorMsg, errorMsg)))
                    .collect(Collectors.toList());
            response.setDetails(details);
        }

        return response;
    }

    /**
     * Mapea ResourceNotFoundException a ErrorResponseDto con status 404.
     */
    public ErrorResponseDto mapResourceNotFound(ResourceNotFoundException ex, ServerRequest request) {
        ErrorResponseDto response = ErrorResponseDto.enriched(
                OffsetDateTime.now().toString(),
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                ex.getUserMessage() != null ? ex.getUserMessage() : "Recurso no encontrado",
                request.path(),
                request.methodName(),
                ex.getErrorCode(),
                ex.getTechnicalMessage(),
                ex.getUserMessage()
        );

        ErrorDetailDto detail = ErrorDetailDto.resourceNotFound(
                ex.getResourceType(),
                ex.getResourceId(),
                ex.getTechnicalMessage(),
                ex.getUserMessage()
        );
        response.setDetails(Collections.singletonList(detail));

        return response;
    }

    /**
     * Mapea AuthorizationException a ErrorResponseDto con status 403.
     */
    public ErrorResponseDto mapAuthorization(AuthorizationException ex, ServerRequest request) {
        ErrorResponseDto response = ErrorResponseDto.enriched(
                OffsetDateTime.now().toString(),
                HttpStatus.FORBIDDEN.value(),
                HttpStatus.FORBIDDEN.getReasonPhrase(),
                ex.getUserMessage() != null ? ex.getUserMessage() : "Acceso denegado",
                request.path(),
                request.methodName(),
                ex.getErrorCode(),
                ex.getTechnicalMessage(),
                ex.getUserMessage()
        );

        ErrorDetailDto detail = ErrorDetailDto.authorization(
                ex.getAction(),
                ex.getResource(),
                ex.getTechnicalMessage(),
                ex.getUserMessage()
        );
        detail.setAdditionalInfo(Map.of("userId", ex.getUserId()));
        response.setDetails(Collections.singletonList(detail));

        return response;
    }

    /**
     * Mapea ExternalServiceException a ErrorResponseDto con status 503.
     */
    public ErrorResponseDto mapExternalService(ExternalServiceException ex, ServerRequest request) {
        ErrorResponseDto response = ErrorResponseDto.enriched(
                OffsetDateTime.now().toString(),
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase(),
                ex.getUserMessage() != null ? ex.getUserMessage() : "Servicio temporalmente no disponible",
                request.path(),
                request.methodName(),
                ex.getErrorCode(),
                ex.getTechnicalMessage(),
                ex.getUserMessage()
        );

        ErrorDetailDto detail = ErrorDetailDto.externalService(
                ex.getServiceName(),
                ex.getOperation(),
                ex.getTechnicalMessage(),
                ex.getUserMessage()
        );
        
        if (ex.getStatusCode() != null) {
            detail.setAdditionalInfo(Map.of(
                    "serviceName", ex.getServiceName(),
                    "operation", ex.getOperation(),
                    "externalStatusCode", ex.getStatusCode()
            ));
        }
        
        response.setDetails(Collections.singletonList(detail));
        return response;
    }

    /**
     * Mapea ValidationException legacy a ErrorResponseDto con status 400.
     * Mantiene compatibilidad con el sistema existente.
     */
    public ErrorResponseDto mapLegacyValidation(ValidationException ex, ServerRequest request) {
        ErrorResponseDto response = ErrorResponseDto.basic(
                OffsetDateTime.now().toString(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage(),
                request.path(),
                request.methodName()
        );

        if (ex.getField() != null) {
            ErrorDetailDto detail = ErrorDetailDto.fieldValidation(
                    ex.getField(),
                    null,
                    ex.getMessage(),
                    ex.getMessage()
            );
            response.setDetails(Collections.singletonList(detail));
        }

        return response;
    }

    /**
     * Mapea ConstraintViolationException a ErrorResponseDto con status 400.
     */
    public ErrorResponseDto mapConstraintViolation(ConstraintViolationException ex, ServerRequest request) {
        ErrorResponseDto response = ErrorResponseDto.basic(
                OffsetDateTime.now().toString(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Datos de entrada inválidos",
                request.path(),
                request.methodName()
        );

        List<ErrorDetailDto> details = ex.getConstraintViolations().stream()
                .map(this::constraintViolationToDetail)
                .collect(Collectors.toList());
        response.setDetails(details);

        return response;
    }

    /**
     * Mapea IllegalArgumentException a ErrorResponseDto con status 400.
     */
    public ErrorResponseDto mapIllegalArgument(IllegalArgumentException ex, ServerRequest request) {
        return ErrorResponseDto.basic(
                OffsetDateTime.now().toString(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage(),
                request.path(),
                request.methodName()
        );
    }

    /**
     * Mapea excepciones genéricas a ErrorResponseDto con status 500.
     */
    public ErrorResponseDto mapGenericException(Throwable ex, ServerRequest request) {
        return ErrorResponseDto.enriched(
                OffsetDateTime.now().toString(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "Ha ocurrido un error inesperado en el sistema",
                request.path(),
                request.methodName(),
                "INTERNAL_SERVER_ERROR",
                ex.getMessage(),
                "Ha ocurrido un error inesperado en el sistema. Por favor contacte al administrador."
        );
    }

    /**
     * Convierte una ConstraintViolation en ErrorDetailDto.
     */
    private ErrorDetailDto constraintViolationToDetail(ConstraintViolation<?> violation) {
        String field = violation.getPropertyPath() != null ? 
                violation.getPropertyPath().toString() : "unknown";
        
        return ErrorDetailDto.fieldValidation(
                field,
                violation.getInvalidValue(),
                violation.getMessage(),
                violation.getMessage()
        );
    }
}