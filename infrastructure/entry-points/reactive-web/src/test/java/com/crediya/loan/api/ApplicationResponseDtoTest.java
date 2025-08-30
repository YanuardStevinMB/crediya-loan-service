package com.crediya.loan.api;


import com.crediya.loan.api.dto.ApplicationResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationResponseDtoTest {

    @Test
    @DisplayName("Getters/Setters: persisten los valores asignados")
    void gettersSetters_shouldPersistValues() {
        ApplicationResponseDto dto = new ApplicationResponseDto();

        Long id = 1L;
        BigDecimal amount = new BigDecimal("12345.67");
        LocalDate term = LocalDate.of(2025, 12, 15);
        String email = "user@example.com";
        String identityDocument = "1234567890";
        Long stateId = 9L;
        Long loanTypeId = 2L;

        dto.setId(id);
        dto.setAmount(amount);
        dto.setTerm(term);
        dto.setEmail(email);
        dto.setIdentityDocument(identityDocument);
        dto.setStateId(stateId);
        dto.setLoanTypeId(loanTypeId);

        assertAll(
                () -> assertEquals(id, dto.getId()),
                () -> assertEquals(amount, dto.getAmount()),
                () -> assertEquals(term, dto.getTerm()),
                () -> assertEquals(email, dto.getEmail()),
                () -> assertEquals(identityDocument, dto.getIdentityDocument()),
                () -> assertEquals(stateId, dto.getStateId()),
                () -> assertEquals(loanTypeId, dto.getLoanTypeId())
        );
    }

    @Test
    @DisplayName("equals/hashCode: usan todos los campos")
    void equalsHashCode_shouldUseAllFields() {
        ApplicationResponseDto a = sample();
        ApplicationResponseDto b = sample();

        ApplicationResponseDto c = sample();
        c.setAmount(new BigDecimal("999.99")); // cambia 1 campo

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
    }

    @Test
    @DisplayName("toString(): no nulo y contiene campos")
    void toString_shouldContainFields() {
        ApplicationResponseDto dto = sample();
        String s = dto.toString();

        assertNotNull(s);
        assertTrue(s.contains("ApplicationResponseDto"));
        assertTrue(s.contains("id=1"));
        assertTrue(s.contains("amount=12345.67"));
    }

    @Test
    @DisplayName("Por defecto: todos los campos son null")
    void defaultConstructor_shouldHaveNullFields() {
        ApplicationResponseDto dto = new ApplicationResponseDto();

        assertAll(
                () -> assertNull(dto.getId()),
                () -> assertNull(dto.getAmount()),
                () -> assertNull(dto.getTerm()),
                () -> assertNull(dto.getEmail()),
                () -> assertNull(dto.getIdentityDocument()),
                () -> assertNull(dto.getStateId()),
                () -> assertNull(dto.getLoanTypeId())
        );
    }

    @Test
    @DisplayName("BigDecimal: conserva la escala (sin normalización)")
    void bigDecimal_shouldPreserveScale() {
        ApplicationResponseDto dto = new ApplicationResponseDto();
        BigDecimal withScale = new BigDecimal("100.00");

        dto.setAmount(withScale);

        // equals() de BigDecimal considera la escala: validamos que no se cambió.
        assertEquals(new BigDecimal("100.00"), dto.getAmount());
        assertEquals(2, dto.getAmount().scale());
    }

    // ==== Helper ====
    private static ApplicationResponseDto sample() {
        ApplicationResponseDto dto = new ApplicationResponseDto();
        dto.setId(1L);
        dto.setAmount(new BigDecimal("12345.67"));
        dto.setTerm(LocalDate.of(2025, 12, 15));
        dto.setEmail("user@example.com");
        dto.setIdentityDocument("1234567890");
        dto.setStateId(9L);
        dto.setLoanTypeId(2L);
        return dto;
    }
}