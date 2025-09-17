package com.crediya.loan.model.calculateborrowingcapacity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AnswersApplicationSqsTest {

    @Test
    @DisplayName("NoArgs + setters/getters deben funcionar")
    void noArgsAndSettersGetters_work() {
        AnswersApplicationSqs answer = new AnswersApplicationSqs();
        List<Installment> installments = Arrays.asList(
            Installment.builder().n(1).cuota(new BigDecimal("500.00")).build()
        );
        Totals totals = Totals.builder()
            .totalIntereses(new BigDecimal("1200.00"))
            .totalPagado(new BigDecimal("12200.00"))
            .build();
        Instant decidedAt = Instant.now();
        
        answer.setId(1L);
        answer.setIdentityDocument("1234567890");
        answer.setStatus("APPROVED");
        answer.setStatusCode("A001");
        answer.setRequestId("REQ-123");
        answer.setClientEmail("client@example.com");
        answer.setCuotaNueva(new BigDecimal("500.00"));
        answer.setCapacidadDisponible(new BigDecimal("15000.00"));
        answer.setInteresMensual(new BigDecimal("12.50"));
        answer.setPlazoMeses(24);
        answer.setPaymentPlan(installments);
        answer.setTotales(totals);
        answer.setDecidedAt(decidedAt);

        assertEquals(1L, answer.getId());
        assertEquals("1234567890", answer.getIdentityDocument());
        assertEquals("APPROVED", answer.getStatus());
        assertEquals("A001", answer.getStatusCode());
        assertEquals("REQ-123", answer.getRequestId());
        assertEquals("client@example.com", answer.getClientEmail());
        assertEquals(new BigDecimal("500.00"), answer.getCuotaNueva());
        assertEquals(new BigDecimal("15000.00"), answer.getCapacidadDisponible());
        assertEquals(new BigDecimal("12.50"), answer.getInteresMensual());
        assertEquals(24, answer.getPlazoMeses());
        assertEquals(installments, answer.getPaymentPlan());
        assertEquals(totals, answer.getTotales());
        assertEquals(decidedAt, answer.getDecidedAt());
    }

    @Test
    @DisplayName("AllArgsConstructor debe setear todos los campos")
    void allArgsConstructor_setsAllFields() {
        List<Installment> installments = Arrays.asList(
            Installment.builder().n(1).cuota(new BigDecimal("600.00")).build(),
            Installment.builder().n(2).cuota(new BigDecimal("600.00")).build()
        );
        Totals totals = Totals.builder()
            .totalIntereses(new BigDecimal("2400.00"))
            .totalPagado(new BigDecimal("14400.00"))
            .build();
        Instant decidedAt = Instant.parse("2024-01-15T10:30:00Z");
        
        AnswersApplicationSqs answer = new AnswersApplicationSqs(
                2L,
                "CC-987654321",
                "REJECTED",
                "R001",
                "REQ-456",
                "user@domain.com",
                new BigDecimal("600.00"),
                new BigDecimal("8000.00"),
                new BigDecimal("15.75"),
                36,
                installments,
                totals,
                decidedAt
        );

        assertEquals(2L, answer.getId());
        assertEquals("CC-987654321", answer.getIdentityDocument());
        assertEquals("REJECTED", answer.getStatus());
        assertEquals("R001", answer.getStatusCode());
        assertEquals("REQ-456", answer.getRequestId());
        assertEquals("user@domain.com", answer.getClientEmail());
        assertEquals(new BigDecimal("600.00"), answer.getCuotaNueva());
        assertEquals(new BigDecimal("8000.00"), answer.getCapacidadDisponible());
        assertEquals(new BigDecimal("15.75"), answer.getInteresMensual());
        assertEquals(36, answer.getPlazoMeses());
        assertEquals(installments, answer.getPaymentPlan());
        assertEquals(totals, answer.getTotales());
        assertEquals(decidedAt, answer.getDecidedAt());
    }

    @Test
    @DisplayName("Builder debe construir correctamente")
    void builder_buildsCorrectly() {
        Installment installment = Installment.builder()
                .n(1)
                .cuota(new BigDecimal("400.00"))
                .interes(new BigDecimal("50.00"))
                .abonoCapital(new BigDecimal("350.00"))
                .saldo(new BigDecimal("9650.00"))
                .build();
        List<Installment> paymentPlan = Arrays.asList(installment);
        
        Totals totals = Totals.builder()
                .totalIntereses(new BigDecimal("800.00"))
                .totalPagado(new BigDecimal("10800.00"))
                .build();
                
        Instant decidedAt = Instant.parse("2024-03-20T14:45:30Z");
        
        AnswersApplicationSqs answer = AnswersApplicationSqs.builder()
                .id(3L)
                .identityDocument("DOC-123456")
                .status("PENDING")
                .statusCode("P001")
                .requestId("REQ-789")
                .clientEmail("test@correo.com")
                .cuotaNueva(new BigDecimal("400.00"))
                .capacidadDisponible(new BigDecimal("10000.00"))
                .interesMensual(new BigDecimal("10.25"))
                .plazoMeses(12)
                .paymentPlan(paymentPlan)
                .totales(totals)
                .decidedAt(decidedAt)
                .build();

        assertEquals(3L, answer.getId());
        assertEquals("DOC-123456", answer.getIdentityDocument());
        assertEquals("PENDING", answer.getStatus());
        assertEquals("P001", answer.getStatusCode());
        assertEquals("REQ-789", answer.getRequestId());
        assertEquals("test@correo.com", answer.getClientEmail());
        assertEquals(new BigDecimal("400.00"), answer.getCuotaNueva());
        assertEquals(new BigDecimal("10000.00"), answer.getCapacidadDisponible());
        assertEquals(new BigDecimal("10.25"), answer.getInteresMensual());
        assertEquals(12, answer.getPlazoMeses());
        assertEquals(paymentPlan, answer.getPaymentPlan());
        assertEquals(totals, answer.getTotales());
        assertEquals(decidedAt, answer.getDecidedAt());
    }

    @Test
    @DisplayName("Builder acepta nulos sin lanzar excepción")
    void builder_allowsNulls() {
        AnswersApplicationSqs answer = AnswersApplicationSqs.builder()
                .id(null)
                .identityDocument(null)
                .status(null)
                .statusCode(null)
                .requestId(null)
                .clientEmail(null)
                .cuotaNueva(null)
                .capacidadDisponible(null)
                .interesMensual(null)
                .plazoMeses(null)
                .paymentPlan(null)
                .totales(null)
                .decidedAt(null)
                .build();

        assertNull(answer.getId());
        assertNull(answer.getIdentityDocument());
        assertNull(answer.getStatus());
        assertNull(answer.getStatusCode());
        assertNull(answer.getRequestId());
        assertNull(answer.getClientEmail());
        assertNull(answer.getCuotaNueva());
        assertNull(answer.getCapacidadDisponible());
        assertNull(answer.getInteresMensual());
        assertNull(answer.getPlazoMeses());
        assertNull(answer.getPaymentPlan());
        assertNull(answer.getTotales());
        assertNull(answer.getDecidedAt());
    }

    @Test
    @DisplayName("Debe manejar lista vacía de installments")
    void builder_handlesEmptyPaymentPlan() {
        AnswersApplicationSqs answer = AnswersApplicationSqs.builder()
                .id(4L)
                .identityDocument("EMPTY-123")
                .paymentPlan(Collections.emptyList())
                .build();

        assertEquals(4L, answer.getId());
        assertEquals("EMPTY-123", answer.getIdentityDocument());
        assertNotNull(answer.getPaymentPlan());
        assertTrue(answer.getPaymentPlan().isEmpty());
    }

    @Test
    @DisplayName("@Data debe generar equals, hashCode y toString")
    void data_generatesEqualsHashCodeToString() {
        Instant now = Instant.now();
        
        AnswersApplicationSqs answer1 = AnswersApplicationSqs.builder()
                .id(5L)
                .identityDocument("EQUAL-123")
                .status("APPROVED")
                .decidedAt(now)
                .build();
                
        AnswersApplicationSqs answer2 = AnswersApplicationSqs.builder()
                .id(5L)
                .identityDocument("EQUAL-123")
                .status("APPROVED")
                .decidedAt(now)
                .build();
                
        AnswersApplicationSqs answer3 = AnswersApplicationSqs.builder()
                .id(6L)
                .identityDocument("DIFFERENT-123")
                .status("REJECTED")
                .decidedAt(now)
                .build();

        // Equals
        assertEquals(answer1, answer2);
        assertNotEquals(answer1, answer3);
        
        // HashCode
        assertEquals(answer1.hashCode(), answer2.hashCode());
        
        // ToString
        String toString = answer1.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("AnswersApplicationSqs"));
        assertTrue(toString.contains("id=5"));
        assertTrue(toString.contains("identityDocument=EQUAL-123"));
        assertTrue(toString.contains("status=APPROVED"));
    }

    @Test
    @DisplayName("Debe manejar múltiples installments correctamente")
    void builder_handlesMultipleInstallments() {
        List<Installment> installments = Arrays.asList(
            Installment.builder().n(1).cuota(new BigDecimal("300.00")).build(),
            Installment.builder().n(2).cuota(new BigDecimal("300.00")).build(),
            Installment.builder().n(3).cuota(new BigDecimal("300.00")).build()
        );
        
        AnswersApplicationSqs answer = AnswersApplicationSqs.builder()
                .id(6L)
                .paymentPlan(installments)
                .plazoMeses(3)
                .build();

        assertEquals(3, answer.getPaymentPlan().size());
        assertEquals(3, answer.getPlazoMeses());
        assertEquals(new BigDecimal("300.00"), answer.getPaymentPlan().get(0).getCuota());
        assertEquals(new BigDecimal("300.00"), answer.getPaymentPlan().get(1).getCuota());
        assertEquals(new BigDecimal("300.00"), answer.getPaymentPlan().get(2).getCuota());
    }

    @Test
    @DisplayName("Debe manejar valores decimales complejos")
    void builder_handlesComplexDecimals() {
        AnswersApplicationSqs answer = AnswersApplicationSqs.builder()
                .id(7L)
                .cuotaNueva(new BigDecimal("456.789"))
                .capacidadDisponible(new BigDecimal("123456.123456"))
                .interesMensual(new BigDecimal("0.001234"))
                .build();

        assertEquals(new BigDecimal("456.789"), answer.getCuotaNueva());
        assertEquals(new BigDecimal("123456.123456"), answer.getCapacidadDisponible());
        assertEquals(new BigDecimal("0.001234"), answer.getInteresMensual());
    }
}