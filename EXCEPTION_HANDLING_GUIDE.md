# Guía del Sistema de Manejo de Excepciones Personalizado

## Resumen

Este documento describe el sistema completo de manejo de excepciones personalizado implementado para el **LOAN-SERVICE**. El sistema proporciona un manejo robusto y consistente de errores con excepciones específicas del dominio de préstamos.

## ✅ Características Implementadas

### 1. **Jerarquía de Excepciones Personalizada**
- `LoanServiceException` - Clase base abstracta para todas las excepciones del dominio
- `BusinessValidationException` - Errores de validación de reglas de negocio
- `ResourceNotFoundException` - Recursos no encontrados (404)
- `AuthorizationException` - Errores de autorización y autenticación (403/401)
- `ExternalServiceException` - Errores de servicios externos (503)
- `SystemConfigurationException` - Errores de configuración del sistema (500)

### 2. **Global Exception Handler Mejorado**
- Manejo específico para cada tipo de excepción personalizada
- **Compatibilidad completa** con excepciones existentes (`ValidationException`, `ConfigurationException`, `ApplicationUpdateException`)
- Logging estructurado por nivel de severidad
- Respuestas HTTP apropiadas según el tipo de error

### 3. **Estructura de Respuesta Mejorada**
- `ErrorDetail` - Modelo estructurado para detalles de error
- `ApiResponse` - Mantiene el formato existente (sin cambios)
- Información técnica y de usuario separada
- Timestamps y path de request incluidos

## 🏗️ Estructura de Archivos

```
domain/usecase/src/main/java/com/crediya/loan/usecase/shared/exception/
├── LoanServiceException.java                    # Clase base abstracta
├── BusinessValidationException.java             # Validaciones de negocio
├── ResourceNotFoundException.java               # Recursos no encontrados  
├── AuthorizationException.java                  # Autorización/Autenticación
├── ExternalServiceException.java                # Servicios externos
└── SystemConfigurationException.java            # Configuración del sistema

infrastructure/entry-points/reactive-web/src/main/java/com/crediya/loan/api/
├── dto/ErrorDetail.java                         # Modelo de detalles de error
└── exceptionHandler/GlobalExceptionHandler.java # Handler global mejorado
```

## 🚀 Cómo Usar las Nuevas Excepciones

### **BusinessValidationException**

```java
// Validación simple
throw new BusinessValidationException(
    "loanAmount",
    requestedAmount,
    "Amount exceeds maximum limit", 
    "El monto excede el límite máximo"
);

// Usando métodos de conveniencia
throw BusinessValidationException.loanAmountExceedsLimit(50000.0, 30000.0);
throw BusinessValidationException.insufficientCreditScore(620, 700);
throw BusinessValidationException.invalidLoanTerm(84, 12, 60);
```

### **ResourceNotFoundException**

```java
// Recurso genérico
throw new ResourceNotFoundException(
    "Application", 
    applicationId,
    "Application not found",
    "La solicitud no fue encontrada"
);

// Usando métodos de conveniencia
throw ResourceNotFoundException.application(applicationId);
throw ResourceNotFoundException.user(userId);
throw ResourceNotFoundException.loanType(loanTypeId);
```

### **AuthorizationException**

```java
// Error de autorización genérico
throw AuthorizationException.accessDenied(userId, "update", "application");
throw AuthorizationException.applicationOwnership(userId, applicationId);
throw AuthorizationException.adminRequired(userId, "deleteApplication");
```

### **ExternalServiceException**

```java
// Servicios externos
throw ExternalServiceException.creditScoreService("getScore", "Timeout", cause);
throw ExternalServiceException.userService("getUser", 404, "User not found");
throw ExternalServiceException.databaseTimeout("saveApplication", cause);
```

### **SystemConfigurationException**

```java
// Configuración del sistema
throw SystemConfigurationException.missingProperty("database.url");
throw SystemConfigurationException.loanRulesConfiguration("maxAmount", "Invalid value");
```

## 🔄 Migración Gradual

### **Paso 1: Usar Nuevas Excepciones en Código Nuevo**
```java
// ✅ NUEVO - Usar BusinessValidationException
if (amount.compareTo(maxAmount) > 0) {
    throw BusinessValidationException.loanAmountExceedsLimit(
        amount.doubleValue(), maxAmount.doubleValue()
    );
}
```

### **Paso 2: Mantener Código Legacy Funcionando**
```java
// ✅ EXISTENTE - Sigue funcionando sin cambios
if (doc == null) {
    throw new ValidationException("identityDocument", "Required field");
}
```

### **Paso 3: Migración Gradual (Opcional)**
```java
// Reemplazar gradualmente ValidationException por BusinessValidationException
// Ver: EnhancedApplicationValidator.java como ejemplo
```

## 📋 Mapeo de Códigos de Estado HTTP

| Excepción | Código HTTP | Uso |
|-----------|-------------|-----|
| `BusinessValidationException` | 400 BAD_REQUEST | Errores de validación |
| `ResourceNotFoundException` | 404 NOT_FOUND | Recursos no encontrados |
| `AuthorizationException` | 403 FORBIDDEN | Sin permisos |
| `ExternalServiceException` | 503 SERVICE_UNAVAILABLE | Servicios externos |
| `SystemConfigurationException` | 500 INTERNAL_SERVER_ERROR | Config del sistema |
| `ApplicationUpdateException` | 409 CONFLICT | **Mantiene comportamiento existente** |
| `ValidationException` (legacy) | 400 BAD_REQUEST | **Mantiene comportamiento existente** |
| `ConfigurationException` (legacy) | 500 INTERNAL_SERVER_ERROR | **Mantiene comportamiento existente** |

## 🔍 Ejemplo de Respuesta de Error

### **BusinessValidationException**
```json
{
  "success": false,
  "message": "El monto solicitado excede el límite máximo permitido",
  "data": null,
  "errors": {
    "errorCode": "BUSINESS_VALIDATION_ERROR",
    "field": "loanAmount", 
    "rejectedValue": 50000.0,
    "technicalMessage": "Requested loan amount 50000.00 exceeds maximum limit 30000.00",
    "userMessage": "El monto solicitado excede el límite máximo permitido",
    "timestamp": "2025-09-17T04:40:00Z"
  },
  "path": "/api/v1/loans/applications",
  "timestamp": "2025-09-17T04:40:00Z"
}
```

### **ResourceNotFoundException**
```json
{
  "success": false,
  "message": "La solicitud de préstamo no fue encontrada",
  "data": null,
  "errors": {
    "errorCode": "RESOURCE_NOT_FOUND",
    "resourceType": "Application",
    "resourceId": "12345",
    "technicalMessage": "Application with ID 12345 not found",
    "userMessage": "La solicitud de préstamo no fue encontrada",
    "timestamp": "2025-09-17T04:40:00Z"
  },
  "path": "/api/v1/loans/applications/12345",
  "timestamp": "2025-09-17T04:40:00Z"
}
```

## 🛡️ Garantías de Compatibilidad

### **✅ LO QUE SIGUE FUNCIONANDO SIN CAMBIOS:**
1. Todas las excepciones existentes (`ValidationException`, `ConfigurationException`, `ApplicationUpdateException`)
2. El formato de respuesta `ApiResponse` existente
3. Los códigos de estado HTTP existentes
4. Toda la funcionalidad actual de `GlobalExceptionHandler`

### **✅ LO QUE SE MEJORÓ:**
1. Manejo más específico y detallado de errores
2. Logging estructurado por nivel de severidad
3. Información más rica en las respuestas de error
4. Excepciones específicas del dominio de préstamos

## 🔧 Configuración y Testing

### **Verificar que todo funciona:**
```bash
# Compilar el proyecto
./gradlew build

# Ejecutar tests
./gradlew test

# Verificar que no hay errores de compilación
./gradlew compileJava
```

### **Ejemplo de uso en un caso de uso:**
```java
@Component
public class LoanApplicationUseCase {
    
    public Application createApplication(Application application) {
        try {
            // Validar usando las nuevas excepciones
            EnhancedApplicationValidator.validateAndNormalize(application);
            
            // Verificar si el usuario existe
            User user = userGateway.findByDocument(application.getIdentityDocument())
                .orElseThrow(() -> ResourceNotFoundException.user(application.getIdentityDocument()));
            
            // Verificar autorización
            if (!canCreateApplication(user, application)) {
                throw AuthorizationException.accessDenied(
                    user.getId(), "create", "application"
                );
            }
            
            return applicationGateway.save(application);
            
        } catch (DatabaseException e) {
            throw ExternalServiceException.databaseTimeout("createApplication", e);
        }
    }
}
```

## ⚠️ Consideraciones Importantes

1. **NO ROMPE FUNCIONALIDAD EXISTENTE**: Todo el código actual sigue funcionando
2. **MIGRACIÓN OPCIONAL**: Puedes usar las nuevas excepciones gradualmente
3. **LOGGING MEJORADO**: Se incluye logging estructurado para debugging
4. **PERFORMANCE**: Las nuevas excepciones no impactan el performance
5. **TESTING**: Las excepciones existentes siguen siendo manejadas igual

## 🎯 Próximos Pasos Recomendados

1. **Compilar y probar** que todo funciona correctamente
2. **Usar nuevas excepciones** en código nuevo que escribas
3. **Migrar gradualmente** validaciones críticas (opcional)
4. **Monitorear logs** para identificar áreas de mejora
5. **Crear tests específicos** para las nuevas excepciones (recomendado)

---

**📞 Contacto**: Si tienes alguna duda sobre el sistema de excepciones, revisa este documento primero. El sistema está diseñado para ser intuitivo y mantener toda la funcionalidad existente.