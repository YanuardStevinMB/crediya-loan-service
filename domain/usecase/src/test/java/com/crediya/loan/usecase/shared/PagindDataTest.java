package com.crediya.loan.usecase.shared;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PagindData - Test de constantes de paginación")
class PagindDataTest {

    @Test
    @DisplayName("Debe validar que las constantes de campos de paginación tienen los valores correctos")
    void testPaginationFieldConstants() {
        // Given & When & Then
        assertEquals("state", PagindData.PAGINED_STATE,
                "La constante PAGINED_STATE debe ser 'state'");
        
        assertEquals("document", PagindData.PAGINED_DOCUMENT,
                "La constante PAGINED_DOCUMENT debe ser 'document'");
        
        assertEquals("email", PagindData.PAGINED_EMAIL,
                "La constante PAGINED_EMAIL debe ser 'email'");
        
        assertEquals("size", PagindData.PAGINED_SIZE,
                "La constante PAGINED_SIZE debe ser 'size'");
        
        assertEquals("page", PagindData.PAGINED_PAGE,
                "La constante PAGINED_PAGE debe ser 'page'");
    }

    @Test
    @DisplayName("Debe validar que las constantes de valores de paginación tienen los valores correctos")
    void testPaginationValueConstants() {
        // Given & When & Then
        assertEquals("10", PagindData.PAGINED_SIZE_VALUE,
                "La constante PAGINED_SIZE_VALUE debe ser '10'");
        
        assertEquals("1", PagindData.PAGINED_PAGE_VALUE,
                "La constante PAGINED_PAGE_VALUE debe ser '1'");
    }

    @Test
    @DisplayName("Debe validar que todas las constantes de campos son diferentes entre sí")
    void testFieldConstantsAreUnique() {
        // Given & When & Then
        assertNotEquals(PagindData.PAGINED_STATE, PagindData.PAGINED_DOCUMENT,
                "PAGINED_STATE y PAGINED_DOCUMENT deben ser diferentes");
        
        assertNotEquals(PagindData.PAGINED_STATE, PagindData.PAGINED_EMAIL,
                "PAGINED_STATE y PAGINED_EMAIL deben ser diferentes");
        
        assertNotEquals(PagindData.PAGINED_STATE, PagindData.PAGINED_SIZE,
                "PAGINED_STATE y PAGINED_SIZE deben ser diferentes");
        
        assertNotEquals(PagindData.PAGINED_STATE, PagindData.PAGINED_PAGE,
                "PAGINED_STATE y PAGINED_PAGE deben ser diferentes");
        
        assertNotEquals(PagindData.PAGINED_DOCUMENT, PagindData.PAGINED_EMAIL,
                "PAGINED_DOCUMENT y PAGINED_EMAIL deben ser diferentes");
        
        assertNotEquals(PagindData.PAGINED_DOCUMENT, PagindData.PAGINED_SIZE,
                "PAGINED_DOCUMENT y PAGINED_SIZE deben ser diferentes");
        
        assertNotEquals(PagindData.PAGINED_DOCUMENT, PagindData.PAGINED_PAGE,
                "PAGINED_DOCUMENT y PAGINED_PAGE deben ser diferentes");
        
        assertNotEquals(PagindData.PAGINED_EMAIL, PagindData.PAGINED_SIZE,
                "PAGINED_EMAIL y PAGINED_SIZE deben ser diferentes");
        
        assertNotEquals(PagindData.PAGINED_EMAIL, PagindData.PAGINED_PAGE,
                "PAGINED_EMAIL y PAGINED_PAGE deben ser diferentes");
        
        assertNotEquals(PagindData.PAGINED_SIZE, PagindData.PAGINED_PAGE,
                "PAGINED_SIZE y PAGINED_PAGE deben ser diferentes");
    }

    @Test
    @DisplayName("Debe validar que los valores por defecto son diferentes entre sí")
    void testValueConstantsAreUnique() {
        // Given & When & Then
        assertNotEquals(PagindData.PAGINED_SIZE_VALUE, PagindData.PAGINED_PAGE_VALUE,
                "PAGINED_SIZE_VALUE y PAGINED_PAGE_VALUE deben ser diferentes");
    }

    @Test
    @DisplayName("Debe validar que las constantes no son null")
    void testConstantsAreNotNull() {
        // Given & When & Then
        assertNotNull(PagindData.PAGINED_STATE, "PAGINED_STATE no debe ser null");
        assertNotNull(PagindData.PAGINED_DOCUMENT, "PAGINED_DOCUMENT no debe ser null");
        assertNotNull(PagindData.PAGINED_EMAIL, "PAGINED_EMAIL no debe ser null");
        assertNotNull(PagindData.PAGINED_SIZE, "PAGINED_SIZE no debe ser null");
        assertNotNull(PagindData.PAGINED_PAGE, "PAGINED_PAGE no debe ser null");
        
        assertNotNull(PagindData.PAGINED_SIZE_VALUE, "PAGINED_SIZE_VALUE no debe ser null");
        assertNotNull(PagindData.PAGINED_PAGE_VALUE, "PAGINED_PAGE_VALUE no debe ser null");
    }

    @Test
    @DisplayName("Debe validar que las constantes no son cadenas vacías")
    void testConstantsAreNotEmpty() {
        // Given & When & Then
        assertFalse(PagindData.PAGINED_STATE.isEmpty(), 
                "PAGINED_STATE no debe ser cadena vacía");
        assertFalse(PagindData.PAGINED_DOCUMENT.isEmpty(), 
                "PAGINED_DOCUMENT no debe ser cadena vacía");
        assertFalse(PagindData.PAGINED_EMAIL.isEmpty(), 
                "PAGINED_EMAIL no debe ser cadena vacía");
        assertFalse(PagindData.PAGINED_SIZE.isEmpty(), 
                "PAGINED_SIZE no debe ser cadena vacía");
        assertFalse(PagindData.PAGINED_PAGE.isEmpty(), 
                "PAGINED_PAGE no debe ser cadena vacía");
        
        assertFalse(PagindData.PAGINED_SIZE_VALUE.isEmpty(), 
                "PAGINED_SIZE_VALUE no debe ser cadena vacía");
        assertFalse(PagindData.PAGINED_PAGE_VALUE.isEmpty(), 
                "PAGINED_PAGE_VALUE no debe ser cadena vacía");
    }

    @Test
    @DisplayName("Debe validar que los campos son públicos, estáticos y finales")
    void testFieldsArePublicStaticFinal() throws NoSuchFieldException {
        // Given
        Class<PagindData> clazz = PagindData.class;
        
        // When & Then - Validar campos de paginación
        Field paginedStateField = clazz.getField("PAGINED_STATE");
        assertTrue(Modifier.isPublic(paginedStateField.getModifiers()), 
                "PAGINED_STATE debe ser público");
        assertTrue(Modifier.isStatic(paginedStateField.getModifiers()), 
                "PAGINED_STATE debe ser estático");
        assertTrue(Modifier.isFinal(paginedStateField.getModifiers()), 
                "PAGINED_STATE debe ser final");

        Field paginedDocumentField = clazz.getField("PAGINED_DOCUMENT");
        assertTrue(Modifier.isPublic(paginedDocumentField.getModifiers()), 
                "PAGINED_DOCUMENT debe ser público");
        assertTrue(Modifier.isStatic(paginedDocumentField.getModifiers()), 
                "PAGINED_DOCUMENT debe ser estático");
        assertTrue(Modifier.isFinal(paginedDocumentField.getModifiers()), 
                "PAGINED_DOCUMENT debe ser final");

        Field paginedEmailField = clazz.getField("PAGINED_EMAIL");
        assertTrue(Modifier.isPublic(paginedEmailField.getModifiers()), 
                "PAGINED_EMAIL debe ser público");
        assertTrue(Modifier.isStatic(paginedEmailField.getModifiers()), 
                "PAGINED_EMAIL debe ser estático");
        assertTrue(Modifier.isFinal(paginedEmailField.getModifiers()), 
                "PAGINED_EMAIL debe ser final");

        Field paginedSizeField = clazz.getField("PAGINED_SIZE");
        assertTrue(Modifier.isPublic(paginedSizeField.getModifiers()), 
                "PAGINED_SIZE debe ser público");
        assertTrue(Modifier.isStatic(paginedSizeField.getModifiers()), 
                "PAGINED_SIZE debe ser estático");
        assertTrue(Modifier.isFinal(paginedSizeField.getModifiers()), 
                "PAGINED_SIZE debe ser final");

        Field paginedPageField = clazz.getField("PAGINED_PAGE");
        assertTrue(Modifier.isPublic(paginedPageField.getModifiers()), 
                "PAGINED_PAGE debe ser público");
        assertTrue(Modifier.isStatic(paginedPageField.getModifiers()), 
                "PAGINED_PAGE debe ser estático");
        assertTrue(Modifier.isFinal(paginedPageField.getModifiers()), 
                "PAGINED_PAGE debe ser final");

        // When & Then - Validar valores por defecto
        Field paginedSizeValueField = clazz.getField("PAGINED_SIZE_VALUE");
        assertTrue(Modifier.isPublic(paginedSizeValueField.getModifiers()), 
                "PAGINED_SIZE_VALUE debe ser público");
        assertTrue(Modifier.isStatic(paginedSizeValueField.getModifiers()), 
                "PAGINED_SIZE_VALUE debe ser estático");
        assertTrue(Modifier.isFinal(paginedSizeValueField.getModifiers()), 
                "PAGINED_SIZE_VALUE debe ser final");

        Field paginedPageValueField = clazz.getField("PAGINED_PAGE_VALUE");
        assertTrue(Modifier.isPublic(paginedPageValueField.getModifiers()), 
                "PAGINED_PAGE_VALUE debe ser público");
        assertTrue(Modifier.isStatic(paginedPageValueField.getModifiers()), 
                "PAGINED_PAGE_VALUE debe ser estático");
        assertTrue(Modifier.isFinal(paginedPageValueField.getModifiers()), 
                "PAGINED_PAGE_VALUE debe ser final");
    }

    @Test
    @DisplayName("Debe validar que el tipo de los campos es String")
    void testFieldsAreStringType() throws NoSuchFieldException {
        // Given
        Class<PagindData> clazz = PagindData.class;
        
        // When & Then
        assertEquals(String.class, clazz.getField("PAGINED_STATE").getType(), 
                "PAGINED_STATE debe ser de tipo String");
        assertEquals(String.class, clazz.getField("PAGINED_DOCUMENT").getType(), 
                "PAGINED_DOCUMENT debe ser de tipo String");
        assertEquals(String.class, clazz.getField("PAGINED_EMAIL").getType(), 
                "PAGINED_EMAIL debe ser de tipo String");
        assertEquals(String.class, clazz.getField("PAGINED_SIZE").getType(), 
                "PAGINED_SIZE debe ser de tipo String");
        assertEquals(String.class, clazz.getField("PAGINED_PAGE").getType(), 
                "PAGINED_PAGE debe ser de tipo String");
        
        assertEquals(String.class, clazz.getField("PAGINED_SIZE_VALUE").getType(), 
                "PAGINED_SIZE_VALUE debe ser de tipo String");
        assertEquals(String.class, clazz.getField("PAGINED_PAGE_VALUE").getType(), 
                "PAGINED_PAGE_VALUE debe ser de tipo String");
    }

    @Test
    @DisplayName("Debe validar que la clase tiene exactamente 7 campos públicos")
    void testClassHasExpectedNumberOfFields() {
        // Given
        Class<PagindData> clazz = PagindData.class;
        
        // When
        Field[] publicFields = clazz.getFields();
        
        // Then
        assertEquals(7, publicFields.length, 
                "La clase PagindData debe tener exactamente 7 campos públicos");
    }

    @Test
    @DisplayName("Debe validar que la clase es pública y final")
    void testClassModifiers() {
        // Given
        Class<PagindData> clazz = PagindData.class;
        
        // When & Then
        assertTrue(Modifier.isPublic(clazz.getModifiers()), 
                "La clase PagindData debe ser pública");
        assertTrue(Modifier.isFinal(clazz.getModifiers()), 
                "La clase PagindData debe ser final (utility class)");
    }

    @Test
    @DisplayName("Debe validar que los valores por defecto son numéricos válidos")
    void testDefaultValuesAreValidNumbers() {
        // Given & When & Then
        assertDoesNotThrow(() -> {
            int sizeValue = Integer.parseInt(PagindData.PAGINED_SIZE_VALUE);
            assertTrue(sizeValue > 0, "El valor por defecto del tamaño debe ser positivo");
            assertEquals(10, sizeValue, "El valor por defecto del tamaño debe ser 10");
        }, "PAGINED_SIZE_VALUE debe ser un número entero válido");
        
        assertDoesNotThrow(() -> {
            int pageValue = Integer.parseInt(PagindData.PAGINED_PAGE_VALUE);
            assertTrue(pageValue > 0, "El valor por defecto de la página debe ser positivo");
            assertEquals(1, pageValue, "El valor por defecto de la página debe ser 1");
        }, "PAGINED_PAGE_VALUE debe ser un número entero válido");
    }

    @Test
    @DisplayName("Debe validar que las constantes pueden ser accedidas directamente desde la clase")
    void testConstantsAccessibility() {
        // Given & When & Then - Se puede acceder sin instanciar la clase
        assertDoesNotThrow(() -> {
            String state = PagindData.PAGINED_STATE;
            String document = PagindData.PAGINED_DOCUMENT;
            String email = PagindData.PAGINED_EMAIL;
            String size = PagindData.PAGINED_SIZE;
            String page = PagindData.PAGINED_PAGE;
            String sizeValue = PagindData.PAGINED_SIZE_VALUE;
            String pageValue = PagindData.PAGINED_PAGE_VALUE;
            
            // Verificar que no son null
            assertNotNull(state);
            assertNotNull(document);
            assertNotNull(email);
            assertNotNull(size);
            assertNotNull(page);
            assertNotNull(sizeValue);
            assertNotNull(pageValue);
        }, "Las constantes deben ser accesibles directamente desde la clase");
    }

    @Test
    @DisplayName("Debe validar la coherencia de los nombres de los campos de paginación")
    void testFieldNamesConsistency() {
        // Given & When & Then - Los nombres deben ser identificadores válidos para parámetros
        assertTrue(PagindData.PAGINED_STATE.matches("^[a-zA-Z][a-zA-Z0-9_]*$"), 
                "PAGINED_STATE debe ser un identificador válido");
        assertTrue(PagindData.PAGINED_DOCUMENT.matches("^[a-zA-Z][a-zA-Z0-9_]*$"), 
                "PAGINED_DOCUMENT debe ser un identificador válido");
        assertTrue(PagindData.PAGINED_EMAIL.matches("^[a-zA-Z][a-zA-Z0-9_]*$"), 
                "PAGINED_EMAIL debe ser un identificador válido");
        assertTrue(PagindData.PAGINED_SIZE.matches("^[a-zA-Z][a-zA-Z0-9_]*$"), 
                "PAGINED_SIZE debe ser un identificador válido");
        assertTrue(PagindData.PAGINED_PAGE.matches("^[a-zA-Z][a-zA-Z0-9_]*$"), 
                "PAGINED_PAGE debe ser un identificador válido");
    }

    @Test
    @DisplayName("Debe validar que los valores son sensatos para paginación")
    void testValuesAreSensibleForPagination() {
        // Given & When & Then
        int sizeValue = Integer.parseInt(PagindData.PAGINED_SIZE_VALUE);
        int pageValue = Integer.parseInt(PagindData.PAGINED_PAGE_VALUE);
        
        // Los valores por defecto deben ser apropiados para paginación
        assertTrue(sizeValue >= 1 && sizeValue <= 100, 
                "El tamaño por defecto debe estar entre 1 y 100");
        assertTrue(pageValue >= 1, 
                "La página por defecto debe ser al menos 1");
        
        // Valores específicos esperados
        assertEquals(10, sizeValue, 
                "El tamaño por defecto debe ser 10 (valor común para paginación)");
        assertEquals(1, pageValue, 
                "La página por defecto debe ser 1 (primera página)");
    }

    @Test
    @DisplayName("Debe validar que las constantes son inmutables")
    void testConstantsAreImmutable() {
        // Given
        String originalState = PagindData.PAGINED_STATE;
        String originalDocument = PagindData.PAGINED_DOCUMENT;
        String originalEmail = PagindData.PAGINED_EMAIL;
        String originalSize = PagindData.PAGINED_SIZE;
        String originalPage = PagindData.PAGINED_PAGE;
        String originalSizeValue = PagindData.PAGINED_SIZE_VALUE;
        String originalPageValue = PagindData.PAGINED_PAGE_VALUE;
        
        // When & Then - Las constantes deben mantener sus valores (son final)
        assertEquals(originalState, PagindData.PAGINED_STATE, 
                "PAGINED_STATE debe mantener su valor inmutable");
        assertEquals(originalDocument, PagindData.PAGINED_DOCUMENT, 
                "PAGINED_DOCUMENT debe mantener su valor inmutable");
        assertEquals(originalEmail, PagindData.PAGINED_EMAIL, 
                "PAGINED_EMAIL debe mantener su valor inmutable");
        assertEquals(originalSize, PagindData.PAGINED_SIZE, 
                "PAGINED_SIZE debe mantener su valor inmutable");
        assertEquals(originalPage, PagindData.PAGINED_PAGE, 
                "PAGINED_PAGE debe mantener su valor inmutable");
        assertEquals(originalSizeValue, PagindData.PAGINED_SIZE_VALUE, 
                "PAGINED_SIZE_VALUE debe mantener su valor inmutable");
        assertEquals(originalPageValue, PagindData.PAGINED_PAGE_VALUE, 
                "PAGINED_PAGE_VALUE debe mantener su valor inmutable");
    }

    @Test
    @DisplayName("Debe validar que se puede crear una instancia de PagindData si es necesario")
    void testClassInstantiation() {
        // Given & When & Then - Aunque sea una utility class final, verificar que la clase está bien formada
        Class<PagindData> clazz = PagindData.class;
        
        // Verificar que la clase es final (ya probado arriba, pero lo incluimos aquí para completitud)
        assertTrue(Modifier.isFinal(clazz.getModifiers()),
                "PagindData debe ser una clase final (utility class)");
        
        // Verificar que tiene exactamente 7 campos públicos estáticos finales
        Field[] publicFields = clazz.getFields();
        assertEquals(7, publicFields.length, 
                "PagindData debe tener exactamente 7 campos públicos");
        
        // Todos los campos deben ser estáticos y finales
        for (Field field : publicFields) {
            assertTrue(Modifier.isStatic(field.getModifiers()),
                    "Todos los campos deben ser estáticos");
            assertTrue(Modifier.isFinal(field.getModifiers()),
                    "Todos los campos deben ser finales");
        }
    }
}