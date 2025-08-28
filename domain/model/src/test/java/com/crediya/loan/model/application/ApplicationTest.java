package com.crediya.loan.model.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ApplicationTest {

    @Test
    @DisplayName("NoArgs + setters/getters deben funcionar")
    void noArgsAndSettersGetters_work() {
        Application app = new Application();
        app.setId(1L);
        app.setAmount(new BigDecimal("15000.00"));
        app.setTerm(LocalDate.of(2026, 1, 15));
        app.setEmail("user@example.com");
        app.setIdentityDocument("1234567890");
        app.setStateId(10L);
        app.setLoanTypeId(5L);

        assertEquals(1L, app.getId());
        assertEquals(new BigDecimal("15000.00"), app.getAmount());
        assertEquals(LocalDate.of(2026, 1, 15), app.getTerm());
        assertEquals("user@example.com", app.getEmail());
        assertEquals("1234567890", app.getIdentityDocument());
        assertEquals(10L, app.getStateId());
        assertEquals(5L, app.getLoanTypeId());
    }

    @Test
    @DisplayName("AllArgsConstructor debe setear todos los campos")
    void allArgsConstructor_setsAllFields() {
        Application app = new Application(
                2L,
                new BigDecimal("25000.50"),
                LocalDate.of(2027, 6, 30),
                "client@domain.com",
                "CC-987654321",
                20L,
                7L
        );

        assertEquals(2L, app.getId());
        assertEquals(new BigDecimal("25000.50"), app.getAmount());
        assertEquals(LocalDate.of(2027, 6, 30), app.getTerm());
        assertEquals("client@domain.com", app.getEmail());
        assertEquals("CC-987654321", app.getIdentityDocument());
        assertEquals(20L, app.getStateId());
        assertEquals(7L, app.getLoanTypeId());
    }

    @Test
    @DisplayName("Builder debe construir correctamente")
    void builder_buildsCorrectly() {
        Application app = Application.builder()
                .id(3L)
                .amount(new BigDecimal("1000.00"))
                .term(LocalDate.of(2025, 12, 1))
                .email("test@correo.com")
                .identityDocument("DOC-123")
                .stateId(1L)
                .loanTypeId(2L)
                .build();

        assertEquals(3L, app.getId());
        assertEquals(new BigDecimal("1000.00"), app.getAmount());
        assertEquals(LocalDate.of(2025, 12, 1), app.getTerm());
        assertEquals("test@correo.com", app.getEmail());
        assertEquals("DOC-123", app.getIdentityDocument());
        assertEquals(1L, app.getStateId());
        assertEquals(2L, app.getLoanTypeId());
    }

    @Test
    @DisplayName("Builder acepta nulos sin lanzar excepción")
    void builder_allowsNulls() {
        Application app = Application.builder()
                .id(null)
                .amount(null)
                .term(null)
                .email(null)
                .identityDocument(null)
                .stateId(null)
                .loanTypeId(null)
                .build();

        assertNull(app.getId());
        assertNull(app.getAmount());
        assertNull(app.getTerm());
        assertNull(app.getEmail());
        assertNull(app.getIdentityDocument());
        assertNull(app.getStateId());
        assertNull(app.getLoanTypeId());
    }
}