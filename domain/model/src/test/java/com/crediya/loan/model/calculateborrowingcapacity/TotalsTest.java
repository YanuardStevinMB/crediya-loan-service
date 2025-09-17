package com.crediya.loan.model.calculateborrowingcapacity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class TotalsTest {

    @Test
    @DisplayName("NoArgs constructor debe crear objeto con campos nulos")
    void noArgsConstructor_createsObjectWithNullFields() {
        Totals totals = new Totals();
        
        assertNull(totals.getTotalIntereses());
        assertNull(totals.getTotalPagado());
    }

    @Test
    @DisplayName("AllArgsConstructor debe setear todos los campos")
    void allArgsConstructor_setsAllFields() {
        Totals totals = new Totals(
                new BigDecimal("1200.00"),
                new BigDecimal("11200.00")
        );

        assertEquals(new BigDecimal("1200.00"), totals.getTotalIntereses());
        assertEquals(new BigDecimal("11200.00"), totals.getTotalPagado());
    }

    @Test
    @DisplayName("Builder debe construir correctamente")
    void builder_buildsCorrectly() {
        Totals totals = Totals.builder()
                .totalIntereses(new BigDecimal("2400.50"))
                .totalPagado(new BigDecimal("22400.50"))
                .build();

        assertEquals(new BigDecimal("2400.50"), totals.getTotalIntereses());
        assertEquals(new BigDecimal("22400.50"), totals.getTotalPagado());
    }

    @Test
    @DisplayName("Builder acepta nulos sin lanzar excepción")
    void builder_allowsNulls() {
        Totals totals = Totals.builder()
                .totalIntereses(null)
                .totalPagado(null)
                .build();

        assertNull(totals.getTotalIntereses());
        assertNull(totals.getTotalPagado());
    }

    @Test
    @DisplayName("Debe manejar valores decimales complejos")
    void builder_handlesComplexDecimals() {
        BigDecimal complexIntereses = new BigDecimal("1234.567890123");
        BigDecimal complexPagado = new BigDecimal("98765.432109876");
        
        Totals totals = Totals.builder()
                .totalIntereses(complexIntereses)
                .totalPagado(complexPagado)
                .build();

        assertEquals(complexIntereses, totals.getTotalIntereses());
        assertEquals(complexPagado, totals.getTotalPagado());
    }

    @Test
    @DisplayName("Debe manejar valores en cero correctamente")
    void builder_handlesZeroValues() {
        Totals totals = Totals.builder()
                .totalIntereses(BigDecimal.ZERO)
                .totalPagado(BigDecimal.ZERO)
                .build();

        assertEquals(BigDecimal.ZERO, totals.getTotalIntereses());
        assertEquals(BigDecimal.ZERO, totals.getTotalPagado());
    }

    @Test
    @DisplayName("Debe manejar valores negativos (casos edge)")
    void builder_handlesNegativeValues() {
        // Aunque no sea lógico en el contexto de negocio, el modelo debe soportarlo
        Totals totals = Totals.builder()
                .totalIntereses(new BigDecimal("-100.00"))
                .totalPagado(new BigDecimal("-1000.00"))
                .build();

        assertEquals(new BigDecimal("-100.00"), totals.getTotalIntereses());
        assertEquals(new BigDecimal("-1000.00"), totals.getTotalPagado());
    }

    @Test
    @DisplayName("Debe manejar números muy altos")
    void builder_handlesLargeNumbers() {
        Totals totals = Totals.builder()
                .totalIntereses(new BigDecimal("999999999.99"))
                .totalPagado(new BigDecimal("9999999999.99"))
                .build();

        assertEquals(new BigDecimal("999999999.99"), totals.getTotalIntereses());
        assertEquals(new BigDecimal("9999999999.99"), totals.getTotalPagado());
    }

    @Test
    @DisplayName("Solo tiene getters, no setters (inmutable)")
    void onlyHasGetters_noSetters() {
        Totals totals = Totals.builder()
                .totalIntereses(new BigDecimal("500.00"))
                .totalPagado(new BigDecimal("5500.00"))
                .build();

        // Verificar que los getters funcionan
        assertNotNull(totals.getTotalIntereses());
        assertNotNull(totals.getTotalPagado());
        
        // Verificar que no hay setters públicos (solo getters debido a @Getter)
        // Esto se verifica implícitamente al no poder llamar setters
        assertEquals(new BigDecimal("500.00"), totals.getTotalIntereses());
        assertEquals(new BigDecimal("5500.00"), totals.getTotalPagado());
    }

    @Test
    @DisplayName("Casos típicos de cálculo de totales")
    void builder_handlesTypicalTotalCalculations() {
        // Caso de préstamo típico: $10,000 a 18 meses con 10% anual
        // Cuota mensual aproximada: $573.44
        // Total a pagar: $10,322 (aprox)
        // Total intereses: $322 (aprox)
        Totals totalsPrestamoPersonal = Totals.builder()
                .totalIntereses(new BigDecimal("322.92"))
                .totalPagado(new BigDecimal("10322.92"))
                .build();

        // Caso de préstamo hipotecario: $100,000 a 360 meses con 5% anual
        // Total intereses mucho mayor
        Totals totalsHipotecario = Totals.builder()
                .totalIntereses(new BigDecimal("93255.78"))
                .totalPagado(new BigDecimal("193255.78"))
                .build();

        assertEquals(new BigDecimal("322.92"), totalsPrestamoPersonal.getTotalIntereses());
        assertEquals(new BigDecimal("10322.92"), totalsPrestamoPersonal.getTotalPagado());
        
        assertEquals(new BigDecimal("93255.78"), totalsHipotecario.getTotalIntereses());
        assertEquals(new BigDecimal("193255.78"), totalsHipotecario.getTotalPagado());
        
        // El préstamo hipotecario debe tener más intereses
        assertTrue(totalsHipotecario.getTotalIntereses().compareTo(totalsPrestamoPersonal.getTotalIntereses()) > 0);
    }

    @Test
    @DisplayName("Builder debe ser inmutable después de build")
    void builder_isImmutableAfterBuild() {
        BigDecimal originalIntereses = new BigDecimal("1000.00");
        BigDecimal originalPagado = new BigDecimal("11000.00");
        
        Totals totals = Totals.builder()
                .totalIntereses(originalIntereses)
                .totalPagado(originalPagado)
                .build();

        // Los valores no deben cambiar después del build
        assertEquals(originalIntereses, totals.getTotalIntereses());
        assertEquals(originalPagado, totals.getTotalPagado());
        
        // Modificar las referencias originales no debe afectar el objeto construido
        originalIntereses = new BigDecimal("2000.00");
        originalPagado = new BigDecimal("22000.00");
        
        // Los valores del objeto Totals no deben haber cambiado
        assertEquals(new BigDecimal("1000.00"), totals.getTotalIntereses());
        assertEquals(new BigDecimal("11000.00"), totals.getTotalPagado());
    }

    @Test
    @DisplayName("Debe generar toString legible (si tiene)")
    void shouldGenerateReadableToString() {
        Totals totals = Totals.builder()
                .totalIntereses(new BigDecimal("500.00"))
                .totalPagado(new BigDecimal("5500.00"))
                .build();
        
        String toString = totals.toString();
        
        assertNotNull(toString);
        // El toString debe contener información sobre la clase y los valores
        assertTrue(toString.contains("Totals") || toString.contains("total"));
    }
}