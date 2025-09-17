package com.crediya.loan.model.calculateborrowingcapacity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class InstallmentTest {

    @Test
    @DisplayName("NoArgs + setters/getters deben funcionar")
    void noArgsAndSettersGetters_work() {
        Installment installment = new Installment();
        installment.setN(1);
        installment.setCuota(new BigDecimal("500.00"));
        installment.setInteres(new BigDecimal("50.00"));
        installment.setAbonoCapital(new BigDecimal("450.00"));
        installment.setSaldo(new BigDecimal("9550.00"));

        assertEquals(1, installment.getN());
        assertEquals(new BigDecimal("500.00"), installment.getCuota());
        assertEquals(new BigDecimal("50.00"), installment.getInteres());
        assertEquals(new BigDecimal("450.00"), installment.getAbonoCapital());
        assertEquals(new BigDecimal("9550.00"), installment.getSaldo());
    }

    @Test
    @DisplayName("AllArgsConstructor debe setear todos los campos")
    void allArgsConstructor_setsAllFields() {
        Installment installment = new Installment(
                2,
                new BigDecimal("600.00"),
                new BigDecimal("75.25"),
                new BigDecimal("524.75"),
                new BigDecimal("8475.25")
        );

        assertEquals(2, installment.getN());
        assertEquals(new BigDecimal("600.00"), installment.getCuota());
        assertEquals(new BigDecimal("75.25"), installment.getInteres());
        assertEquals(new BigDecimal("524.75"), installment.getAbonoCapital());
        assertEquals(new BigDecimal("8475.25"), installment.getSaldo());
    }

    @Test
    @DisplayName("Builder debe construir correctamente")
    void builder_buildsCorrectly() {
        Installment installment = Installment.builder()
                .n(3)
                .cuota(new BigDecimal("400.00"))
                .interes(new BigDecimal("35.50"))
                .abonoCapital(new BigDecimal("364.50"))
                .saldo(new BigDecimal("7635.50"))
                .build();

        assertEquals(3, installment.getN());
        assertEquals(new BigDecimal("400.00"), installment.getCuota());
        assertEquals(new BigDecimal("35.50"), installment.getInteres());
        assertEquals(new BigDecimal("364.50"), installment.getAbonoCapital());
        assertEquals(new BigDecimal("7635.50"), installment.getSaldo());
    }

    @Test
    @DisplayName("Builder acepta nulos sin lanzar excepción")
    void builder_allowsNulls() {
        Installment installment = Installment.builder()
                .n(null)
                .cuota(null)
                .interes(null)
                .abonoCapital(null)
                .saldo(null)
                .build();

        assertNull(installment.getN());
        assertNull(installment.getCuota());
        assertNull(installment.getInteres());
        assertNull(installment.getAbonoCapital());
        assertNull(installment.getSaldo());
    }

    @Test
    @DisplayName("Debe manejar valores decimales complejos")
    void builder_handlesComplexDecimals() {
        BigDecimal complexCuota = new BigDecimal("456.789123");
        BigDecimal complexInteres = new BigDecimal("12.345678");
        BigDecimal complexAbono = new BigDecimal("444.443445");
        BigDecimal complexSaldo = new BigDecimal("12345.67890123");
        
        Installment installment = Installment.builder()
                .n(4)
                .cuota(complexCuota)
                .interes(complexInteres)
                .abonoCapital(complexAbono)
                .saldo(complexSaldo)
                .build();

        assertEquals(4, installment.getN());
        assertEquals(complexCuota, installment.getCuota());
        assertEquals(complexInteres, installment.getInteres());
        assertEquals(complexAbono, installment.getAbonoCapital());
        assertEquals(complexSaldo, installment.getSaldo());
    }

    @Test
    @DisplayName("@Data debe generar equals, hashCode y toString")
    void data_generatesEqualsHashCodeToString() {
        Installment installment1 = Installment.builder()
                .n(5)
                .cuota(new BigDecimal("300.00"))
                .interes(new BigDecimal("25.00"))
                .abonoCapital(new BigDecimal("275.00"))
                .saldo(new BigDecimal("5725.00"))
                .build();
                
        Installment installment2 = Installment.builder()
                .n(5)
                .cuota(new BigDecimal("300.00"))
                .interes(new BigDecimal("25.00"))
                .abonoCapital(new BigDecimal("275.00"))
                .saldo(new BigDecimal("5725.00"))
                .build();
                
        Installment installment3 = Installment.builder()
                .n(6)
                .cuota(new BigDecimal("400.00"))
                .interes(new BigDecimal("30.00"))
                .abonoCapital(new BigDecimal("370.00"))
                .saldo(new BigDecimal("5355.00"))
                .build();

        // Equals
        assertEquals(installment1, installment2);
        assertNotEquals(installment1, installment3);
        
        // HashCode
        assertEquals(installment1.hashCode(), installment2.hashCode());
        
        // ToString
        String toString = installment1.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("Installment"));
        assertTrue(toString.contains("n=5"));
        assertTrue(toString.contains("cuota=300.00"));
        assertTrue(toString.contains("interes=25.00"));
        assertTrue(toString.contains("abonoCapital=275.00"));
        assertTrue(toString.contains("saldo=5725.00"));
    }

    @Test
    @DisplayName("Debe manejar valores en cero correctamente")
    void builder_handlesZeroValues() {
        Installment installment = Installment.builder()
                .n(0)
                .cuota(BigDecimal.ZERO)
                .interes(BigDecimal.ZERO)
                .abonoCapital(BigDecimal.ZERO)
                .saldo(BigDecimal.ZERO)
                .build();

        assertEquals(0, installment.getN());
        assertEquals(BigDecimal.ZERO, installment.getCuota());
        assertEquals(BigDecimal.ZERO, installment.getInteres());
        assertEquals(BigDecimal.ZERO, installment.getAbonoCapital());
        assertEquals(BigDecimal.ZERO, installment.getSaldo());
    }

    @Test
    @DisplayName("Debe manejar números negativos (casos edge)")
    void builder_handlesNegativeValues() {
        Installment installment = Installment.builder()
                .n(-1) // N negativo (caso edge)
                .cuota(new BigDecimal("-100.00")) // Cuota negativa (caso edge)
                .interes(new BigDecimal("50.00"))
                .abonoCapital(new BigDecimal("-150.00")) // Abono negativo (caso edge)
                .saldo(new BigDecimal("10150.00"))
                .build();

        assertEquals(-1, installment.getN());
        assertEquals(new BigDecimal("-100.00"), installment.getCuota());
        assertEquals(new BigDecimal("50.00"), installment.getInteres());
        assertEquals(new BigDecimal("-150.00"), installment.getAbonoCapital());
        assertEquals(new BigDecimal("10150.00"), installment.getSaldo());
    }

    @Test
    @DisplayName("Debe manejar números altos correctamente")
    void builder_handlesLargeNumbers() {
        Installment installment = Installment.builder()
                .n(999)
                .cuota(new BigDecimal("999999.99"))
                .interes(new BigDecimal("123456.78"))
                .abonoCapital(new BigDecimal("876543.21"))
                .saldo(new BigDecimal("1000000000.00"))
                .build();

        assertEquals(999, installment.getN());
        assertEquals(new BigDecimal("999999.99"), installment.getCuota());
        assertEquals(new BigDecimal("123456.78"), installment.getInteres());
        assertEquals(new BigDecimal("876543.21"), installment.getAbonoCapital());
        assertEquals(new BigDecimal("1000000000.00"), installment.getSaldo());
    }

    @Test
    @DisplayName("Casos típicos de cuotas de amortización")
    void builder_handlesTypicalAmortizationScenarios() {
        // Primera cuota (más interés, menos abono a capital)
        Installment primeraCuota = Installment.builder()
                .n(1)
                .cuota(new BigDecimal("573.44"))
                .interes(new BigDecimal("83.33"))
                .abonoCapital(new BigDecimal("490.11"))
                .saldo(new BigDecimal("9509.89"))
                .build();

        // Última cuota (menos interés, más abono a capital)
        Installment ultimaCuota = Installment.builder()
                .n(18)
                .cuota(new BigDecimal("573.44"))
                .interes(new BigDecimal("4.78"))
                .abonoCapital(new BigDecimal("568.66"))
                .saldo(BigDecimal.ZERO)
                .build();

        assertEquals(1, primeraCuota.getN());
        assertEquals(18, ultimaCuota.getN());
        
        // Primera cuota debe tener más interés
        assertTrue(primeraCuota.getInteres().compareTo(ultimaCuota.getInteres()) > 0);
        
        // Última cuota debe tener más abono a capital
        assertTrue(ultimaCuota.getAbonoCapital().compareTo(primeraCuota.getAbonoCapital()) > 0);
        
        // Última cuota debe tener saldo cero
        assertEquals(BigDecimal.ZERO, ultimaCuota.getSaldo());
    }
}