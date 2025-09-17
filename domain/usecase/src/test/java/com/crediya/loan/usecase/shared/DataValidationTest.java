package com.crediya.loan.usecase.shared;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DataValidation - Test de constantes de validación de datos")
class DataValidationTest {

    @Test
    @DisplayName("Debe validar que las constantes de códigos de estado tienen los valores correctos")
    void testStatusCodes() {
        // Given & When & Then
        assertEquals("PEN", DataValidation.PENDING_STATUS_CODE,
                "El código de estado pendiente debe ser 'PEN'");
        
        assertEquals("APROB", DataValidation.APROB_STATUS_CODE,
                "El código de estado aprobado debe ser 'APROB'");
        
        assertEquals("RECH", DataValidation.RECH_STATUS_CODE,
                "El código de estado rechazado debe ser 'RECH'");
    }

    @Test
    @DisplayName("Debe validar que las constantes de descripciones de estado tienen los valores correctos")
    void testStatusDescriptions() {
        // Given & When & Then
        assertEquals("PENDIENTE", DataValidation.PENDING_STATUS,
                "La descripción del estado pendiente debe ser 'PENDIENTE'");
        
        assertEquals("APROBADO", DataValidation.APROB_STATUS,
                "La descripción del estado aprobado debe ser 'APROBADO'");
        
        assertEquals("RECHAZADO", DataValidation.RECH_STATUS,
                "La descripción del estado rechazado debe ser 'RECHAZADO'");
    }

    @Test
    @DisplayName("Debe validar que todas las constantes de códigos son diferentes entre sí")
    void testStatusCodesAreUnique() {
        // Given & When & Then
        assertNotEquals(DataValidation.PENDING_STATUS_CODE, DataValidation.APROB_STATUS_CODE,
                "Los códigos PENDING y APROB deben ser diferentes");
        
        assertNotEquals(DataValidation.PENDING_STATUS_CODE, DataValidation.RECH_STATUS_CODE,
                "Los códigos PENDING y RECH deben ser diferentes");
        
        assertNotEquals(DataValidation.APROB_STATUS_CODE, DataValidation.RECH_STATUS_CODE,
                "Los códigos APROB y RECH deben ser diferentes");
    }

    @Test
    @DisplayName("Debe validar que todas las descripciones de estado son diferentes entre sí")
    void testStatusDescriptionsAreUnique() {
        // Given & When & Then
        assertNotEquals(DataValidation.PENDING_STATUS, DataValidation.APROB_STATUS,
                "Las descripciones PENDING y APROB deben ser diferentes");
        
        assertNotEquals(DataValidation.PENDING_STATUS, DataValidation.RECH_STATUS,
                "Las descripciones PENDING y RECH deben ser diferentes");
        
        assertNotEquals(DataValidation.APROB_STATUS, DataValidation.RECH_STATUS,
                "Las descripciones APROB y RECH deben ser diferentes");
    }

    @Test
    @DisplayName("Debe validar que las constantes no son null")
    void testConstantsAreNotNull() {
        // Given & When & Then
        assertNotNull(DataValidation.PENDING_STATUS_CODE, "PENDING_STATUS_CODE no debe ser null");
        assertNotNull(DataValidation.APROB_STATUS_CODE, "APROB_STATUS_CODE no debe ser null");
        assertNotNull(DataValidation.RECH_STATUS_CODE, "RECH_STATUS_CODE no debe ser null");
        
        assertNotNull(DataValidation.PENDING_STATUS, "PENDING_STATUS no debe ser null");
        assertNotNull(DataValidation.APROB_STATUS, "APROB_STATUS no debe ser null");
        assertNotNull(DataValidation.RECH_STATUS, "RECH_STATUS no debe ser null");
    }

    @Test
    @DisplayName("Debe validar que las constantes no son cadenas vacías")
    void testConstantsAreNotEmpty() {
        // Given & When & Then
        assertFalse(DataValidation.PENDING_STATUS_CODE.isEmpty(), 
                "PENDING_STATUS_CODE no debe ser cadena vacía");
        assertFalse(DataValidation.APROB_STATUS_CODE.isEmpty(), 
                "APROB_STATUS_CODE no debe ser cadena vacía");
        assertFalse(DataValidation.RECH_STATUS_CODE.isEmpty(), 
                "RECH_STATUS_CODE no debe ser cadena vacía");
        
        assertFalse(DataValidation.PENDING_STATUS.isEmpty(), 
                "PENDING_STATUS no debe ser cadena vacía");
        assertFalse(DataValidation.APROB_STATUS.isEmpty(), 
                "APROB_STATUS no debe ser cadena vacía");
        assertFalse(DataValidation.RECH_STATUS.isEmpty(), 
                "RECH_STATUS no debe ser cadena vacía");
    }

    @Test
    @DisplayName("Debe validar que los campos son públicos, estáticos y finales")
    void testFieldsArePublicStaticFinal() throws NoSuchFieldException {
        // Given
        Class<DataValidation> clazz = DataValidation.class;
        
        // When & Then - Validar códigos de estado
        Field pendingStatusCodeField = clazz.getField("PENDING_STATUS_CODE");
        assertTrue(Modifier.isPublic(pendingStatusCodeField.getModifiers()), 
                "PENDING_STATUS_CODE debe ser público");
        assertTrue(Modifier.isStatic(pendingStatusCodeField.getModifiers()), 
                "PENDING_STATUS_CODE debe ser estático");
        assertTrue(Modifier.isFinal(pendingStatusCodeField.getModifiers()), 
                "PENDING_STATUS_CODE debe ser final");

        Field aprobStatusCodeField = clazz.getField("APROB_STATUS_CODE");
        assertTrue(Modifier.isPublic(aprobStatusCodeField.getModifiers()), 
                "APROB_STATUS_CODE debe ser público");
        assertTrue(Modifier.isStatic(aprobStatusCodeField.getModifiers()), 
                "APROB_STATUS_CODE debe ser estático");
        assertTrue(Modifier.isFinal(aprobStatusCodeField.getModifiers()), 
                "APROB_STATUS_CODE debe ser final");

        Field rechStatusCodeField = clazz.getField("RECH_STATUS_CODE");
        assertTrue(Modifier.isPublic(rechStatusCodeField.getModifiers()), 
                "RECH_STATUS_CODE debe ser público");
        assertTrue(Modifier.isStatic(rechStatusCodeField.getModifiers()), 
                "RECH_STATUS_CODE debe ser estático");
        assertTrue(Modifier.isFinal(rechStatusCodeField.getModifiers()), 
                "RECH_STATUS_CODE debe ser final");

        // When & Then - Validar descripciones de estado
        Field pendingStatusField = clazz.getField("PENDING_STATUS");
        assertTrue(Modifier.isPublic(pendingStatusField.getModifiers()), 
                "PENDING_STATUS debe ser público");
        assertTrue(Modifier.isStatic(pendingStatusField.getModifiers()), 
                "PENDING_STATUS debe ser estático");
        assertTrue(Modifier.isFinal(pendingStatusField.getModifiers()), 
                "PENDING_STATUS debe ser final");

        Field aprobStatusField = clazz.getField("APROB_STATUS");
        assertTrue(Modifier.isPublic(aprobStatusField.getModifiers()), 
                "APROB_STATUS debe ser público");
        assertTrue(Modifier.isStatic(aprobStatusField.getModifiers()), 
                "APROB_STATUS debe ser estático");
        assertTrue(Modifier.isFinal(aprobStatusField.getModifiers()), 
                "APROB_STATUS debe ser final");

        Field rechStatusField = clazz.getField("RECH_STATUS");
        assertTrue(Modifier.isPublic(rechStatusField.getModifiers()), 
                "RECH_STATUS debe ser público");
        assertTrue(Modifier.isStatic(rechStatusField.getModifiers()), 
                "RECH_STATUS debe ser estático");
        assertTrue(Modifier.isFinal(rechStatusField.getModifiers()), 
                "RECH_STATUS debe ser final");
    }

    @Test
    @DisplayName("Debe validar que el tipo de los campos es String")
    void testFieldsAreStringType() throws NoSuchFieldException {
        // Given
        Class<DataValidation> clazz = DataValidation.class;
        
        // When & Then
        assertEquals(String.class, clazz.getField("PENDING_STATUS_CODE").getType(), 
                "PENDING_STATUS_CODE debe ser de tipo String");
        assertEquals(String.class, clazz.getField("APROB_STATUS_CODE").getType(), 
                "APROB_STATUS_CODE debe ser de tipo String");
        assertEquals(String.class, clazz.getField("RECH_STATUS_CODE").getType(), 
                "RECH_STATUS_CODE debe ser de tipo String");
        
        assertEquals(String.class, clazz.getField("PENDING_STATUS").getType(), 
                "PENDING_STATUS debe ser de tipo String");
        assertEquals(String.class, clazz.getField("APROB_STATUS").getType(), 
                "APROB_STATUS debe ser de tipo String");
        assertEquals(String.class, clazz.getField("RECH_STATUS").getType(), 
                "RECH_STATUS debe ser de tipo String");
    }

    @Test
    @DisplayName("Debe validar que la clase tiene exactamente 6 campos públicos")
    void testClassHasExpectedNumberOfFields() {
        // Given
        Class<DataValidation> clazz = DataValidation.class;
        
        // When
        Field[] publicFields = clazz.getFields();
        
        // Then
        assertEquals(6, publicFields.length, 
                "La clase DataValidation debe tener exactamente 6 campos públicos");
    }

    @Test
    @DisplayName("Debe validar que la clase es pública y no final")
    void testClassModifiers() {
        // Given
        Class<DataValidation> clazz = DataValidation.class;
        
        // When & Then
        assertTrue(Modifier.isPublic(clazz.getModifiers()), 
                "La clase DataValidation debe ser pública");
        assertFalse(Modifier.isFinal(clazz.getModifiers()), 
                "La clase DataValidation no debe ser final");
    }

    @Test
    @DisplayName("Debe validar que la clase tiene un constructor público sin argumentos")
    void testDefaultConstructor() {
        // Given & When & Then
        assertDoesNotThrow(() -> {
            Constructor<DataValidation> constructor = DataValidation.class.getConstructor();
            assertTrue(Modifier.isPublic(constructor.getModifiers()), 
                    "El constructor por defecto debe ser público");
            
            // Verificar que se puede instanciar
            DataValidation instance = constructor.newInstance();
            assertNotNull(instance, "Se debe poder crear una instancia de DataValidation");
        }, "Debe existir un constructor público sin argumentos");
    }

    @Test
    @DisplayName("Debe validar que las constantes pueden ser accedidas directamente desde la clase")
    void testConstantsAccessibility() {
        // Given & When & Then - Se puede acceder sin instanciar la clase
        assertDoesNotThrow(() -> {
            String pendingCode = DataValidation.PENDING_STATUS_CODE;
            String aprobCode = DataValidation.APROB_STATUS_CODE;
            String rechCode = DataValidation.RECH_STATUS_CODE;
            String pendingStatus = DataValidation.PENDING_STATUS;
            String aprobStatus = DataValidation.APROB_STATUS;
            String rechStatus = DataValidation.RECH_STATUS;
            
            // Verificar que no son null
            assertNotNull(pendingCode);
            assertNotNull(aprobCode);
            assertNotNull(rechCode);
            assertNotNull(pendingStatus);
            assertNotNull(aprobStatus);
            assertNotNull(rechStatus);
        }, "Las constantes deben ser accesibles directamente desde la clase");
    }

    @Test
    @DisplayName("Debe validar la coherencia entre códigos y descripciones de estado")
    void testStatusConsistency() {
        // Given & When & Then - Los códigos y descripciones deben estar relacionados lógicamente
        
        // PENDING
        assertTrue(DataValidation.PENDING_STATUS_CODE.startsWith("P"), 
                "El código de estado pendiente debe comenzar con 'P'");
        assertTrue(DataValidation.PENDING_STATUS.startsWith("P"), 
                "La descripción de estado pendiente debe comenzar con 'P'");
        
        // APROB  
        assertTrue(DataValidation.APROB_STATUS_CODE.startsWith("A"), 
                "El código de estado aprobado debe comenzar con 'A'");
        assertTrue(DataValidation.APROB_STATUS.startsWith("A"), 
                "La descripción de estado aprobado debe comenzar con 'A'");
        
        // RECH
        assertTrue(DataValidation.RECH_STATUS_CODE.startsWith("R"), 
                "El código de estado rechazado debe comenzar con 'R'");
        assertTrue(DataValidation.RECH_STATUS.startsWith("R"), 
                "La descripción de estado rechazado debe comenzar con 'R'");
    }
}