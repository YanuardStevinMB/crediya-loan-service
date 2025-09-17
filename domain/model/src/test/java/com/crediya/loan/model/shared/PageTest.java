package com.crediya.loan.model.shared;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PageTest {

    @Test
    @DisplayName("Constructor debe crear Page correctamente")
    void constructor_createsPageCorrectly() {
        List<String> content = Arrays.asList("item1", "item2", "item3");
        Page<String> page = new Page<>(content, 0, 10, 3L, 1);

        assertEquals(content, page.content());
        assertEquals(0, page.page());
        assertEquals(10, page.size());
        assertEquals(3L, page.totalElements());
        assertEquals(1, page.totalPages());
    }

    @Test
    @DisplayName("Método estático of() debe calcular totalPages correctamente")
    void of_calculatesTotalPagesCorrectly() {
        List<String> content = Arrays.asList("item1", "item2", "item3");
        
        // Caso 1: exactamente divisible
        Page<String> page1 = Page.of(content, 0, 3, 3L);
        assertEquals(1, page1.totalPages());
        
        // Caso 2: con residuo
        Page<String> page2 = Page.of(content, 0, 2, 3L);
        assertEquals(2, page2.totalPages());
        
        // Caso 3: más elementos de los que hay en el contenido actual
        Page<String> page3 = Page.of(content, 0, 10, 25L);
        assertEquals(3, page3.totalPages());
    }

    @Test
    @DisplayName("Método of() con lista vacía debe funcionar")
    void of_worksWithEmptyList() {
        List<String> emptyContent = Collections.emptyList();
        Page<String> page = Page.of(emptyContent, 0, 10, 0L);

        assertEquals(emptyContent, page.content());
        assertEquals(0, page.page());
        assertEquals(10, page.size());
        assertEquals(0L, page.totalElements());
        assertEquals(0, page.totalPages());
    }

    @Test
    @DisplayName("Método of() debe manejar totalElements 0 correctamente")
    void of_handlesZeroTotalElements() {
        List<String> content = Collections.emptyList();
        Page<String> page = Page.of(content, 0, 5, 0L);

        assertEquals(0, page.totalPages());
        assertEquals(0L, page.totalElements());
    }

    @Test
    @DisplayName("Método of() debe manejar size 1 correctamente")
    void of_handlesSizeOne() {
        List<String> content = Arrays.asList("single-item");
        Page<String> page = Page.of(content, 2, 1, 5L);

        assertEquals(1, page.content().size());
        assertEquals(2, page.page());
        assertEquals(1, page.size());
        assertEquals(5L, page.totalElements());
        assertEquals(5, page.totalPages()); // Math.ceil(5.0 / 1) = 5
    }

    @Test
    @DisplayName("Record debe ser inmutable y tener equals/hashCode")
    void record_isImmutableAndHasEqualsHashCode() {
        List<String> content1 = Arrays.asList("a", "b");
        List<String> content2 = Arrays.asList("a", "b");
        
        Page<String> page1 = new Page<>(content1, 1, 5, 10L, 2);
        Page<String> page2 = new Page<>(content2, 1, 5, 10L, 2);
        Page<String> page3 = new Page<>(content1, 2, 5, 10L, 2); // diferente página

        // Equals
        assertEquals(page1, page2);
        assertNotEquals(page1, page3);
        
        // HashCode
        assertEquals(page1.hashCode(), page2.hashCode());
    }

    @Test
    @DisplayName("Debe manejar diferentes tipos de contenido")
    void shouldHandleDifferentContentTypes() {
        // Con integers
        List<Integer> intContent = Arrays.asList(1, 2, 3, 4, 5);
        Page<Integer> intPage = Page.of(intContent, 0, 5, 5L);
        assertEquals(5, intPage.content().size());
        assertEquals(1, intPage.totalPages());
        
        // Con objetos custom (usando String como ejemplo)
        List<String> stringContent = Arrays.asList("test1", "test2");
        Page<String> stringPage = Page.of(stringContent, 1, 2, 4L);
        assertEquals(2, stringPage.content().size());
        assertEquals(2, stringPage.totalPages());
    }

    @Test
    @DisplayName("Cálculo de totalPages debe usar Math.ceil correctamente")
    void totalPages_usesMathCeilCorrectly() {
        // Casos edge para verificar el cálculo de Math.ceil
        
        // 1 elemento, size 10 -> ceil(1.0/10) = ceil(0.1) = 1
        Page<String> page1 = Page.of(Arrays.asList("item"), 0, 10, 1L);
        assertEquals(1, page1.totalPages());
        
        // 10 elementos, size 10 -> ceil(10.0/10) = ceil(1.0) = 1  
        Page<String> page2 = Page.of(Arrays.asList("item"), 0, 10, 10L);
        assertEquals(1, page2.totalPages());
        
        // 11 elementos, size 10 -> ceil(11.0/10) = ceil(1.1) = 2
        Page<String> page3 = Page.of(Arrays.asList("item"), 0, 10, 11L);
        assertEquals(2, page3.totalPages());
        
        // 100 elementos, size 7 -> ceil(100.0/7) = ceil(14.28...) = 15
        Page<String> page4 = Page.of(Arrays.asList("item"), 0, 7, 100L);
        assertEquals(15, page4.totalPages());
    }

    @Test
    @DisplayName("toString debe generar representación legible")
    void toString_generatesReadableRepresentation() {
        List<String> content = Arrays.asList("item1", "item2");
        Page<String> page = new Page<>(content, 1, 5, 12L, 3);
        
        String toString = page.toString();
        
        assertNotNull(toString);
        assertTrue(toString.contains("Page"));
        assertTrue(toString.contains("content="));
        assertTrue(toString.contains("page=1"));
        assertTrue(toString.contains("size=5"));
        assertTrue(toString.contains("totalElements=12"));
        assertTrue(toString.contains("totalPages=3"));
    }
}