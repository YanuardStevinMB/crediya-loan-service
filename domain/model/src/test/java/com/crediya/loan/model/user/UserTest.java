package com.crediya.loan.model.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    @DisplayName("NoArgs + setters/getters deben funcionar")
    void noArgsAndSettersGetters_work() {
        User user = new User();
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setIdentityDocument("1234567890");
        user.setBaseSalary(new BigDecimal("3500.00"));

        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertEquals("1234567890", user.getIdentityDocument());
        assertEquals(new BigDecimal("3500.00"), user.getBaseSalary());
    }

    @Test
    @DisplayName("AllArgsConstructor debe setear todos los campos")
    void allArgsConstructor_setsAllFields() {
        User user = new User(
                "Jane",
                "Smith", 
                "CC-987654321",
                new BigDecimal("4200.75")
        );

        assertEquals("Jane", user.getFirstName());
        assertEquals("Smith", user.getLastName());
        assertEquals("CC-987654321", user.getIdentityDocument());
        assertEquals(new BigDecimal("4200.75"), user.getBaseSalary());
    }

    @Test
    @DisplayName("Builder debe construir correctamente")
    void builder_buildsCorrectly() {
        User user = User.builder()
                .firstName("Bob")
                .lastName("Johnson")
                .identityDocument("DOC-123456")
                .baseSalary(new BigDecimal("2800.00"))
                .build();

        assertEquals("Bob", user.getFirstName());
        assertEquals("Johnson", user.getLastName());
        assertEquals("DOC-123456", user.getIdentityDocument());
        assertEquals(new BigDecimal("2800.00"), user.getBaseSalary());
    }

    @Test
    @DisplayName("Builder acepta nulos sin lanzar excepción")
    void builder_allowsNulls() {
        User user = User.builder()
                .firstName(null)
                .lastName(null)
                .identityDocument(null)
                .baseSalary(null)
                .build();

        assertNull(user.getFirstName());
        assertNull(user.getLastName());
        assertNull(user.getIdentityDocument());
        assertNull(user.getBaseSalary());
    }

    @Test
    @DisplayName("Debe manejar strings vacíos correctamente")
    void builder_handlesEmptyStrings() {
        User user = User.builder()
                .firstName("")
                .lastName("")
                .identityDocument("")
                .baseSalary(new BigDecimal("0.00"))
                .build();

        assertEquals("", user.getFirstName());
        assertEquals("", user.getLastName());
        assertEquals("", user.getIdentityDocument());
        assertEquals(new BigDecimal("0.00"), user.getBaseSalary());
    }

    @Test
    @DisplayName("Debe permitir salarios decimales complejos")
    void builder_handlesComplexSalaries() {
        BigDecimal complexSalary = new BigDecimal("5432.9876");
        
        User user = User.builder()
                .firstName("Alice")
                .lastName("Wonder")
                .identityDocument("ID-789")
                .baseSalary(complexSalary)
                .build();

        assertEquals("Alice", user.getFirstName());
        assertEquals("Wonder", user.getLastName());
        assertEquals("ID-789", user.getIdentityDocument());
        assertEquals(complexSalary, user.getBaseSalary());
    }

    @Test
    @DisplayName("Debe manejar nombres con caracteres especiales")
    void builder_handlesSpecialCharacters() {
        User user = User.builder()
                .firstName("José María")
                .lastName("García-López")
                .identityDocument("CC-123.456.789-0")
                .baseSalary(new BigDecimal("3000.00"))
                .build();

        assertEquals("José María", user.getFirstName());
        assertEquals("García-López", user.getLastName());
        assertEquals("CC-123.456.789-0", user.getIdentityDocument());
        assertEquals(new BigDecimal("3000.00"), user.getBaseSalary());
    }

    @Test
    @DisplayName("Debe manejar salario cero y negativo")
    void builder_handlesZeroAndNegativeSalaries() {
        User userZero = User.builder()
                .firstName("Zero")
                .lastName("Salary")
                .identityDocument("ZERO-001")
                .baseSalary(BigDecimal.ZERO)
                .build();

        User userNegative = User.builder()
                .firstName("Negative")
                .lastName("Salary")
                .identityDocument("NEG-001")
                .baseSalary(new BigDecimal("-100.00"))
                .build();

        assertEquals(BigDecimal.ZERO, userZero.getBaseSalary());
        assertEquals(new BigDecimal("-100.00"), userNegative.getBaseSalary());
    }
}