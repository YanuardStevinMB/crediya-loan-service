package com.crediya.loan.usecase.shared.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ResourceNotFoundException Tests")
class ResourceNotFoundExceptionTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create exception with all parameters")
        void shouldCreateExceptionWithAllParameters() {
            // Given
            String resourceType = "Document";
            String resourceId = "doc123";
            String technicalMessage = "Document with ID doc123 not found in database";
            String userMessage = "El documento solicitado no existe";

            // When
            ResourceNotFoundException exception = new ResourceNotFoundException(
                    resourceType, resourceId, technicalMessage, userMessage
            );

            // Then
            assertEquals("RESOURCE_NOT_FOUND", exception.getErrorCode());
            assertEquals(resourceType, exception.getResourceType());
            assertEquals(resourceId, exception.getResourceId());
            assertEquals(technicalMessage, exception.getTechnicalMessage());
            assertEquals(userMessage, exception.getUserMessage());
        }

        @Test
        @DisplayName("Should handle null resource type")
        void shouldHandleNullResourceType() {
            // When
            ResourceNotFoundException exception = new ResourceNotFoundException(
                    null, "id123", "technical", "user"
            );

            // Then
            assertEquals("RESOURCE_NOT_FOUND", exception.getErrorCode());
            assertNull(exception.getResourceType());
            assertEquals("id123", exception.getResourceId());
        }

        @Test
        @DisplayName("Should handle null resource id")
        void shouldHandleNullResourceId() {
            // When
            ResourceNotFoundException exception = new ResourceNotFoundException(
                    "Resource", null, "technical", "user"
            );

            // Then
            assertEquals("RESOURCE_NOT_FOUND", exception.getErrorCode());
            assertEquals("Resource", exception.getResourceType());
            assertNull(exception.getResourceId());
        }

        @Test
        @DisplayName("Should handle empty strings")
        void shouldHandleEmptyStrings() {
            // When
            ResourceNotFoundException exception = new ResourceNotFoundException(
                    "", "", "technical", "user"
            );

            // Then
            assertEquals("RESOURCE_NOT_FOUND", exception.getErrorCode());
            assertEquals("", exception.getResourceType());
            assertEquals("", exception.getResourceId());
        }
    }

    @Nested
    @DisplayName("Factory Methods Tests")
    class FactoryMethodsTests {

        @Test
        @DisplayName("Should create application not found exception")
        void shouldCreateApplicationNotFoundException() {
            // Given
            String applicationId = "app456";

            // When
            ResourceNotFoundException exception = ResourceNotFoundException.application(applicationId);

            // Then
            assertEquals("RESOURCE_NOT_FOUND", exception.getErrorCode());
            assertEquals("Application", exception.getResourceType());
            assertEquals(applicationId, exception.getResourceId());
            assertTrue(exception.getTechnicalMessage().contains("Application with ID app456 not found"));
            assertEquals("La solicitud de préstamo no fue encontrada", exception.getUserMessage());
        }

        @Test
        @DisplayName("Should create user not found exception")
        void shouldCreateUserNotFoundException() {
            // Given
            String userId = "user789";

            // When
            ResourceNotFoundException exception = ResourceNotFoundException.user(userId);

            // Then
            assertEquals("RESOURCE_NOT_FOUND", exception.getErrorCode());
            assertEquals("User", exception.getResourceType());
            assertEquals(userId, exception.getResourceId());
            assertTrue(exception.getTechnicalMessage().contains("User with ID user789 not found"));
            assertEquals("El usuario no fue encontrado en el sistema", exception.getUserMessage());
        }

        @Test
        @DisplayName("Should create loan type not found exception")
        void shouldCreateLoanTypeNotFoundException() {
            // Given
            String loanTypeId = "personal-loan";

            // When
            ResourceNotFoundException exception = ResourceNotFoundException.loanType(loanTypeId);

            // Then
            assertEquals("RESOURCE_NOT_FOUND", exception.getErrorCode());
            assertEquals("LoanType", exception.getResourceType());
            assertEquals(loanTypeId, exception.getResourceId());
            assertTrue(exception.getTechnicalMessage().contains("Loan type with ID personal-loan not found"));
            assertEquals("El tipo de préstamo solicitado no existe", exception.getUserMessage());
        }

        @Test
        @DisplayName("Should create borrowing capacity not found exception")
        void shouldCreateBorrowingCapacityNotFoundException() {
            // Given
            String userId = "user123";

            // When
            ResourceNotFoundException exception = ResourceNotFoundException.borrowingCapacity(userId);

            // Then
            assertEquals("RESOURCE_NOT_FOUND", exception.getErrorCode());
            assertEquals("BorrowingCapacity", exception.getResourceType());
            assertEquals(userId, exception.getResourceId());
            assertTrue(exception.getTechnicalMessage().contains("Borrowing capacity not found for user user123"));
            assertEquals(
                    "No se encontró información de capacidad de endeudamiento para este usuario",
                    exception.getUserMessage()
            );
        }

        @Test
        @DisplayName("Should create state not found exception")
        void shouldCreateStateNotFoundException() {
            // Given
            String stateId = "bogota";

            // When
            ResourceNotFoundException exception = ResourceNotFoundException.state(stateId);

            // Then
            assertEquals("RESOURCE_NOT_FOUND", exception.getErrorCode());
            assertEquals("State", exception.getResourceType());
            assertEquals(stateId, exception.getResourceId());
            assertTrue(exception.getTechnicalMessage().contains("State with ID bogota not found"));
            assertEquals("El estado especificado no existe en el sistema", exception.getUserMessage());
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle null parameters in factory methods")
        void shouldHandleNullParametersInFactoryMethods() {
            // When
            ResourceNotFoundException appException = ResourceNotFoundException.application(null);
            ResourceNotFoundException userException = ResourceNotFoundException.user(null);
            ResourceNotFoundException loanTypeException = ResourceNotFoundException.loanType(null);
            ResourceNotFoundException borrowingException = ResourceNotFoundException.borrowingCapacity(null);
            ResourceNotFoundException stateException = ResourceNotFoundException.state(null);

            // Then
            assertNull(appException.getResourceId());
            assertNull(userException.getResourceId());
            assertNull(loanTypeException.getResourceId());
            assertNull(borrowingException.getResourceId());
            assertNull(stateException.getResourceId());

            // Resource types should still be set
            assertEquals("Application", appException.getResourceType());
            assertEquals("User", userException.getResourceType());
            assertEquals("LoanType", loanTypeException.getResourceType());
            assertEquals("BorrowingCapacity", borrowingException.getResourceType());
            assertEquals("State", stateException.getResourceType());
        }

        @Test
        @DisplayName("Should handle empty string parameters in factory methods")
        void shouldHandleEmptyStringParametersInFactoryMethods() {
            // Given
            String emptyId = "";

            // When
            ResourceNotFoundException appException = ResourceNotFoundException.application(emptyId);
            ResourceNotFoundException userException = ResourceNotFoundException.user(emptyId);
            ResourceNotFoundException loanTypeException = ResourceNotFoundException.loanType(emptyId);
            ResourceNotFoundException borrowingException = ResourceNotFoundException.borrowingCapacity(emptyId);
            ResourceNotFoundException stateException = ResourceNotFoundException.state(emptyId);

            // Then
            assertEquals("", appException.getResourceId());
            assertEquals("", userException.getResourceId());
            assertEquals("", loanTypeException.getResourceId());
            assertEquals("", borrowingException.getResourceId());
            assertEquals("", stateException.getResourceId());

            // Technical messages should contain the empty string
            assertNotNull(appException.getTechnicalMessage());
            assertNotNull(userException.getTechnicalMessage());
            assertNotNull(loanTypeException.getTechnicalMessage());
            assertNotNull(borrowingException.getTechnicalMessage());
            assertNotNull(stateException.getTechnicalMessage());
        }

        @Test
        @DisplayName("Should handle special characters in resource IDs")
        void shouldHandleSpecialCharactersInResourceIds() {
            // Given
            String specialId = "resource@#$%^&*()_+-=[]{}|;':\",./<>?`~";

            // When
            ResourceNotFoundException exception = ResourceNotFoundException.application(specialId);

            // Then
            assertEquals(specialId, exception.getResourceId());
            assertTrue(exception.getTechnicalMessage().contains(specialId));
        }

        @Test
        @DisplayName("Should handle very long resource IDs")
        void shouldHandleVeryLongResourceIds() {
            // Given
            String longId = "a".repeat(1000);

            // When
            ResourceNotFoundException exception = ResourceNotFoundException.user(longId);

            // Then
            assertEquals(longId, exception.getResourceId());
            assertTrue(exception.getTechnicalMessage().contains(longId));
        }

        @Test
        @DisplayName("Should handle whitespace in resource IDs")
        void shouldHandleWhitespaceInResourceIds() {
            // Given
            String whitespaceId = "  resource with spaces  ";

            // When
            ResourceNotFoundException exception = ResourceNotFoundException.loanType(whitespaceId);

            // Then
            assertEquals(whitespaceId, exception.getResourceId());
            assertTrue(exception.getTechnicalMessage().contains(whitespaceId));
        }
    }

    @Nested
    @DisplayName("Inheritance and General Tests")
    class InheritanceAndGeneralTests {

        @Test
        @DisplayName("Should inherit from LoanServiceException")
        void shouldInheritFromLoanServiceException() {
            // When
            ResourceNotFoundException exception = ResourceNotFoundException.application("123");

            // Then
            assertTrue(exception instanceof LoanServiceException);
            assertTrue(exception instanceof RuntimeException);
            assertNotNull(exception.getMessage()); // Inherited from parent
        }

        @Test
        @DisplayName("Should create different instances with factory methods")
        void shouldCreateDifferentInstancesWithFactoryMethods() {
            // When
            ResourceNotFoundException app = ResourceNotFoundException.application("app1");
            ResourceNotFoundException user = ResourceNotFoundException.user("user1");
            ResourceNotFoundException loanType = ResourceNotFoundException.loanType("type1");
            ResourceNotFoundException borrowing = ResourceNotFoundException.borrowingCapacity("user1");
            ResourceNotFoundException state = ResourceNotFoundException.state("state1");

            // Then
            assertNotSame(app, user);
            assertNotSame(user, loanType);
            assertNotSame(loanType, borrowing);
            assertNotSame(borrowing, state);

            // All should have the same error code
            assertEquals("RESOURCE_NOT_FOUND", app.getErrorCode());
            assertEquals("RESOURCE_NOT_FOUND", user.getErrorCode());
            assertEquals("RESOURCE_NOT_FOUND", loanType.getErrorCode());
            assertEquals("RESOURCE_NOT_FOUND", borrowing.getErrorCode());
            assertEquals("RESOURCE_NOT_FOUND", state.getErrorCode());
        }

        @Test
        @DisplayName("Should have different resource types for each factory method")
        void shouldHaveDifferentResourceTypesForEachFactoryMethod() {
            // When
            ResourceNotFoundException app = ResourceNotFoundException.application("id");
            ResourceNotFoundException user = ResourceNotFoundException.user("id");
            ResourceNotFoundException loanType = ResourceNotFoundException.loanType("id");
            ResourceNotFoundException borrowing = ResourceNotFoundException.borrowingCapacity("id");
            ResourceNotFoundException state = ResourceNotFoundException.state("id");

            // Then
            assertEquals("Application", app.getResourceType());
            assertEquals("User", user.getResourceType());
            assertEquals("LoanType", loanType.getResourceType());
            assertEquals("BorrowingCapacity", borrowing.getResourceType());
            assertEquals("State", state.getResourceType());
        }

        @Test
        @DisplayName("Should format technical messages consistently")
        void shouldFormatTechnicalMessagesConsistently() {
            // Given
            String resourceId = "test123";

            // When
            ResourceNotFoundException app = ResourceNotFoundException.application(resourceId);
            ResourceNotFoundException user = ResourceNotFoundException.user(resourceId);
            ResourceNotFoundException loanType = ResourceNotFoundException.loanType(resourceId);
            ResourceNotFoundException borrowing = ResourceNotFoundException.borrowingCapacity(resourceId);
            ResourceNotFoundException state = ResourceNotFoundException.state(resourceId);

            // Then
            assertTrue(app.getTechnicalMessage().contains(resourceId));
            assertTrue(user.getTechnicalMessage().contains(resourceId));
            assertTrue(loanType.getTechnicalMessage().contains(resourceId));
            assertTrue(borrowing.getTechnicalMessage().contains(resourceId));
            assertTrue(state.getTechnicalMessage().contains(resourceId));

            // Each should mention the resource type
            assertTrue(app.getTechnicalMessage().toLowerCase().contains("application"));
            assertTrue(user.getTechnicalMessage().toLowerCase().contains("user"));
            assertTrue(loanType.getTechnicalMessage().toLowerCase().contains("loan type"));
            assertTrue(borrowing.getTechnicalMessage().toLowerCase().contains("borrowing capacity"));
            assertTrue(state.getTechnicalMessage().toLowerCase().contains("state"));
        }

        @Test
        @DisplayName("Should have appropriate user messages")
        void shouldHaveAppropriateUserMessages() {
            // When
            ResourceNotFoundException app = ResourceNotFoundException.application("id");
            ResourceNotFoundException user = ResourceNotFoundException.user("id");
            ResourceNotFoundException loanType = ResourceNotFoundException.loanType("id");
            ResourceNotFoundException borrowing = ResourceNotFoundException.borrowingCapacity("id");
            ResourceNotFoundException state = ResourceNotFoundException.state("id");

            // Then - All user messages should be in Spanish and user-friendly
            assertTrue(app.getUserMessage().contains("solicitud"));
            assertTrue(user.getUserMessage().contains("usuario"));
            assertTrue(loanType.getUserMessage().contains("préstamo"));
            assertTrue(borrowing.getUserMessage().contains("capacidad"));
            assertTrue(state.getUserMessage().contains("estado"));

            // Should not contain technical terms
            assertFalse(app.getUserMessage().contains("ID"));
            assertFalse(user.getUserMessage().contains("database"));
            assertFalse(loanType.getUserMessage().contains("repository"));
        }

        @Test
        @DisplayName("Should maintain immutability of resource information")
        void shouldMaintainImmutabilityOfResourceInformation() {
            // Given
            String originalType = "TestResource";
            String originalId = "test123";

            // When
            ResourceNotFoundException exception = new ResourceNotFoundException(
                    originalType, originalId, "technical", "user"
            );

            // Then - getters should return the same values consistently
            assertEquals(originalType, exception.getResourceType());
            assertEquals(originalId, exception.getResourceId());

            // Multiple calls should return same values
            assertSame(exception.getResourceType(), exception.getResourceType());
            assertSame(exception.getResourceId(), exception.getResourceId());
        }
    }
}