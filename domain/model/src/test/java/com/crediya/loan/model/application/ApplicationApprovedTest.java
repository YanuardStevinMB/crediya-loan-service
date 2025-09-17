package com.crediya.loan.model.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationApprovedTest {

    @Test
    @DisplayName("NoArgs + setters/getters deben funcionar")
    void noArgsAndSettersGetters_work() {
        ApplicationApproved app = new ApplicationApproved();
        app.setAmount(new BigDecimal("15000.00"));
        app.setInterestRate(new BigDecimal("12.50"));
        app.setTermMonths(24L);

        assertEquals(new BigDecimal("15000.00"), app.getAmount());
        assertEquals(new BigDecimal("12.50"), app.getInterestRate());
        assertEquals(24L, app.getTermMonths());
    }

    @Test
    @DisplayName("AllArgsConstructor debe setear todos los campos")
    void allArgsConstructor_setsAllFields() {
        ApplicationApproved app = new ApplicationApproved(
                new BigDecimal("25000.50"),
                new BigDecimal("9.75"),
                36L
        );

        assertEquals(new BigDecimal("25000.50"), app.getAmount());
        assertEquals(new BigDecimal("9.75"), app.getInterestRate());
        assertEquals(36L, app.getTermMonths());
    }

    @Test
    @DisplayName("Builder debe construir correctamente")
    void builder_buildsCorrectly() {
        ApplicationApproved app = ApplicationApproved.builder()
                .amount(new BigDecimal("1000.00"))
                .interestRate(new BigDecimal("11.00"))
                .termMonths(12L)
                .build();

        assertEquals(new BigDecimal("1000.00"), app.getAmount());
        assertEquals(new BigDecimal("11.00"), app.getInterestRate());
        assertEquals(12L, app.getTermMonths());
    }

    @Test
    @DisplayName("Builder acepta nulos sin lanzar excepción")
    void builder_allowsNulls() {
        ApplicationApproved app = ApplicationApproved.builder()
                .amount(null)
                .interestRate(null)
                .termMonths(null)
                .build();

        assertNull(app.getAmount());
        assertNull(app.getInterestRate());
        assertNull(app.getTermMonths());
    }

    @Test
    @DisplayName("Debe permitir valores decimales complejos en amount")
    void builder_handlesComplexDecimals() {
        BigDecimal complexAmount = new BigDecimal("12345.6789");
        BigDecimal complexRate = new BigDecimal("0.0125");
        
        ApplicationApproved app = ApplicationApproved.builder()
                .amount(complexAmount)
                .interestRate(complexRate)
                .termMonths(48L)
                .build();

        assertEquals(complexAmount, app.getAmount());
        assertEquals(complexRate, app.getInterestRate());
        assertEquals(48L, app.getTermMonths());
    }
}