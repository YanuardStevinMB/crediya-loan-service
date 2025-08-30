package com.crediya.loan.api;

import com.crediya.loan.api.applicationMapper.ApplicationMapper;
import com.crediya.loan.api.dto.ApplicationResponseDto;
import com.crediya.loan.api.dto.ApplicationSaveDto;
import com.crediya.loan.model.application.Application;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationMapperTest {

    private final ApplicationMapper mapper = Mappers.getMapper(ApplicationMapper.class);

    @Test
    void toModel_mapsAllFields() {
        ApplicationSaveDto dto = new ApplicationSaveDto(
                10L,
                new BigDecimal("5000.00"),
                LocalDate.of(2026, 1, 31),
                "john@example.com",
                "123456789",
                3L
        );

        Application model = mapper.toModel(dto);
        assertNotNull(model);
        assertEquals(10L, model.getId());
        assertEquals(new BigDecimal("5000.00"), model.getAmount());
        assertEquals(LocalDate.of(2026, 1, 31), model.getTerm());
        assertEquals("john@example.com", model.getEmail());
        assertEquals("123456789", model.getIdentityDocument());
        assertEquals(3L, model.getLoanTypeId());
    }

    @Test
    void toModel_null_returnsNull() {
        assertNull(mapper.toModel(null));
    }

    @Test
    void toResponseDto_mapsAllFields() {
        Application model = Application.builder()
                .id(22L)
                .amount(new BigDecimal("1500.50"))
                .term(LocalDate.of(2025, 12, 15))
                .email("a@b.co")
                .identityDocument("987654321")
                .stateId(7L)
                .loanTypeId(4L)
                .build();

        ApplicationResponseDto dto = mapper.toResponseDto(model);
        assertNotNull(dto);
        assertEquals(22L, dto.getId());
        assertEquals(new BigDecimal("1500.50"), dto.getAmount());
        assertEquals(LocalDate.of(2025, 12, 15), dto.getTerm());
        assertEquals("a@b.co", dto.getEmail());
        assertEquals("987654321", dto.getIdentityDocument());
        assertEquals(7L, dto.getStateId());
        assertEquals(4L, dto.getLoanTypeId());
    }
}
