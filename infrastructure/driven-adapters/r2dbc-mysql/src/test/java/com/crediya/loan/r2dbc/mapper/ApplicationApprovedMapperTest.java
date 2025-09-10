package com.crediya.loan.r2dbc.mapper;


import com.crediya.loan.model.application.ApplicationApproved;
import com.crediya.loan.r2dbc.dto.ApplicationApprovedDto;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationApprovedMapperTest {

    private final ApplicationApprovedMapper mapper =
            Mappers.getMapper(ApplicationApprovedMapper.class);

    @Test
    void toDto_shouldMapAllFieldsCorrectly() {
        ApplicationApproved domain = ApplicationApproved.builder()
                .amount(BigDecimal.valueOf(15000))
                .interestRate(BigDecimal.valueOf(12.5))
                .termMonths(24L)
                .build();

        ApplicationApprovedDto dto = mapper.toDto(domain);

        assertNotNull(dto);
        assertEquals(BigDecimal.valueOf(15000), dto.getAmount());
        assertEquals(BigDecimal.valueOf(12.5), dto.getInterestRate());
        assertEquals(24L, dto.getTermMonths());
    }

    @Test
    void toDomain_shouldMapAllFieldsCorrectly() {
        ApplicationApprovedDto dto = ApplicationApprovedDto.builder()
                .amount(BigDecimal.valueOf(20000))
                .interestRate(BigDecimal.valueOf(9.8))
                .termMonths(36L)
                .build();

        ApplicationApproved domain = mapper.toDomain(dto);

        assertNotNull(domain);
        assertEquals(BigDecimal.valueOf(20000), domain.getAmount());
        assertEquals(BigDecimal.valueOf(9.8), domain.getInterestRate());
        assertEquals(36L, domain.getTermMonths());
    }

    @Test
    void nullMappings_shouldReturnNull() {
        assertNull(mapper.toDto(null));
        assertNull(mapper.toDomain(null));
    }
}
