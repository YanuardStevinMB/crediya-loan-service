package com.crediya.loan.model.loantype;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class LoanTypeTest {

    @Test
    @DisplayName("NoArgs + setters/getters deben funcionar")
    void noArgsAndSettersGetters_work() {
        LoanType lt = new LoanType();
        lt.setId(1L);
        lt.setName("Personal");
        lt.setAmountMin(new BigDecimal("1000.00"));
        lt.setAmountMax(new BigDecimal("50000.00"));
        lt.setInterestRate(new BigDecimal("12.50"));
        lt.setAutomaticValidation(Boolean.TRUE);

        assertEquals(1L, lt.getId());
        assertEquals("Personal", lt.getName());
        assertEquals(new BigDecimal("1000.00"), lt.getAmountMin());
        assertEquals(new BigDecimal("50000.00"), lt.getAmountMax());
        assertEquals(new BigDecimal("12.50"), lt.getInterestRate());
        assertTrue(lt.getAutomaticValidation());
    }

    @Test
    @DisplayName("AllArgsConstructor debe setear todos los campos")
    void allArgsConstructor_setsAllFields() {
        LoanType lt = new LoanType(
                2L,
                "Hipotecario",
                new BigDecimal("20000.00"),
                new BigDecimal("300000.00"),
                new BigDecimal("9.75"),
                Boolean.FALSE
        );

        assertEquals(2L, lt.getId());
        assertEquals("Hipotecario", lt.getName());
        assertEquals(new BigDecimal("20000.00"), lt.getAmountMin());
        assertEquals(new BigDecimal("300000.00"), lt.getAmountMax());
        assertEquals(new BigDecimal("9.75"), lt.getInterestRate());
        assertFalse(lt.getAutomaticValidation());
    }

    @Test
    @DisplayName("Builder debe construir correctamente")
    void builder_buildsCorrectly() {
        LoanType lt = LoanType.builder()
                .id(3L)
                .name("Vehicular")
                .amountMin(new BigDecimal("5000.00"))
                .amountMax(new BigDecimal("80000.00"))
                .interestRate(new BigDecimal("11.00"))
                .automaticValidation(Boolean.TRUE)
                .build();

        assertEquals(3L, lt.getId());
        assertEquals("Vehicular", lt.getName());
        assertEquals(new BigDecimal("5000.00"), lt.getAmountMin());
        assertEquals(new BigDecimal("80000.00"), lt.getAmountMax());
        assertEquals(new BigDecimal("11.00"), lt.getInterestRate());
        assertTrue(lt.getAutomaticValidation());
    }

    @Test
    @DisplayName("Builder acepta nulos sin lanzar excepción")
    void builder_allowsNulls() {
        LoanType lt = LoanType.builder()
                .id(null)
                .name(null)
                .amountMin(null)
                .amountMax(null)
                .interestRate(null)
                .automaticValidation(null)
                .build();

        assertNull(lt.getId());
        assertNull(lt.getName());
        assertNull(lt.getAmountMin());
        assertNull(lt.getAmountMax());
        assertNull(lt.getInterestRate());
        assertNull(lt.getAutomaticValidation());
    }
}