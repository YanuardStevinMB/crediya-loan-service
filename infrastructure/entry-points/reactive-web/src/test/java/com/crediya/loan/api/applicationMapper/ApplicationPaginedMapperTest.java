package com.crediya.loan.api.applicationMapper;

import com.crediya.loan.api.dto.ApplicationPaginedDto;
import com.crediya.loan.model.application.ApplicationPagined;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationPaginedMapperTest {

    private final ApplicationPaginedMapper mapper =
            Mappers.getMapper(ApplicationPaginedMapper.class);

    @Test
    void toResponseDto_shouldMapAllFields_whenModelIsComplete() {
        // Given
        ApplicationPagined model = ApplicationPagined.builder()
                .id(123L)
                .amount(new BigDecimal("1500000.50"))
                .term(LocalDate.of(2025, 1, 15))
                .email("test@example.com")
                .identityDocument("1090123456")
                .state("PENDIENTE")
                .loan("LIBRE_INVERSION")
                .stateId(10L)
                .loanTypeId(3L)
                .fullName("Juan Pérez")
                .baseSalary(new BigDecimal("2500000"))
                .build();

        // When
        ApplicationPaginedDto dto = mapper.toResponseDto(model);

        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.id()).isEqualTo(123L);
        assertThat(dto.amount()).isEqualByComparingTo("1500000.50");
        assertThat(dto.term()).isEqualTo(LocalDate.of(2025, 1, 15));
        assertThat(dto.email()).isEqualTo("test@example.com");
        assertThat(dto.identityDocument()).isEqualTo("1090123456");
        assertThat(dto.state()).isEqualTo("PENDIENTE");
        assertThat(dto.loan()).isEqualTo("LIBRE_INVERSION");
        assertThat(dto.stateId()).isEqualTo(10L);
        assertThat(dto.loanTypeId()).isEqualTo(3L);
        assertThat(dto.fullName()).isEqualTo("Juan Pérez");
        assertThat(dto.baseSalary()).isEqualByComparingTo("2500000");
    }

    @Test
    void toResponseDto_shouldReturnNull_whenModelIsNull() {
        // When
        ApplicationPaginedDto dto = mapper.toResponseDto(null);

        // Then
        assertThat(dto).isNull();
    }

    @Test
    void toResponseDto_shouldMapNullFieldsAsNull() {
        // Given: algunos campos nulos
        ApplicationPagined model = ApplicationPagined.builder()
                .id(1L)
                .amount(null)
                .term(null)
                .email("user@mail.com")
                .identityDocument(null)
                .state(null)
                .loan("EDUCATIVO")
                .stateId(null)
                .loanTypeId(7L)
                .fullName(null)
                .baseSalary(null)
                .build();

        // When
        ApplicationPaginedDto dto = mapper.toResponseDto(model);

        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.amount()).isNull();
        assertThat(dto.term()).isNull();
        assertThat(dto.email()).isEqualTo("user@mail.com");
        assertThat(dto.identityDocument()).isNull();
        assertThat(dto.state()).isNull();
        assertThat(dto.loan()).isEqualTo("EDUCATIVO");
        assertThat(dto.stateId()).isNull();
        assertThat(dto.loanTypeId()).isEqualTo(7L);
        assertThat(dto.fullName()).isNull();
        assertThat(dto.baseSalary()).isNull();
    }
}
