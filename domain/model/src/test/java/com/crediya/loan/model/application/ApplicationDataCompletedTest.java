package com.crediya.loan.model.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationDataCompletedTest {

    @Test
    @DisplayName("NoArgs + setters/getters deben funcionar")
    void noArgsAndSettersGetters_work() {
        ApplicationDataCompleted app = new ApplicationDataCompleted();
        app.setId(1L);
        app.setAmount(new BigDecimal("15000.00"));
        app.setEmail("user@example.com");
        app.setIdentityDocument("1234567890");
        app.setState("Pending");
        app.setLoan("Personal");
        app.setStateId(10L);
        app.setLoanTypeId(5L);

        assertEquals(1L, app.getId());
        assertEquals(new BigDecimal("15000.00"), app.getAmount());
        assertEquals("user@example.com", app.getEmail());
        assertEquals("1234567890", app.getIdentityDocument());
        assertEquals("Pending", app.getState());
        assertEquals("Personal", app.getLoan());
        assertEquals(10L, app.getStateId());
        assertEquals(5L, app.getLoanTypeId());
    }

    @Test
    @DisplayName("AllArgsConstructor debe setear todos los campos")
    void allArgsConstructor_setsAllFields() {
        ApplicationDataCompleted app = new ApplicationDataCompleted(
                2L,
                new BigDecimal("25000.50"),
                "client@domain.com",
                "CC-987654321",
                "Approved",
                "Mortgage",
                20L,
                7L
        );

        assertEquals(2L, app.getId());
        assertEquals(new BigDecimal("25000.50"), app.getAmount());
        assertEquals("client@domain.com", app.getEmail());
        assertEquals("CC-987654321", app.getIdentityDocument());
        assertEquals("Approved", app.getState());
        assertEquals("Mortgage", app.getLoan());
        assertEquals(20L, app.getStateId());
        assertEquals(7L, app.getLoanTypeId());
    }

    @Test
    @DisplayName("Builder debe construir correctamente")
    void builder_buildsCorrectly() {
        ApplicationDataCompleted app = ApplicationDataCompleted.builder()
                .id(3L)
                .amount(new BigDecimal("1000.00"))
                .email("test@correo.com")
                .identityDocument("DOC-123")
                .state("In Review")
                .loan("Vehicle")
                .stateId(1L)
                .loanTypeId(2L)
                .build();

        assertEquals(3L, app.getId());
        assertEquals(new BigDecimal("1000.00"), app.getAmount());
        assertEquals("test@correo.com", app.getEmail());
        assertEquals("DOC-123", app.getIdentityDocument());
        assertEquals("In Review", app.getState());
        assertEquals("Vehicle", app.getLoan());
        assertEquals(1L, app.getStateId());
        assertEquals(2L, app.getLoanTypeId());
    }

    @Test
    @DisplayName("Builder acepta nulos sin lanzar excepción")
    void builder_allowsNulls() {
        ApplicationDataCompleted app = ApplicationDataCompleted.builder()
                .id(null)
                .amount(null)
                .email(null)
                .identityDocument(null)
                .state(null)
                .loan(null)
                .stateId(null)
                .loanTypeId(null)
                .build();

        assertNull(app.getId());
        assertNull(app.getAmount());
        assertNull(app.getEmail());
        assertNull(app.getIdentityDocument());
        assertNull(app.getState());
        assertNull(app.getLoan());
        assertNull(app.getStateId());
        assertNull(app.getLoanTypeId());
    }

    @Test
    @DisplayName("Debe manejar strings vacíos correctamente")
    void builder_handlesEmptyStrings() {
        ApplicationDataCompleted app = ApplicationDataCompleted.builder()
                .id(4L)
                .amount(new BigDecimal("5000.00"))
                .email("")
                .identityDocument("")
                .state("")
                .loan("")
                .stateId(5L)
                .loanTypeId(3L)
                .build();

        assertEquals(4L, app.getId());
        assertEquals(new BigDecimal("5000.00"), app.getAmount());
        assertEquals("", app.getEmail());
        assertEquals("", app.getIdentityDocument());
        assertEquals("", app.getState());
        assertEquals("", app.getLoan());
        assertEquals(5L, app.getStateId());
        assertEquals(3L, app.getLoanTypeId());
    }
}