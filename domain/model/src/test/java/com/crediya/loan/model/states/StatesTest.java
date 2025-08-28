package com.crediya.loan.model.states;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StatesTest {

    @Test
    @DisplayName("NoArgs + setters/getters deben funcionar")
    void noArgsAndSettersGetters_work() {
        States s = new States();
        s.setId(1L);
        s.setName("Pending");
        s.setDescription("State description");
        s.setCode("PEN");

        assertEquals(1L, s.getId());
        assertEquals("Pending", s.getName());
        assertEquals("State description", s.getDescription());
        assertEquals("PEN", s.getCode());
    }

    @Test
    @DisplayName("AllArgsConstructor debe setear todos los campos")
    void allArgsConstructor_setsAllFields() {
        States s = new States(2L, "Approved", "Approved state", "APR");

        assertEquals(2L, s.getId());
        assertEquals("Approved", s.getName());
        assertEquals("Approved state", s.getDescription());
        assertEquals("APR", s.getCode());
    }

    @Test
    @DisplayName("Builder debe construir correctamente")
    void builder_buildsCorrectly() {
        States s = States.builder()
                .id(3L)
                .name("Rejected")
                .description("Rejected state")
                .code("REJ")
                .build();

        assertEquals(3L, s.getId());
        assertEquals("Rejected", s.getName());
        assertEquals("Rejected state", s.getDescription());
        assertEquals("REJ", s.getCode());
    }

    @Test
    @DisplayName("toBuilder debe clonar y permitir modificar un campo")
    void toBuilder_clonesAndModifies() {
        States original = States.builder()
                .id(4L)
                .name("Pending")
                .description("Desc")
                .code("PEN")
                .build();

        States modified = original.toBuilder()
                .name("In Review")
                .build();

        assertNotSame(original, modified);
        assertEquals(original.getId(), modified.getId());
        assertEquals("In Review", modified.getName());
        assertEquals(original.getDescription(), modified.getDescription());
        assertEquals(original.getCode(), modified.getCode());
    }

    @Test
    @DisplayName("Builder acepta nulos sin lanzar excepción")
    void builder_allowsNulls() {
        States s = States.builder()
                .id(null)
                .name(null)
                .description(null)
                .code(null)
                .build();

        assertNull(s.getId());
        assertNull(s.getName());
        assertNull(s.getDescription());
        assertNull(s.getCode());
    }
}