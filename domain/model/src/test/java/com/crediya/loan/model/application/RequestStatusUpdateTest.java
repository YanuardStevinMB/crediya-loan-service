package com.crediya.loan.model.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RequestStatusUpdateTest {

    @Test
    @DisplayName("NoArgs + setters/getters deben funcionar")
    void noArgsAndSettersGetters_work() {
        RequestStatusUpdate request = new RequestStatusUpdate();
        request.setId(1L);
        request.setStateId(10L);

        assertEquals(1L, request.getId());
        assertEquals(10L, request.getStateId());
    }

    @Test
    @DisplayName("AllArgsConstructor debe setear todos los campos")
    void allArgsConstructor_setsAllFields() {
        RequestStatusUpdate request = new RequestStatusUpdate(2L, 20L);

        assertEquals(2L, request.getId());
        assertEquals(20L, request.getStateId());
    }

    @Test
    @DisplayName("Builder debe construir correctamente")
    void builder_buildsCorrectly() {
        RequestStatusUpdate request = RequestStatusUpdate.builder()
                .id(3L)
                .stateId(30L)
                .build();

        assertEquals(3L, request.getId());
        assertEquals(30L, request.getStateId());
    }

    @Test
    @DisplayName("Builder acepta nulos sin lanzar excepción")
    void builder_allowsNulls() {
        RequestStatusUpdate request = RequestStatusUpdate.builder()
                .id(null)
                .stateId(null)
                .build();

        assertNull(request.getId());
        assertNull(request.getStateId());
    }

    @Test
    @DisplayName("toString debe generar representación correcta")
    void toString_generatesCorrectRepresentation() {
        RequestStatusUpdate request = RequestStatusUpdate.builder()
                .id(4L)
                .stateId(40L)
                .build();

        String toString = request.toString();
        
        assertNotNull(toString);
        assertTrue(toString.contains("RequestStatusUpdate"));
        assertTrue(toString.contains("id=4"));
        assertTrue(toString.contains("stateId=40"));
    }

    @Test
    @DisplayName("toString debe manejar valores nulos correctamente")
    void toString_handlesNullsCorrectly() {
        RequestStatusUpdate request = new RequestStatusUpdate();
        String toString = request.toString();
        
        assertNotNull(toString);
        assertTrue(toString.contains("RequestStatusUpdate"));
        // No debe lanzar excepción aunque tenga campos nulos
    }

    @Test
    @DisplayName("Debe manejar valores cero correctamente")
    void builder_handlesZeroValues() {
        RequestStatusUpdate request = RequestStatusUpdate.builder()
                .id(0L)
                .stateId(0L)
                .build();

        assertEquals(0L, request.getId());
        assertEquals(0L, request.getStateId());
    }

    @Test
    @DisplayName("Debe manejar valores negativos (casos edge)")
    void builder_handlesNegativeValues() {
        // Aunque no sea lógico en el contexto de negocio, el modelo debe soportarlo
        RequestStatusUpdate request = RequestStatusUpdate.builder()
                .id(-1L)
                .stateId(-5L)
                .build();

        assertEquals(-1L, request.getId());
        assertEquals(-5L, request.getStateId());
    }

    @Test
    @DisplayName("Debe manejar números altos correctamente")
    void builder_handlesLargeNumbers() {
        RequestStatusUpdate request = RequestStatusUpdate.builder()
                .id(Long.MAX_VALUE)
                .stateId(999999999L)
                .build();

        assertEquals(Long.MAX_VALUE, request.getId());
        assertEquals(999999999L, request.getStateId());
    }

    @Test
    @DisplayName("Setters deben permitir modificar valores después de construcción")
    void setters_allowModificationAfterConstruction() {
        RequestStatusUpdate request = RequestStatusUpdate.builder()
                .id(5L)
                .stateId(50L)
                .build();

        // Valores iniciales
        assertEquals(5L, request.getId());
        assertEquals(50L, request.getStateId());

        // Modificar con setters
        request.setId(6L);
        request.setStateId(60L);

        // Verificar cambios
        assertEquals(6L, request.getId());
        assertEquals(60L, request.getStateId());
    }

    @Test
    @DisplayName("Debe manejar transiciones de estado típicas")
    void builder_handlesTypicalStateTransitions() {
        // Estados típicos de una aplicación de préstamo
        Long[] stateIds = {1L, 2L, 3L, 4L, 5L}; // PENDING, IN_REVIEW, APPROVED, REJECTED, CANCELLED
        
        for (int i = 0; i < stateIds.length; i++) {
            RequestStatusUpdate request = RequestStatusUpdate.builder()
                    .id(100L + i) // IDs de aplicaciones diferentes
                    .stateId(stateIds[i])
                    .build();
            
            assertEquals(100L + i, request.getId());
            assertEquals(stateIds[i], request.getStateId());
        }
    }

    @Test
    @DisplayName("Debe permitir múltiples actualizaciones del mismo objeto")
    void setters_allowMultipleUpdates() {
        RequestStatusUpdate request = new RequestStatusUpdate();
        
        // Primera actualización
        request.setId(7L);
        request.setStateId(1L); // PENDING
        assertEquals(7L, request.getId());
        assertEquals(1L, request.getStateId());
        
        // Segunda actualización
        request.setStateId(2L); // IN_REVIEW
        assertEquals(7L, request.getId()); // ID no cambia
        assertEquals(2L, request.getStateId());
        
        // Tercera actualización
        request.setStateId(3L); // APPROVED
        assertEquals(7L, request.getId()); // ID no cambia
        assertEquals(3L, request.getStateId());
    }

    @Test
    @DisplayName("Constructor con parámetros nulos debe funcionar")
    void allArgsConstructor_handlesNulls() {
        RequestStatusUpdate request = new RequestStatusUpdate(null, null);
        
        assertNull(request.getId());
        assertNull(request.getStateId());
    }

    @Test
    @DisplayName("Builder debe crear objetos independientes")
    void builder_createsIndependentObjects() {
        RequestStatusUpdate request1 = RequestStatusUpdate.builder()
                .id(8L)
                .stateId(80L)
                .build();
                
        RequestStatusUpdate request2 = RequestStatusUpdate.builder()
                .id(9L)
                .stateId(90L)
                .build();

        // Objetos deben ser diferentes
        assertNotSame(request1, request2);
        
        // Modificar uno no debe afectar el otro
        request1.setStateId(85L);
        
        assertEquals(85L, request1.getStateId());
        assertEquals(90L, request2.getStateId()); // No debe haber cambiado
    }

    @Test
    @DisplayName("Debe manejar casos de rollback de estado")
    void setters_handleStateRollback() {
        RequestStatusUpdate request = RequestStatusUpdate.builder()
                .id(10L)
                .stateId(4L) // REJECTED
                .build();

        assertEquals(4L, request.getStateId());
        
        // Rollback a estado anterior (caso edge que puede ocurrir en el negocio)
        request.setStateId(2L); // Volver a IN_REVIEW
        
        assertEquals(2L, request.getStateId());
    }
}