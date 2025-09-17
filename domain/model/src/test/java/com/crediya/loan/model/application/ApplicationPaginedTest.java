package com.crediya.loan.model.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationPaginedTest {

    @Test
    @DisplayName("NoArgs + setters/getters deben funcionar")
    void noArgsAndSettersGetters_work() {
        ApplicationPagined app = new ApplicationPagined();
        app.setId(1L);
        app.setAmount(new BigDecimal("15000.00"));
        app.setTerm(LocalDate.of(2026, 1, 15));
        app.setEmail("user@example.com");
        app.setIdentityDocument("1234567890");
        app.setState("Pending");
        app.setLoan("Personal");
        app.setStateId(10L);
        app.setLoanTypeId(5L);
        app.setFullName("John Doe");
        app.setBaseSalary(new BigDecimal("3500.00"));

        assertEquals(1L, app.getId());
        assertEquals(new BigDecimal("15000.00"), app.getAmount());
        assertEquals(LocalDate.of(2026, 1, 15), app.getTerm());
        assertEquals("user@example.com", app.getEmail());
        assertEquals("1234567890", app.getIdentityDocument());
        assertEquals("Pending", app.getState());
        assertEquals("Personal", app.getLoan());
        assertEquals(10L, app.getStateId());
        assertEquals(5L, app.getLoanTypeId());
        assertEquals("John Doe", app.getFullName());
        assertEquals(new BigDecimal("3500.00"), app.getBaseSalary());
    }

    @Test
    @DisplayName("AllArgsConstructor debe setear todos los campos")
    void allArgsConstructor_setsAllFields() {
        ApplicationPagined app = new ApplicationPagined(
                2L,
                new BigDecimal("25000.50"),
                LocalDate.of(2027, 6, 30),
                "client@domain.com",
                "CC-987654321",
                "Approved",
                "Mortgage",
                20L,
                7L,
                "Jane Smith",
                new BigDecimal("4200.75")
        );

        assertEquals(2L, app.getId());
        assertEquals(new BigDecimal("25000.50"), app.getAmount());
        assertEquals(LocalDate.of(2027, 6, 30), app.getTerm());
        assertEquals("client@domain.com", app.getEmail());
        assertEquals("CC-987654321", app.getIdentityDocument());
        assertEquals("Approved", app.getState());
        assertEquals("Mortgage", app.getLoan());
        assertEquals(20L, app.getStateId());
        assertEquals(7L, app.getLoanTypeId());
        assertEquals("Jane Smith", app.getFullName());
        assertEquals(new BigDecimal("4200.75"), app.getBaseSalary());
    }

    @Test
    @DisplayName("Builder debe construir correctamente")
    void builder_buildsCorrectly() {
        ApplicationPagined app = ApplicationPagined.builder()
                .id(3L)
                .amount(new BigDecimal("1000.00"))
                .term(LocalDate.of(2025, 12, 1))
                .email("test@correo.com")
                .identityDocument("DOC-123")
                .state("In Review")
                .loan("Vehicle")
                .stateId(1L)
                .loanTypeId(2L)
                .fullName("Bob Johnson")
                .baseSalary(new BigDecimal("2800.00"))
                .build();

        assertEquals(3L, app.getId());
        assertEquals(new BigDecimal("1000.00"), app.getAmount());
        assertEquals(LocalDate.of(2025, 12, 1), app.getTerm());
        assertEquals("test@correo.com", app.getEmail());
        assertEquals("DOC-123", app.getIdentityDocument());
        assertEquals("In Review", app.getState());
        assertEquals("Vehicle", app.getLoan());
        assertEquals(1L, app.getStateId());
        assertEquals(2L, app.getLoanTypeId());
        assertEquals("Bob Johnson", app.getFullName());
        assertEquals(new BigDecimal("2800.00"), app.getBaseSalary());
    }

    @Test
    @DisplayName("Builder acepta nulos sin lanzar excepción")
    void builder_allowsNulls() {
        ApplicationPagined app = ApplicationPagined.builder()
                .id(null)
                .amount(null)
                .term(null)
                .email(null)
                .identityDocument(null)
                .state(null)
                .loan(null)
                .stateId(null)
                .loanTypeId(null)
                .fullName(null)
                .baseSalary(null)
                .build();

        assertNull(app.getId());
        assertNull(app.getAmount());
        assertNull(app.getTerm());
        assertNull(app.getEmail());
        assertNull(app.getIdentityDocument());
        assertNull(app.getState());
        assertNull(app.getLoan());
        assertNull(app.getStateId());
        assertNull(app.getLoanTypeId());
        assertNull(app.getFullName());
        assertNull(app.getBaseSalary());
    }

    @Test
    @DisplayName("toString debe generar representación correcta")
    void toString_generatesCorrectRepresentation() {
        ApplicationPagined app = ApplicationPagined.builder()
                .id(4L)
                .amount(new BigDecimal("5000.00"))
                .term(LocalDate.of(2024, 3, 15))
                .email("test@email.com")
                .identityDocument("ID-456")
                .state("Processing")
                .loan("Personal")
                .stateId(3L)
                .loanTypeId(1L)
                .fullName("Alice Wonder")
                .baseSalary(new BigDecimal("3000.00"))
                .build();

        String toString = app.toString();
        
        // Verificar que contiene información clave
        assertTrue(toString.contains("ApplicationPagined"));
        assertTrue(toString.contains("id=4"));
        assertTrue(toString.contains("amount=5000.00"));
        assertTrue(toString.contains("email=test@email.com"));
        assertTrue(toString.contains("fullName=Alice Wonder"));
        assertTrue(toString.contains("baseSalary=3000.00"));
    }

    @Test
    @DisplayName("toString debe manejar valores nulos correctamente")
    void toString_handlesNullsCorrectly() {
        ApplicationPagined app = new ApplicationPagined();
        String toString = app.toString();
        
        assertNotNull(toString);
        assertTrue(toString.contains("ApplicationPagined"));
        // No debe lanzar excepción aunque tenga campos nulos
    }

    @Test
    @DisplayName("Debe manejar fechas límite correctamente")
    void builder_handlesEdgeDates() {
        LocalDate pastDate = LocalDate.of(2020, 1, 1);
        LocalDate futureDate = LocalDate.of(2030, 12, 31);
        
        ApplicationPagined pastApp = ApplicationPagined.builder()
                .id(5L)
                .term(pastDate)
                .build();
                
        ApplicationPagined futureApp = ApplicationPagined.builder()
                .id(6L)
                .term(futureDate)
                .build();

        assertEquals(pastDate, pastApp.getTerm());
        assertEquals(futureDate, futureApp.getTerm());
    }
}