package com.crediya.loan.api.service;

import com.crediya.loan.usecase.shared.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Servicio de logging estructurado para errores y métricas.
 * No modifica configuraciones existentes, solo agrega funcionalidad de logging.
 */
@Service
public class ErrorLoggingService {

    private static final Logger logger = LoggerFactory.getLogger(ErrorLoggingService.class);
    private static final Logger metricsLogger = LoggerFactory.getLogger("METRICS");

    /**
     * Registra un error de validación de negocio.
     */
    public void logBusinessValidationError(BusinessValidationException ex, ServerRequest request) {
        Map<String, Object> logContext = createBaseLogContext(ex, request, "BUSINESS_VALIDATION");
        
        if (ex.getField() != null) {
            logContext.put("field", ex.getField());
        }
        if (ex.getRejectedValue() != null) {
            logContext.put("rejectedValue", ex.getRejectedValue().toString());
        }
        
        logWithContext(logger, "Business validation error", logContext, ex);
        recordMetric("error.business_validation", request);
    }

    /**
     * Registra un error de recurso no encontrado.
     */
    public void logResourceNotFoundError(ResourceNotFoundException ex, ServerRequest request) {
        Map<String, Object> logContext = createBaseLogContext(ex, request, "RESOURCE_NOT_FOUND");
        logContext.put("resourceType", ex.getResourceType());
        logContext.put("resourceId", ex.getResourceId());
        
        logWithContext(logger, "Resource not found", logContext, ex);
        recordMetric("error.resource_not_found", request);
    }

    /**
     * Registra un error de autorización.
     */
    public void logAuthorizationError(AuthorizationException ex, ServerRequest request) {
        Map<String, Object> logContext = createBaseLogContext(ex, request, "AUTHORIZATION");
        logContext.put("action", ex.getAction());
        logContext.put("resource", ex.getResource());
        logContext.put("userId", ex.getUserId());
        
        // No incluir stack trace para errores de autorización (información sensible)
        logWithContext(logger, "Authorization error", logContext, null);
        recordMetric("error.authorization", request);
    }

    /**
     * Registra un error de servicio externo.
     */
    public void logExternalServiceError(ExternalServiceException ex, ServerRequest request) {
        Map<String, Object> logContext = createBaseLogContext(ex, request, "EXTERNAL_SERVICE");
        logContext.put("serviceName", ex.getServiceName());
        logContext.put("operation", ex.getOperation());
        
        if (ex.getStatusCode() != null) {
            logContext.put("externalStatusCode", ex.getStatusCode());
        }
        
        logWithContext(logger, "External service error", logContext, ex);
        recordMetric("error.external_service", request);
    }

    /**
     * Registra un error genérico del sistema.
     */
    public void logGenericError(Throwable ex, ServerRequest request) {
        Map<String, Object> logContext = createBaseLogContext(ex, request, "SYSTEM_ERROR");
        
        logWithContext(logger, "System error", logContext, ex);
        recordMetric("error.system", request);
    }

    /**
     * Registra un warning para errores de validación legacy.
     */
    public void logLegacyValidationWarning(Exception ex, ServerRequest request) {
        Map<String, Object> logContext = createBaseLogContext(ex, request, "LEGACY_VALIDATION");
        
        logWithContext(logger, "Legacy validation error", logContext, null);
        recordMetric("warning.legacy_validation", request);
    }

    /**
     * Crea el contexto base para todos los logs.
     */
    private Map<String, Object> createBaseLogContext(Throwable ex, ServerRequest request, String errorType) {
        Map<String, Object> context = new HashMap<>();
        context.put("timestamp", Instant.now().toString());
        context.put("errorType", errorType);
        context.put("path", request.path());
        context.put("method", request.methodName());
        context.put("userAgent", request.headers().firstHeader("User-Agent"));
        context.put("requestId", generateRequestId(request));
        
        if (ex instanceof LoanServiceException loanEx) {
            context.put("errorCode", loanEx.getErrorCode());
            context.put("userMessage", loanEx.getUserMessage());
            context.put("technicalMessage", loanEx.getTechnicalMessage());
        } else {
            context.put("message", ex.getMessage());
        }
        
        return context;
    }

    /**
     * Registra el log con contexto estructurado.
     */
    private void logWithContext(Logger targetLogger, String message, Map<String, Object> context, Throwable ex) {
        try {
            // Agregar contexto al MDC
            context.forEach((key, value) -> {
                if (value != null) {
                    MDC.put(key, value.toString());
                }
            });
            
            if (ex != null) {
                targetLogger.error("{} - Context: {}", message, context, ex);
            } else {
                targetLogger.warn("{} - Context: {}", message, context);
            }
            
        } finally {
            // Limpiar MDC
            MDC.clear();
        }
    }

    /**
     * Registra métricas de errores.
     */
    private void recordMetric(String metricName, ServerRequest request) {
        Map<String, Object> metricContext = new HashMap<>();
        metricContext.put("metric", metricName);
        metricContext.put("path", request.path());
        metricContext.put("method", request.methodName());
        metricContext.put("timestamp", Instant.now().toString());
        
        metricsLogger.info("Metric recorded: {}", metricContext);
    }

    /**
     * Genera un ID de request único para tracking.
     */
    private String generateRequestId(ServerRequest request) {
        // Intenta obtener un trace ID existente de headers
        String traceId = request.headers().firstHeader("X-Trace-Id");
        if (traceId != null && !traceId.isBlank()) {
            return traceId;
        }
        
        // Si no existe, genera uno nuevo
        return java.util.UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Registra información de performance para endpoints exitosos.
     */
    public void logSuccessMetrics(ServerRequest request, long responseTimeMs) {
        Map<String, Object> successContext = new HashMap<>();
        successContext.put("metric", "success");
        successContext.put("path", request.path());
        successContext.put("method", request.methodName());
        successContext.put("responseTimeMs", responseTimeMs);
        successContext.put("timestamp", Instant.now().toString());
        
        metricsLogger.info("Success metric: {}", successContext);
    }
}