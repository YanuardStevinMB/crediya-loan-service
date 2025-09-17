package com.crediya.loan.usecase.shared.exception;

/**
 * Excepción para errores de configuración del sistema.
 * Se usa cuando hay problemas con la configuración de la aplicación.
 */
public class SystemConfigurationException extends LoanServiceException {
    
    private static final String ERROR_CODE = "SYSTEM_CONFIGURATION_ERROR";
    
    private final String component;
    private final String parameter;
    
    public SystemConfigurationException(String component, String parameter, String technicalMessage, String userMessage) {
        super(ERROR_CODE, technicalMessage, userMessage);
        this.component = component;
        this.parameter = parameter;
    }
    
    public SystemConfigurationException(String component, String parameter, String technicalMessage, String userMessage, Throwable cause) {
        super(ERROR_CODE, technicalMessage, userMessage, cause);
        this.component = component;
        this.parameter = parameter;
    }
    
    // Métodos de conveniencia para errores de configuración específicos
    public static SystemConfigurationException missingProperty(String propertyName) {
        return new SystemConfigurationException(
            "ApplicationProperties",
            propertyName,
            String.format("Required configuration property '%s' is missing", propertyName),
            "Error de configuración del sistema. Contacte al administrador."
        );
    }
    
    public static SystemConfigurationException invalidPropertyValue(String propertyName, String value, String expectedFormat) {
        return new SystemConfigurationException(
            "ApplicationProperties",
            propertyName,
            String.format("Property '%s' has invalid value '%s'. Expected format: %s", propertyName, value, expectedFormat),
            "Error de configuración del sistema. Contacte al administrador."
        );
    }
    
    public static SystemConfigurationException databaseConfiguration(String parameter, String technicalMessage, Throwable cause) {
        return new SystemConfigurationException(
            "Database",
            parameter,
            technicalMessage,
            "Error de configuración de base de datos. El servicio no está disponible temporalmente.",
            cause
        );
    }
    
    public static SystemConfigurationException securityConfiguration(String parameter, String technicalMessage) {
        return new SystemConfigurationException(
            "Security",
            parameter,
            technicalMessage,
            "Error de configuración de seguridad. Contacte al administrador."
        );
    }
    
    public static SystemConfigurationException messagingConfiguration(String parameter, String technicalMessage, Throwable cause) {
        return new SystemConfigurationException(
            "Messaging",
            parameter,
            technicalMessage,
            "Error de configuración del sistema de mensajería. Algunas notificaciones podrían no funcionar.",
            cause
        );
    }
    
    public static SystemConfigurationException loanRulesConfiguration(String ruleName, String technicalMessage) {
        return new SystemConfigurationException(
            "LoanRules",
            ruleName,
            String.format("Loan business rule '%s' is not properly configured: %s", ruleName, technicalMessage),
            "Error en la configuración de reglas de negocio. Contacte al administrador."
        );
    }
    
    // Getters
    public String getComponent() {
        return component;
    }
    
    public String getParameter() {
        return parameter;
    }
}