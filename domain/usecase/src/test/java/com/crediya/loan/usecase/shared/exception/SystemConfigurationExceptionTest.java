package com.crediya.loan.usecase.shared.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SystemConfigurationException Tests")
class SystemConfigurationExceptionTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create exception with basic parameters")
        void shouldCreateExceptionWithBasicParameters() {
            // Given
            String component = "DatabaseConfig";
            String parameter = "connectionTimeout";
            String technicalMessage = "Connection timeout not properly configured";
            String userMessage = "Database configuration error";

            // When
            SystemConfigurationException exception = new SystemConfigurationException(
                    component, parameter, technicalMessage, userMessage
            );

            // Then
            assertEquals("SYSTEM_CONFIGURATION_ERROR", exception.getErrorCode());
            assertEquals(component, exception.getComponent());
            assertEquals(parameter, exception.getParameter());
            assertEquals(technicalMessage, exception.getTechnicalMessage());
            assertEquals(userMessage, exception.getUserMessage());
            assertNull(exception.getCause());
        }

        @Test
        @DisplayName("Should create exception with cause")
        void shouldCreateExceptionWithCause() {
            // Given
            String component = "SecurityConfig";
            String parameter = "jwtSecret";
            String technicalMessage = "JWT secret key configuration failed";
            String userMessage = "Security configuration error";
            RuntimeException cause = new RuntimeException("Key generation failed");

            // When
            SystemConfigurationException exception = new SystemConfigurationException(
                    component, parameter, technicalMessage, userMessage, cause
            );

            // Then
            assertEquals("SYSTEM_CONFIGURATION_ERROR", exception.getErrorCode());
            assertEquals(component, exception.getComponent());
            assertEquals(parameter, exception.getParameter());
            assertEquals(technicalMessage, exception.getTechnicalMessage());
            assertEquals(userMessage, exception.getUserMessage());
            assertEquals(cause, exception.getCause());
        }
    }

    @Nested
    @DisplayName("Factory Methods Tests")
    class FactoryMethodsTests {

        @Test
        @DisplayName("Should create missing property exception")
        void shouldCreateMissingPropertyException() {
            // Given
            String propertyName = "app.database.url";

            // When
            SystemConfigurationException exception = SystemConfigurationException.missingProperty(propertyName);

            // Then
            assertEquals("SYSTEM_CONFIGURATION_ERROR", exception.getErrorCode());
            assertEquals("ApplicationProperties", exception.getComponent());
            assertEquals(propertyName, exception.getParameter());
            assertTrue(exception.getTechnicalMessage().contains("Required configuration property 'app.database.url' is missing"));
            assertEquals("Error de configuración del sistema. Contacte al administrador.", exception.getUserMessage());
            assertNull(exception.getCause());
        }

        @Test
        @DisplayName("Should create invalid property value exception")
        void shouldCreateInvalidPropertyValueException() {
            // Given
            String propertyName = "app.timeout";
            String value = "invalid_number";
            String expectedFormat = "positive integer";

            // When
            SystemConfigurationException exception = SystemConfigurationException
                    .invalidPropertyValue(propertyName, value, expectedFormat);

            // Then
            assertEquals("SYSTEM_CONFIGURATION_ERROR", exception.getErrorCode());
            assertEquals("ApplicationProperties", exception.getComponent());
            assertEquals(propertyName, exception.getParameter());
            assertTrue(exception.getTechnicalMessage().contains("Property 'app.timeout' has invalid value 'invalid_number'"));
            assertTrue(exception.getTechnicalMessage().contains("Expected format: positive integer"));
            assertEquals("Error de configuración del sistema. Contacte al administrador.", exception.getUserMessage());
            assertNull(exception.getCause());
        }

        @Test
        @DisplayName("Should create database configuration exception")
        void shouldCreateDatabaseConfigurationException() {
            // Given
            String parameter = "poolSize";
            String technicalMessage = "Database connection pool size configuration invalid";
            RuntimeException cause = new RuntimeException("Invalid pool configuration");

            // When
            SystemConfigurationException exception = SystemConfigurationException
                    .databaseConfiguration(parameter, technicalMessage, cause);

            // Then
            assertEquals("SYSTEM_CONFIGURATION_ERROR", exception.getErrorCode());
            assertEquals("Database", exception.getComponent());
            assertEquals(parameter, exception.getParameter());
            assertEquals(technicalMessage, exception.getTechnicalMessage());
            assertEquals(
                    "Error de configuración de base de datos. El servicio no está disponible temporalmente.",
                    exception.getUserMessage()
            );
            assertEquals(cause, exception.getCause());
        }

        @Test
        @DisplayName("Should create security configuration exception")
        void shouldCreateSecurityConfigurationException() {
            // Given
            String parameter = "tokenExpiration";
            String technicalMessage = "JWT token expiration time not configured properly";

            // When
            SystemConfigurationException exception = SystemConfigurationException
                    .securityConfiguration(parameter, technicalMessage);

            // Then
            assertEquals("SYSTEM_CONFIGURATION_ERROR", exception.getErrorCode());
            assertEquals("Security", exception.getComponent());
            assertEquals(parameter, exception.getParameter());
            assertEquals(technicalMessage, exception.getTechnicalMessage());
            assertEquals("Error de configuración de seguridad. Contacte al administrador.", exception.getUserMessage());
            assertNull(exception.getCause());
        }

        @Test
        @DisplayName("Should create messaging configuration exception")
        void shouldCreateMessagingConfigurationException() {
            // Given
            String parameter = "sqsQueueUrl";
            String technicalMessage = "SQS queue URL configuration is missing or invalid";
            Exception cause = new Exception("Queue not found");

            // When
            SystemConfigurationException exception = SystemConfigurationException
                    .messagingConfiguration(parameter, technicalMessage, cause);

            // Then
            assertEquals("SYSTEM_CONFIGURATION_ERROR", exception.getErrorCode());
            assertEquals("Messaging", exception.getComponent());
            assertEquals(parameter, exception.getParameter());
            assertEquals(technicalMessage, exception.getTechnicalMessage());
            assertEquals(
                    "Error de configuración del sistema de mensajería. Algunas notificaciones podrían no funcionar.",
                    exception.getUserMessage()
            );
            assertEquals(cause, exception.getCause());
        }

        @Test
        @DisplayName("Should create loan rules configuration exception")
        void shouldCreateLoanRulesConfigurationException() {
            // Given
            String ruleName = "maxLoanAmount";
            String technicalMessage = "maximum loan amount threshold not defined";

            // When
            SystemConfigurationException exception = SystemConfigurationException
                    .loanRulesConfiguration(ruleName, technicalMessage);

            // Then
            assertEquals("SYSTEM_CONFIGURATION_ERROR", exception.getErrorCode());
            assertEquals("LoanRules", exception.getComponent());
            assertEquals(ruleName, exception.getParameter());
            assertTrue(exception.getTechnicalMessage().contains("Loan business rule 'maxLoanAmount' is not properly configured"));
            assertTrue(exception.getTechnicalMessage().contains(technicalMessage));
            assertEquals("Error en la configuración de reglas de negocio. Contacte al administrador.", exception.getUserMessage());
            assertNull(exception.getCause());
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle null component")
        void shouldHandleNullComponent() {
            // When
            SystemConfigurationException exception = new SystemConfigurationException(
                    null, "parameter", "technical", "user"
            );

            // Then
            assertEquals("SYSTEM_CONFIGURATION_ERROR", exception.getErrorCode());
            assertNull(exception.getComponent());
            assertEquals("parameter", exception.getParameter());
        }

        @Test
        @DisplayName("Should handle null parameter")
        void shouldHandleNullParameter() {
            // When
            SystemConfigurationException exception = new SystemConfigurationException(
                    "Component", null, "technical", "user"
            );

            // Then
            assertEquals("SYSTEM_CONFIGURATION_ERROR", exception.getErrorCode());
            assertEquals("Component", exception.getComponent());
            assertNull(exception.getParameter());
        }

        @Test
        @DisplayName("Should handle empty strings")
        void shouldHandleEmptyStrings() {
            // When
            SystemConfigurationException exception = new SystemConfigurationException(
                    "", "", "technical", "user"
            );

            // Then
            assertEquals("SYSTEM_CONFIGURATION_ERROR", exception.getErrorCode());
            assertEquals("", exception.getComponent());
            assertEquals("", exception.getParameter());
        }

        @Test
        @DisplayName("Should handle null cause")
        void shouldHandleNullCause() {
            // When
            SystemConfigurationException exception = new SystemConfigurationException(
                    "Component", "parameter", "technical", "user", null
            );

            // Then
            assertEquals("SYSTEM_CONFIGURATION_ERROR", exception.getErrorCode());
            assertEquals("Component", exception.getComponent());
            assertEquals("parameter", exception.getParameter());
            assertNull(exception.getCause());
        }

        @Test
        @DisplayName("Should handle factory methods with null parameters")
        void shouldHandleFactoryMethodsWithNullParameters() {
            // Test missingProperty with null
            SystemConfigurationException missingProp = SystemConfigurationException.missingProperty(null);
            assertEquals("ApplicationProperties", missingProp.getComponent());
            assertNull(missingProp.getParameter());

            // Test invalidPropertyValue with nulls
            SystemConfigurationException invalidProp = SystemConfigurationException
                    .invalidPropertyValue(null, null, null);
            assertEquals("ApplicationProperties", invalidProp.getComponent());
            assertNull(invalidProp.getParameter());

            // Test databaseConfiguration with nulls
            SystemConfigurationException dbConfig = SystemConfigurationException
                    .databaseConfiguration(null, null, null);
            assertEquals("Database", dbConfig.getComponent());
            assertNull(dbConfig.getParameter());
            assertNull(dbConfig.getTechnicalMessage());
            assertNull(dbConfig.getCause());
        }

        @Test
        @DisplayName("Should handle special characters in parameters")
        void shouldHandleSpecialCharactersInParameters() {
            // Given
            String specialComponent = "Component@#$%";
            String specialParameter = "param-with_special.chars";

            // When
            SystemConfigurationException exception = new SystemConfigurationException(
                    specialComponent, specialParameter, "technical", "user"
            );

            // Then
            assertEquals(specialComponent, exception.getComponent());
            assertEquals(specialParameter, exception.getParameter());
        }

        @Test
        @DisplayName("Should handle very long component and parameter names")
        void shouldHandleVeryLongComponentAndParameterNames() {
            // Given
            String longComponent = "Component" + "X".repeat(1000);
            String longParameter = "parameter" + "Y".repeat(1000);

            // When
            SystemConfigurationException exception = new SystemConfigurationException(
                    longComponent, longParameter, "technical", "user"
            );

            // Then
            assertEquals(longComponent, exception.getComponent());
            assertEquals(longParameter, exception.getParameter());
        }
    }

    @Nested
    @DisplayName("Inheritance and General Tests")
    class InheritanceAndGeneralTests {

        @Test
        @DisplayName("Should inherit from LoanServiceException")
        void shouldInheritFromLoanServiceException() {
            // When
            SystemConfigurationException exception = new SystemConfigurationException(
                    "Component", "parameter", "technical", "user"
            );

            // Then
            assertTrue(exception instanceof LoanServiceException);
            assertTrue(exception instanceof RuntimeException);
            assertNotNull(exception.getMessage()); // Inherited from parent
        }

        @Test
        @DisplayName("Should create different instances with factory methods")
        void shouldCreateDifferentInstancesWithFactoryMethods() {
            // When
            SystemConfigurationException missing = SystemConfigurationException.missingProperty("prop");
            SystemConfigurationException invalid = SystemConfigurationException.invalidPropertyValue("prop", "val", "format");
            SystemConfigurationException database = SystemConfigurationException.databaseConfiguration("param", "msg", null);
            SystemConfigurationException security = SystemConfigurationException.securityConfiguration("param", "msg");
            SystemConfigurationException messaging = SystemConfigurationException.messagingConfiguration("param", "msg", null);
            SystemConfigurationException loanRules = SystemConfigurationException.loanRulesConfiguration("rule", "msg");

            // Then
            assertNotSame(missing, invalid);
            assertNotSame(invalid, database);
            assertNotSame(database, security);
            assertNotSame(security, messaging);
            assertNotSame(messaging, loanRules);

            // All should have the same error code
            assertEquals("SYSTEM_CONFIGURATION_ERROR", missing.getErrorCode());
            assertEquals("SYSTEM_CONFIGURATION_ERROR", invalid.getErrorCode());
            assertEquals("SYSTEM_CONFIGURATION_ERROR", database.getErrorCode());
            assertEquals("SYSTEM_CONFIGURATION_ERROR", security.getErrorCode());
            assertEquals("SYSTEM_CONFIGURATION_ERROR", messaging.getErrorCode());
            assertEquals("SYSTEM_CONFIGURATION_ERROR", loanRules.getErrorCode());
        }

        @Test
        @DisplayName("Should have correct component names for each factory method")
        void shouldHaveCorrectComponentNamesForEachFactoryMethod() {
            // When
            SystemConfigurationException missing = SystemConfigurationException.missingProperty("prop");
            SystemConfigurationException invalid = SystemConfigurationException.invalidPropertyValue("prop", "val", "format");
            SystemConfigurationException database = SystemConfigurationException.databaseConfiguration("param", "msg", null);
            SystemConfigurationException security = SystemConfigurationException.securityConfiguration("param", "msg");
            SystemConfigurationException messaging = SystemConfigurationException.messagingConfiguration("param", "msg", null);
            SystemConfigurationException loanRules = SystemConfigurationException.loanRulesConfiguration("rule", "msg");

            // Then
            assertEquals("ApplicationProperties", missing.getComponent());
            assertEquals("ApplicationProperties", invalid.getComponent());
            assertEquals("Database", database.getComponent());
            assertEquals("Security", security.getComponent());
            assertEquals("Messaging", messaging.getComponent());
            assertEquals("LoanRules", loanRules.getComponent());
        }

        @Test
        @DisplayName("Should handle cause chain correctly")
        void shouldHandleCauseChainCorrectly() {
            // Given
            RuntimeException rootCause = new RuntimeException("Root cause");
            IllegalStateException intermediateCause = new IllegalStateException("Intermediate", rootCause);

            // When
            SystemConfigurationException exception = new SystemConfigurationException(
                    "Component", "parameter", "technical", "user", intermediateCause
            );

            // Then
            assertEquals(intermediateCause, exception.getCause());
            assertEquals(rootCause, exception.getCause().getCause());
        }

        @Test
        @DisplayName("Should format technical messages correctly in factory methods")
        void shouldFormatTechnicalMessagesCorrectlyInFactoryMethods() {
            // Given
            String propertyName = "test.property";
            String value = "invalid_value";
            String expectedFormat = "number";
            String ruleName = "testRule";
            String ruleMessage = "rule not found";

            // When
            SystemConfigurationException missing = SystemConfigurationException.missingProperty(propertyName);
            SystemConfigurationException invalid = SystemConfigurationException.invalidPropertyValue(propertyName, value, expectedFormat);
            SystemConfigurationException loanRules = SystemConfigurationException.loanRulesConfiguration(ruleName, ruleMessage);

            // Then
            assertTrue(missing.getTechnicalMessage().contains(propertyName));
            assertTrue(invalid.getTechnicalMessage().contains(propertyName));
            assertTrue(invalid.getTechnicalMessage().contains(value));
            assertTrue(invalid.getTechnicalMessage().contains(expectedFormat));
            assertTrue(loanRules.getTechnicalMessage().contains(ruleName));
            assertTrue(loanRules.getTechnicalMessage().contains(ruleMessage));
        }

        @Test
        @DisplayName("Should have appropriate user messages for different components")
        void shouldHaveAppropriateUserMessagesForDifferentComponents() {
            // When
            SystemConfigurationException missing = SystemConfigurationException.missingProperty("prop");
            SystemConfigurationException database = SystemConfigurationException.databaseConfiguration("param", "msg", null);
            SystemConfigurationException security = SystemConfigurationException.securityConfiguration("param", "msg");
            SystemConfigurationException messaging = SystemConfigurationException.messagingConfiguration("param", "msg", null);
            SystemConfigurationException loanRules = SystemConfigurationException.loanRulesConfiguration("rule", "msg");

            // Then - All messages should be in Spanish and user-friendly
            assertTrue(missing.getUserMessage().contains("configuración"));
            assertTrue(database.getUserMessage().contains("base de datos"));
            assertTrue(security.getUserMessage().contains("seguridad"));
            assertTrue(messaging.getUserMessage().contains("mensajería"));
            assertTrue(loanRules.getUserMessage().contains("reglas de negocio"));

            // Should contain appropriate guidance
            assertTrue(missing.getUserMessage().contains("administrador"));
            assertTrue(database.getUserMessage().contains("temporalmente"));
            assertTrue(security.getUserMessage().contains("administrador"));
            assertTrue(messaging.getUserMessage().contains("notificaciones"));
            assertTrue(loanRules.getUserMessage().contains("administrador"));
        }

        @Test
        @DisplayName("Should maintain immutability of component and parameter information")
        void shouldMaintainImmutabilityOfComponentAndParameterInformation() {
            // Given
            String originalComponent = "TestComponent";
            String originalParameter = "testParameter";

            // When
            SystemConfigurationException exception = new SystemConfigurationException(
                    originalComponent, originalParameter, "technical", "user"
            );

            // Then
            assertEquals(originalComponent, exception.getComponent());
            assertEquals(originalParameter, exception.getParameter());

            // Multiple calls should return same values
            assertSame(exception.getComponent(), exception.getComponent());
            assertSame(exception.getParameter(), exception.getParameter());
        }
    }
}