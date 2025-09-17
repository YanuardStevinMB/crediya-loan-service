package com.crediya.loan.api.handler;

import com.crediya.loan.api.dto.ErrorResponseDto;
import com.crediya.loan.api.mapper.ExceptionMapper;
import com.crediya.loan.api.service.ErrorLoggingService;
import com.crediya.loan.usecase.shared.ValidationException;
import com.crediya.loan.usecase.shared.exception.*;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerFilterFunction;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

/**
 * Global Exception Handler para programación reactiva.
 * Maneja todas las excepciones del dominio manteniendo la estructura existente.
 * NO modifica dependencias ni configuraciones existentes.
 */
@Component
public class GlobalExceptionHandler implements HandlerFilterFunction<ServerResponse, ServerResponse> {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    private final ExceptionMapper exceptionMapper;
    private final ErrorLoggingService errorLoggingService;

    public GlobalExceptionHandler(ExceptionMapper exceptionMapper, ErrorLoggingService errorLoggingService) {
        this.exceptionMapper = exceptionMapper;
        this.errorLoggingService = errorLoggingService;
    }

    @Override
    public Mono<ServerResponse> filter(ServerRequest request, HandlerFunction<ServerResponse> next) {
        return next.handle(request)
                // === EXCEPCIONES DE DOMINIO PRINCIPALES ===
                .onErrorResume(BusinessValidationException.class, 
                    ex -> handleBusinessValidation(ex, request))
                
                .onErrorResume(ResourceNotFoundException.class,
                    ex -> handleResourceNotFound(ex, request))
                
                .onErrorResume(AuthorizationException.class,
                    ex -> handleAuthorization(ex, request))
                
                .onErrorResume(ExternalServiceException.class,
                    ex -> handleExternalService(ex, request))

                // === EXCEPCIONES LEGACY (COMPATIBILIDAD) ===
                .onErrorResume(ValidationException.class,
                    ex -> handleLegacyValidation(ex, request))

                // === EXCEPCIONES ESTÁNDAR ===
                .onErrorResume(ConstraintViolationException.class,
                    ex -> handleConstraintViolation(ex, request))

                .onErrorResume(IllegalArgumentException.class,
                    ex -> handleIllegalArgument(ex, request))

                // === UNWRAP REACTOR EXCEPTIONS ===
                .onErrorResume(throwable -> {
                    Throwable unwrapped = Exceptions.unwrap(throwable);
                    
                    if (unwrapped instanceof BusinessValidationException ex) {
                        return handleBusinessValidation(ex, request);
                    } else if (unwrapped instanceof ResourceNotFoundException ex) {
                        return handleResourceNotFound(ex, request);
                    } else if (unwrapped instanceof AuthorizationException ex) {
                        return handleAuthorization(ex, request);
                    } else if (unwrapped instanceof ExternalServiceException ex) {
                        return handleExternalService(ex, request);
                    } else if (unwrapped instanceof ValidationException ex) {
                        return handleLegacyValidation(ex, request);
                    } else if (unwrapped instanceof ConstraintViolationException ex) {
                        return handleConstraintViolation(ex, request);
                    } else if (unwrapped instanceof IllegalArgumentException ex) {
                        return handleIllegalArgument(ex, request);
                    }
                    
                    // Fallback para excepciones no controladas
                    return handleGenericException(unwrapped, request);
                })
                .switchIfEmpty(ServerResponse.noContent().build());
    }

    /**
     * Maneja errores de validación de negocio (400 - Bad Request).
     */
    private Mono<ServerResponse> handleBusinessValidation(BusinessValidationException ex, ServerRequest request) {
        errorLoggingService.logBusinessValidationError(ex, request);
        ErrorResponseDto errorResponse = exceptionMapper.mapBusinessValidation(ex, request);
        return createResponse(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Maneja errores de recurso no encontrado (404 - Not Found).
     */
    private Mono<ServerResponse> handleResourceNotFound(ResourceNotFoundException ex, ServerRequest request) {
        errorLoggingService.logResourceNotFoundError(ex, request);
        ErrorResponseDto errorResponse = exceptionMapper.mapResourceNotFound(ex, request);
        return createResponse(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Maneja errores de autorización (403 - Forbidden).
     */
    private Mono<ServerResponse> handleAuthorization(AuthorizationException ex, ServerRequest request) {
        errorLoggingService.logAuthorizationError(ex, request);
        ErrorResponseDto errorResponse = exceptionMapper.mapAuthorization(ex, request);
        return createResponse(errorResponse, HttpStatus.FORBIDDEN);
    }

    /**
     * Maneja errores de servicios externos (503 - Service Unavailable).
     */
    private Mono<ServerResponse> handleExternalService(ExternalServiceException ex, ServerRequest request) {
        errorLoggingService.logExternalServiceError(ex, request);
        ErrorResponseDto errorResponse = exceptionMapper.mapExternalService(ex, request);
        return createResponse(errorResponse, HttpStatus.SERVICE_UNAVAILABLE);
    }

    /**
     * Maneja ValidationException legacy (400 - Bad Request).
     * Mantiene compatibilidad con el sistema existente.
     */
    private Mono<ServerResponse> handleLegacyValidation(ValidationException ex, ServerRequest request) {
        errorLoggingService.logLegacyValidationWarning(ex, request);
        ErrorResponseDto errorResponse = exceptionMapper.mapLegacyValidation(ex, request);
        return createResponse(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Maneja errores de Bean Validation (400 - Bad Request).
     */
    private Mono<ServerResponse> handleConstraintViolation(ConstraintViolationException ex, ServerRequest request) {
        logger.warn("Constraint violation error - Path: {} {}, Error: {}", request.methodName(), request.path(), ex.getMessage());
        ErrorResponseDto errorResponse = exceptionMapper.mapConstraintViolation(ex, request);
        return createResponse(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Maneja IllegalArgumentException (400 - Bad Request).
     */
    private Mono<ServerResponse> handleIllegalArgument(IllegalArgumentException ex, ServerRequest request) {
        logger.warn("Illegal argument error - Path: {} {}, Error: {}", request.methodName(), request.path(), ex.getMessage());
        ErrorResponseDto errorResponse = exceptionMapper.mapIllegalArgument(ex, request);
        return createResponse(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Maneja excepciones genéricas no controladas (500 - Internal Server Error).
     */
    private Mono<ServerResponse> handleGenericException(Throwable ex, ServerRequest request) {
        errorLoggingService.logGenericError(ex, request);
        ErrorResponseDto errorResponse = exceptionMapper.mapGenericException(ex, request);
        return createResponse(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Crea la respuesta HTTP con el ErrorResponseDto.
     */
    private Mono<ServerResponse> createResponse(ErrorResponseDto errorResponse, HttpStatus status) {
        return ServerResponse.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(errorResponse);
    }

}