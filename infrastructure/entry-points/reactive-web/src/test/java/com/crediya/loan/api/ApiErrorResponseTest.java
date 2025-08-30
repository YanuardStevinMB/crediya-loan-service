package com.crediya.loan.api;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApiErrorResponseTest {

    @Test
    void constructor_y_getters() {
        ApiErrorResponse r = new ApiErrorResponse(
                "2025-08-28T05:18:04Z",
                400,
                "Bad Request",
                "Datos de entrada inválidos",
                "/api/v1/usuarios",
                "POST"
        );

        assertEquals("2025-08-28T05:18:04Z", r.timestamp());
        assertEquals(400, r.status());
        assertEquals("Bad Request", r.error());
        assertEquals("Datos de entrada inválidos", r.message());
        assertEquals("/api/v1/usuarios", r.path());
        assertEquals("POST", r.method());
    }

    @Test
    void equals_y_hashCode() {
        ApiErrorResponse a = new ApiErrorResponse("t", 400, "e", "m", "/p", "POST");
        ApiErrorResponse b = new ApiErrorResponse("t", 400, "e", "m", "/p", "POST");
        ApiErrorResponse c = new ApiErrorResponse("t", 401, "e", "m", "/p", "POST"); // cambia status

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
    }

    @Test
    void toString_noNulo_y_contiene_campos() {
        ApiErrorResponse r = new ApiErrorResponse("t", 400, "e", "m", "/p", "POST");
        String s = r.toString();
        assertNotNull(s);
        assertTrue(s.contains("ApiErrorResponse"));
        assertTrue(s.contains("status=400"));
    }
}