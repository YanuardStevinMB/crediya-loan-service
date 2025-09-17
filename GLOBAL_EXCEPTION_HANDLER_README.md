# Global Exception Handler - Documentación

## Descripción General

Se ha implementado un sistema completo de manejo global de excepciones para el servicio de préstamos, diseñado específicamente para **programación reactiva** y manteniendo **total compatibilidad** con el código existente.

## Características Principales

✅ **Manejo Global**: Captura todas las excepciones del dominio automáticamente  
✅ **Programación Reactiva**: Compatible con WebFlux y Mono/Flux  
✅ **Sin Modificaciones**: No modifica dependencias ni configuraciones existentes  
✅ **Swagger Documentado**: Respuestas de error documentadas automáticamente  
✅ **Logging Estructurado**: Registro detallado para monitoreo y debugging  
✅ **Métricas Integradas**: Tracking de errores para observabilidad  

## Arquitectura Implementada

```
┌─────────────────────┐    ┌──────────────────────┐    ┌─────────────────────┐
│   RouterRest.java   │───▶│ GlobalExceptionHandler│───▶│  ExceptionMapper    │
└─────────────────────┘    └──────────────────────┘    └─────────────────────┘
                                        │                           │
                                        ▼                           ▼
                           ┌──────────────────────┐    ┌─────────────────────┐
                           │ ErrorLoggingService  │    │  ErrorResponseDto   │
                           └──────────────────────┘    └─────────────────────┘
```

## Excepciones Manejadas

### 1. Excepciones de Dominio

#### `BusinessValidationException` → **400 Bad Request**
```java
// Ejemplo de uso en caso de uso
public Mono<Application> validateApplication(Application app) {
    if (app.getAmount().compareTo(MAX_AMOUNT) > 0) {
        throw BusinessValidationException.loanAmountExceedsLimit(
            app.getAmount().doubleValue(), 
            MAX_AMOUNT.doubleValue()
        );
    }
    return Mono.just(app);
}
```

#### `ResourceNotFoundException` → **404 Not Found**
```java
// Ejemplo de uso en repositorio
public Mono<Application> findById(String id) {
    return repository.findById(id)
        .switchIfEmpty(Mono.error(ResourceNotFoundException.application(id)));
}
```

#### `AuthorizationException` → **403 Forbidden**
```java
// Ejemplo de uso en validación de permisos
public Mono<Void> checkUserAccess(String userId, String applicationId) {
    if (!hasAccess(userId, applicationId)) {
        throw AuthorizationException.applicationOwnership(userId, applicationId);
    }
    return Mono.empty();
}
```

#### `ExternalServiceException` → **503 Service Unavailable**
```java
// Ejemplo de uso en servicios externos
public Mono<CreditScore> getCreditScore(String userId) {
    return webClient.get()
        .uri("/credit-score/{userId}", userId)
        .retrieve()
        .bodyToMono(CreditScore.class)
        .onErrorMap(WebClientException.class, 
            ex -> ExternalServiceException.creditScoreService("getCreditScore", ex.getMessage(), ex));
}
```

### 2. Excepciones Legacy (Compatibilidad)

#### `ValidationException` → **400 Bad Request**
```java
// Mantiene compatibilidad con código existente
if (document.length() < 6) {
    throw new ValidationException("identityDocument", "Documento muy corto");
}
```

### 3. Excepciones Estándar

- `ConstraintViolationException` → **400 Bad Request**
- `IllegalArgumentException` → **400 Bad Request**
- `Throwable` (genérica) → **500 Internal Server Error**

## Formato de Respuesta

### Respuesta Básica (Compatible con ApiErrorResponse)
```json
{
  "timestamp": "2025-01-17T06:01:50Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Error de validación",
  "path": "/api/v1/application",
  "method": "POST"
}
```

### Respuesta Enriquecida (Nuevas Excepciones de Dominio)
```json
{
  "timestamp": "2025-01-17T06:01:50Z",
  "status": 400,
  "error": "Bad Request",
  "message": "El monto solicitado excede el límite máximo permitido",
  "path": "/api/v1/application",
  "method": "POST",
  "errorCode": "BUSINESS_VALIDATION_ERROR",
  "technicalMessage": "Requested loan amount 20000000.00 exceeds maximum limit 15000000.00",
  "userMessage": "El monto solicitado excede el límite máximo permitido para este tipo de préstamo",
  "traceId": "a1b2c3d4",
  "details": [
    {
      "field": "loanAmount",
      "rejectedValue": 20000000.00,
      "technicalMessage": "Requested loan amount 20000000.00 exceeds maximum limit 15000000.00",
      "userMessage": "El monto solicitado excede el límite máximo permitido para este tipo de préstamo",
      "errorCode": "FIELD_VALIDATION"
    }
  ]
}
```

## Logging Estructurado

### Contexto de Logs
Cada error se registra con información contextual completa:

```json
{
  "timestamp": "2025-01-17T06:01:50Z",
  "errorType": "BUSINESS_VALIDATION",
  "path": "/api/v1/application",
  "method": "POST",
  "userAgent": "Mozilla/5.0...",
  "requestId": "a1b2c3d4",
  "errorCode": "BUSINESS_VALIDATION_ERROR",
  "field": "loanAmount",
  "rejectedValue": "20000000.00",
  "userMessage": "El monto solicitado excede el límite máximo permitido",
  "technicalMessage": "Requested loan amount 20000000.00 exceeds maximum limit 15000000.00"
}
```

### Métricas
Se registran métricas automáticas para:
- `error.business_validation`
- `error.resource_not_found`
- `error.authorization`
- `error.external_service`
- `error.system`
- `warning.legacy_validation`

## Swagger/OpenAPI

Las respuestas de error están automáticamente documentadas en Swagger:

- **200**: Respuesta exitosa
- **400**: Error de validación (`ErrorResponseDto`)
- **403**: Error de autorización (`ErrorResponseDto`)
- **404**: Recurso no encontrado (`ErrorResponseDto`)
- **500**: Error interno del servidor (`ErrorResponseDto`)
- **503**: Servicio no disponible (`ErrorResponseDto`)

## Configuración

### Dependencias NO Modificadas
- ✅ `build.gradle` - Sin cambios
- ✅ `pom.xml` - Sin cambios
- ✅ Configuración de Spring Boot - Sin cambios
- ✅ Configuración de Swagger - Sin cambios

### Componentes Agregados
- `GlobalExceptionHandler` - Handler principal reactivo
- `ExceptionMapper` - Mapeo de excepciones a DTOs
- `ErrorLoggingService` - Logging estructurado
- `ErrorResponseDto` - DTO de respuesta mejorado
- `ErrorDetailDto` - DTO para detalles específicos

## Cómo Usar

### 1. En Casos de Uso
```java
@UseCase
public class CreateApplicationUseCase {
    
    public Mono<Application> execute(Application application) {
        return validateApplication(application)
            .flatMap(this::processApplication)
            .onErrorMap(DatabaseException.class, 
                ex -> ExternalServiceException.databaseTimeout("createApplication", ex));
    }
    
    private Mono<Application> validateApplication(Application app) {
        if (app.getAmount().compareTo(MAX_AMOUNT) > 0) {
            return Mono.error(BusinessValidationException.loanAmountExceedsLimit(
                app.getAmount().doubleValue(), 
                MAX_AMOUNT.doubleValue()
            ));
        }
        return Mono.just(app);
    }
}
```

### 2. En Repositorios
```java
@Repository
public class ApplicationRepositoryImpl {
    
    public Mono<Application> findById(String id) {
        return databaseClient.select()
            .from(Application.class)
            .matching(where("id").is(id))
            .fetch()
            .one()
            .switchIfEmpty(Mono.error(ResourceNotFoundException.application(id)));
    }
}
```

### 3. En Handlers/Controllers
```java
@Component
public class ApplicationHandler {
    
    public Mono<ServerResponse> createApplication(ServerRequest request) {
        return request.bodyToMono(ApplicationSaveDto.class)
            .flatMap(dto -> {
                // Las excepciones se manejan automáticamente
                return useCase.execute(mapper.toEntity(dto));
            })
            .flatMap(result -> ServerResponse.ok().bodyValue(result));
            // No necesita manejo de excepciones manual
    }
}
```

## Migración desde ApiErrorFilter

### Antes (ApiErrorFilter)
```java
public RouterFunction<ServerResponse> routerFunction(
        ApplicationHandler handler,
        ApiErrorFilter errorFilter  // ❌ Filtro básico
) {
    return route(POST("/api/v1/application"), handler::createApplication)
            .filter(errorFilter);
}
```

### Después (GlobalExceptionHandler)
```java
public RouterFunction<ServerResponse> routerFunction(
        ApplicationHandler handler,
        GlobalExceptionHandler globalExceptionHandler  // ✅ Handler completo
) {
    return route(POST("/api/v1/application"), handler::createApplication)
            .filter(globalExceptionHandler);
}
```

## Monitoreo y Observabilidad

### Logs de Aplicación
```bash
# Filtrar errores de negocio
grep "BUSINESS_VALIDATION" application.log

# Filtrar errores de servicios externos
grep "EXTERNAL_SERVICE" application.log

# Filtrar por requestId específico
grep "a1b2c3d4" application.log
```

### Métricas
```bash
# Ver métricas de errores
grep "METRICS" application.log | grep "error."

# Contar errores por tipo
grep "error.business_validation" application.log | wc -l
```

## Beneficios

1. **Consistencia**: Todas las excepciones se manejan de forma uniforme
2. **Trazabilidad**: Cada error tiene un traceId único
3. **Observabilidad**: Logging estructurado y métricas automáticas
4. **Documentación**: Swagger actualizado automáticamente
5. **Mantenibilidad**: Código centralizado y bien organizado
6. **Compatibilidad**: Funciona con código existente sin modificaciones

## Pruebas

### Prueba de Validación de Negocio
```bash
curl -X POST http://localhost:8080/api/v1/application \
  -H "Content-Type: application/json" \
  -d '{"amount": 20000000, "identityDocument": "12345678"}'
```

### Prueba de Recurso No Encontrado
```bash
curl -X PUT http://localhost:8080/api/v1/application/update-state \
  -H "Content-Type: application/json" \
  -d '{"applicationId": "nonexistent", "newState": "APPROVED"}'
```

---

**Implementado con ❤️ manteniendo la compatibilidad total y siguiendo las mejores prácticas de programación reactiva.**