package com.crediya.loan.model.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PendingApplicationsCriteriaTest {

    @Test
    @DisplayName("Constructor debe crear record correctamente")
    void constructor_createsRecordCorrectly() {
        PendingApplicationsCriteria criteria = new PendingApplicationsCriteria(
                "PENDING",
                "1234567890",
                "user@example.com",
                0,
                10
        );

        assertEquals("PENDING", criteria.state());
        assertEquals("1234567890", criteria.document());
        assertEquals("user@example.com", criteria.email());
        assertEquals(0, criteria.page());
        assertEquals(10, criteria.size());
    }

    @Test
    @DisplayName("Debe permitir valores nulos en campos opcionales")
    void constructor_allowsNullValues() {
        PendingApplicationsCriteria criteria = new PendingApplicationsCriteria(
                null, // state puede ser null para búsquedas sin filtro de estado
                null, // document puede ser null para búsquedas sin filtro de documento
                null, // email puede ser null para búsquedas sin filtro de email
                0,    // page no debe ser null
                10    // size no debe ser null
        );

        assertNull(criteria.state());
        assertNull(criteria.document());
        assertNull(criteria.email());
        assertEquals(0, criteria.page());
        assertEquals(10, criteria.size());
    }

    @Test
    @DisplayName("Debe manejar strings vacíos correctamente")
    void constructor_handlesEmptyStrings() {
        PendingApplicationsCriteria criteria = new PendingApplicationsCriteria(
                "",
                "",
                "",
                1,
                20
        );

        assertEquals("", criteria.state());
        assertEquals("", criteria.document());
        assertEquals("", criteria.email());
        assertEquals(1, criteria.page());
        assertEquals(20, criteria.size());
    }

    @Test
    @DisplayName("Record debe ser inmutable y tener equals/hashCode")
    void record_isImmutableAndHasEqualsHashCode() {
        PendingApplicationsCriteria criteria1 = new PendingApplicationsCriteria(
                "APPROVED",
                "DOC-123",
                "test@correo.com",
                2,
                15
        );
        
        PendingApplicationsCriteria criteria2 = new PendingApplicationsCriteria(
                "APPROVED",
                "DOC-123", 
                "test@correo.com",
                2,
                15
        );
        
        PendingApplicationsCriteria criteria3 = new PendingApplicationsCriteria(
                "REJECTED",
                "DOC-456",
                "different@correo.com",
                3,
                25
        );

        // Equals
        assertEquals(criteria1, criteria2);
        assertNotEquals(criteria1, criteria3);
        
        // HashCode
        assertEquals(criteria1.hashCode(), criteria2.hashCode());
        assertNotEquals(criteria1.hashCode(), criteria3.hashCode());
    }

    @Test
    @DisplayName("toString debe generar representación legible")
    void toString_generatesReadableRepresentation() {
        PendingApplicationsCriteria criteria = new PendingApplicationsCriteria(
                "IN_REVIEW",
                "CC-987654321",
                "client@domain.com",
                3,
                5
        );
        
        String toString = criteria.toString();
        
        assertNotNull(toString);
        assertTrue(toString.contains("PendingApplicationsCriteria"));
        assertTrue(toString.contains("state=IN_REVIEW"));
        assertTrue(toString.contains("document=CC-987654321"));
        assertTrue(toString.contains("email=client@domain.com"));
        assertTrue(toString.contains("page=3"));
        assertTrue(toString.contains("size=5"));
    }

    @Test
    @DisplayName("Debe manejar valores de paginación típicos")
    void constructor_handlesTypicalPaginationValues() {
        // Primera página
        PendingApplicationsCriteria firstPage = new PendingApplicationsCriteria(
                "PENDING", "DOC-001", "user1@test.com", 0, 10
        );
        
        // Segunda página
        PendingApplicationsCriteria secondPage = new PendingApplicationsCriteria(
                "PENDING", "DOC-001", "user1@test.com", 1, 10
        );
        
        // Tamaño diferente
        PendingApplicationsCriteria differentSize = new PendingApplicationsCriteria(
                "PENDING", "DOC-001", "user1@test.com", 0, 25
        );

        assertEquals(0, firstPage.page());
        assertEquals(1, secondPage.page());
        assertEquals(10, firstPage.size());
        assertEquals(25, differentSize.size());
        
        // Las páginas deben ser diferentes
        assertNotEquals(firstPage, secondPage);
        assertNotEquals(firstPage, differentSize);
    }

    @Test
    @DisplayName("Debe manejar valores edge de paginación")
    void constructor_handlesEdgePaginationValues() {
        // Página cero con tamaño 1
        PendingApplicationsCriteria minValues = new PendingApplicationsCriteria(
                "ANY", "ANY", "ANY", 0, 1
        );
        
        // Valores altos
        PendingApplicationsCriteria maxValues = new PendingApplicationsCriteria(
                "ANY", "ANY", "ANY", 999, 100
        );

        assertEquals(0, minValues.page());
        assertEquals(1, minValues.size());
        assertEquals(999, maxValues.page());
        assertEquals(100, maxValues.size());
    }

    @Test
    @DisplayName("Debe manejar diferentes tipos de documentos de identidad")
    void constructor_handlesDifferentDocumentTypes() {
        PendingApplicationsCriteria cedula = new PendingApplicationsCriteria(
                "PENDING", "12345678", "user@test.com", 0, 10
        );
        
        PendingApplicationsCriteria cedulaConFormato = new PendingApplicationsCriteria(
                "PENDING", "CC-12.345.678-9", "user@test.com", 0, 10
        );
        
        PendingApplicationsCriteria pasaporte = new PendingApplicationsCriteria(
                "PENDING", "P-AB123456", "user@test.com", 0, 10
        );

        assertEquals("12345678", cedula.document());
        assertEquals("CC-12.345.678-9", cedulaConFormato.document());
        assertEquals("P-AB123456", pasaporte.document());
    }

    @Test
    @DisplayName("Debe manejar diferentes estados de aplicación")
    void constructor_handlesDifferentApplicationStates() {
        String[] estados = {"PENDING", "IN_REVIEW", "APPROVED", "REJECTED", "CANCELLED", "EXPIRED"};
        
        for (String estado : estados) {
            PendingApplicationsCriteria criteria = new PendingApplicationsCriteria(
                    estado, "DOC-123", "user@test.com", 0, 10
            );
            
            assertEquals(estado, criteria.state());
        }
    }

    @Test
    @DisplayName("Debe manejar diferentes formatos de email")
    void constructor_handlesDifferentEmailFormats() {
        String[] emails = {
            "user@domain.com",
            "test.user+tag@example.co.uk", 
            "firstname.lastname@company.org",
            "user_name@domain-name.com",
            "123456@numbers.net"
        };
        
        for (int i = 0; i < emails.length; i++) {
            PendingApplicationsCriteria criteria = new PendingApplicationsCriteria(
                    "PENDING", "DOC-" + i, emails[i], i, 10
            );
            
            assertEquals(emails[i], criteria.email());
        }
    }
}