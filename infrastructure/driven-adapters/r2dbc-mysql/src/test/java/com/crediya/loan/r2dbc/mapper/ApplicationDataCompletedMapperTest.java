package com.crediya.loan.r2dbc.mapper;


import com.crediya.loan.model.application.ApplicationDataCompleted;
import com.crediya.loan.r2dbc.dto.ApplicationDto;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationDataCompletedMapperTest {

    private final ApplicationDataCompletedMapper mapper =
            Mappers.getMapper(ApplicationDataCompletedMapper.class);

    @Test
    void toDto_shouldMapAllFieldsCorrectly() {
        ApplicationDataCompleted domain = ApplicationDataCompleted.builder()
                .id(1L)
                .amount(BigDecimal.valueOf(5000))
                .email("test@mail.com")
                .identityDocument("123456")
                .state("APROBADO")
                .loan("PERSONAL")
                .stateId(10L)
                .loanTypeId(20L)
                .build();

        ApplicationDto dto = mapper.toDto(domain);

        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals(BigDecimal.valueOf(5000), dto.getAmount());
        assertEquals("test@mail.com", dto.getEmail());
        assertEquals("123456", dto.getIdentityDocument());
        assertEquals("APROBADO", dto.getState());
        assertEquals("PERSONAL", dto.getLoan());
        assertEquals(10L, dto.getStateId());
        assertEquals(20L, dto.getLoanTypeId());
    }

    @Test
    void toDomain_shouldMapAllFieldsCorrectly() {
        ApplicationDto dto = ApplicationDto.builder()
                .id(2L)
                .amount(BigDecimal.valueOf(8000))
                .email("user@mail.com")
                .identityDocument("654321")
                .state("RECHAZADO")
                .loan("HIPOTECARIO")
                .stateId(30L)
                .loanTypeId(40L)
                .build();

        ApplicationDataCompleted domain = mapper.toDomain(dto);

        assertNotNull(domain);
        assertEquals(2L, domain.getId());
        assertEquals(BigDecimal.valueOf(8000), domain.getAmount());
        assertEquals("user@mail.com", domain.getEmail());
        assertEquals("654321", domain.getIdentityDocument());
        assertEquals("RECHAZADO", domain.getState());
        assertEquals("HIPOTECARIO", domain.getLoan());
        assertEquals(30L, domain.getStateId());
        assertEquals(40L, domain.getLoanTypeId());
    }

    @Test
    void nullMappings_shouldReturnNull() {
        assertNull(mapper.toDto(null));
        assertNull(mapper.toDomain(null));
    }
}
