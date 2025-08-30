package com.crediya.loan.api;


import com.crediya.loan.api.dto.ApiResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ApiResponseTest {

    @Test
    @DisplayName("ok(): construye respuesta exitosa con data y timestamp reciente")
    void ok_shouldBuildSuccessResponseWithData() {
        Instant start = Instant.now();

        ApiResponse<String> r = ApiResponse.ok("PAYLOAD", "Hecho", "/api/x");

        Instant end = Instant.now();

        assertAll(
                () -> assertTrue(r.isSuccess()),
                () -> assertEquals("Hecho", r.getMessage()),
                () -> assertEquals("PAYLOAD", r.getData()),
                () -> assertNull(r.getErrors()),
                () -> assertEquals("/api/x", r.getPath()),
                () -> assertNotNull(r.getTimestamp()),
                () -> assertFalse(r.getTimestamp().isBefore(start), "timestamp debe ser >= start"),
                () -> assertFalse(r.getTimestamp().isAfter(end), "timestamp debe ser <= end")
        );
    }

    @Test
    @DisplayName("fail(): construye respuesta de error con errores y sin data")
    void fail_shouldBuildErrorResponseWithErrors() {
        Map<String, String> errors = Map.of("field", "invalid");

        Instant start = Instant.now();
        ApiResponse<?> r = ApiResponse.fail("Oops", errors, "/api/y");
        Instant end = Instant.now();

        assertAll(
                () -> assertFalse(r.isSuccess()),
                () -> assertEquals("Oops", r.getMessage()),
                () -> assertNull(r.getData()),
                () -> assertEquals(errors, r.getErrors()),
                () -> assertEquals("/api/y", r.getPath()),
                () -> assertNotNull(r.getTimestamp()),
                () -> assertFalse(r.getTimestamp().isBefore(start)),
                () -> assertFalse(r.getTimestamp().isAfter(end))
        );
    }

    @Test
    @DisplayName("Genéricos: ok() preserva el tipo T")
    void builder_shouldPreserveGenericType() {
        // DTO simple para probar el tipo genérico
        record Loan(int id, String name) {}

        Loan loan = new Loan(7, "Personal");
        ApiResponse<Loan> r = ApiResponse.ok(loan, "ok", "/api/loan");

        assertAll(
                () -> assertEquals(loan, r.getData()),
                () -> assertEquals(7, r.getData().id()),
                () -> assertEquals("Personal", r.getData().name())
        );
    }

    @Test
    @DisplayName("equals/hashCode de Lombok funcionan en base a todos los campos")
    void equalsAndHashCode_shouldWork() {
        Instant fixed = Instant.parse("2025-08-28T12:00:00Z");

        ApiResponse<String> a = ApiResponse.<String>builder()
                .success(true)
                .message("m1")
                .data("d1")
                .errors(null)
                .path("/p1")
                .timestamp(fixed)
                .build();

        ApiResponse<String> b = ApiResponse.<String>builder()
                .success(true)
                .message("m1")
                .data("d1")
                .errors(null)
                .path("/p1")
                .timestamp(fixed)
                .build();

        ApiResponse<String> c = ApiResponse.<String>builder()
                .success(true)
                .message("otro") // cambia 1 campo
                .data("d1")
                .errors(null)
                .path("/p1")
                .timestamp(fixed)
                .build();

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
    }

    @Test
    @DisplayName("errors puede contener distintos tipos (String, Map...)")
    void errors_canHoldDifferentTypes() {
        ApiResponse<?> r1 = ApiResponse.fail("bad", "mensaje simple", "/err1");
        assertTrue(r1.getErrors() instanceof String);

        Map<String, Object> map = new HashMap<>();
        map.put("code", "E400");
        map.put("detail", "Falta el campo X");

        ApiResponse<?> r2 = ApiResponse.fail("bad", map, "/err2");
        assertTrue(r2.getErrors() instanceof Map);
        assertEquals("E400", ((Map<?, ?>) r2.getErrors()).get("code"));
    }

    @Test
    @DisplayName("toString() nunca debe ser nulo")
    void toString_shouldNotBeNull() {
        ApiResponse<Void> r = ApiResponse.<Void>builder()
                .success(true)
                .message("x")
                .data(null)
                .errors(null)
                .path("/p")
                .timestamp(Instant.now())
                .build();

        assertNotNull(r.toString());
        assertTrue(r.toString().contains("success=true"));
    }
}