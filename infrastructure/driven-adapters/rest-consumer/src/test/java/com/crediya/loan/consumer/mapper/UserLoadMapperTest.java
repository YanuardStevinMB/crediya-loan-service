package com.crediya.loan.consumer.mapper;

import com.crediya.loan.consumer.dto.UserDto;
import com.crediya.loan.model.user.User;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class UserLoadMapperTest {

    private static UserLoadMapper mapper;

    @BeforeAll
    static void init() {
        mapper = Mappers.getMapper(UserLoadMapper.class);
        assertNotNull(mapper, "El mapper generado por MapStruct no debería ser nulo");
    }

    @Test
    void toDomain_shouldMapAllFields() {
        UserDto dto = UserDto.builder()
                .firstName("Ana")
                .lastName("Diaz")
                .identityDocument("CC123")
                .baseSalary(new BigDecimal("1234.56"))
                .build();

        User user = mapper.toDomain(dto);

        assertNotNull(user);
        assertEquals("Ana", user.getFirstName());
        assertEquals("Diaz", user.getLastName());
        assertEquals("CC123", user.getIdentityDocument());
        assertEquals(new BigDecimal("1234.56"), user.getBaseSalary());
    }

    @Test
    void toDomain_shouldReturnNull_whenDtoIsNull() {
        assertNull(mapper.toDomain(null));
    }

    @Test
    void toDomain_shouldAllowNullFields() {
        UserDto dto = UserDto.builder()
                .firstName(null)
                .lastName(null)
                .identityDocument(null)
                .baseSalary(null)
                .build();

        User user = mapper.toDomain(dto);

        assertNotNull(user);
        assertNull(user.getFirstName());
        assertNull(user.getLastName());
        assertNull(user.getIdentityDocument());
        assertNull(user.getBaseSalary());
    }
}
