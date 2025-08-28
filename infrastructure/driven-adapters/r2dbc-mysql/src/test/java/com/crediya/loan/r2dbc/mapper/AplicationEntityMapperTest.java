package com.crediya.loan.r2dbc.mapper;

import com.crediya.loan.model.application.Application;
import com.crediya.loan.r2dbc.entity.ApplicationEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AplicationEntityMapperTest {

    AplicationEntityMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new AplicationEntityMapper() {}; // Implementación anónima para testing
    }

    private Application buildApplication(Long id, String email) {
        return Application.builder()
                .id(id)
                .email(email)
                .amount(BigDecimal.valueOf(5000))
                .term(LocalDate.of(2025, 12, 31))
                .identityDocument("12345678")
                .loanTypeId(1L)
                .stateId(100L)
                .build();
    }

    private ApplicationEntity buildApplicationEntity(Long id, String email) {
        return ApplicationEntity.builder()
                .id(id)
                .email(email)
                .amount(BigDecimal.valueOf(5000))
                .term(LocalDate.of(2025, 12, 31))
                .identityDocument("12345678")
                .loanTypeId(1L)
                .stateId(100L)
                .build();
    }

    // ---------- toEntity -----------

    @Test
    void toEntity_null_returnsNull() {
        ApplicationEntity result = mapper.toEntity(null);
        assertNull(result);
    }

    @Test
    void toEntity_validDomain_mapsCorrectly() {
        var domain = buildApplication(1L, "test@example.com");

        ApplicationEntity result = mapper.toEntity(domain);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("test@example.com", result.getEmail());
        assertEquals(BigDecimal.valueOf(5000), result.getAmount());
        assertEquals(LocalDate.of(2025, 12, 31), result.getTerm());
        assertEquals("12345678", result.getIdentityDocument());
        assertEquals(1L, result.getLoanTypeId());
        assertEquals(100L, result.getStateId());
    }

    @Test
    void toEntity_domainWithNullId_mapsCorrectly() {
        var domain = buildApplication(null, "test@example.com");

        ApplicationEntity result = mapper.toEntity(domain);

        assertNotNull(result);
        assertNull(result.getId());
        assertEquals("test@example.com", result.getEmail());
        assertEquals(BigDecimal.valueOf(5000), result.getAmount());
    }

    // ---------- toDomain -----------

    @Test
    void toDomain_null_returnsNull() {
        Application result = mapper.toDomain(null);
        assertNull(result);
    }

    @Test
    void toDomain_validEntity_mapsCorrectly() {
        var entity = buildApplicationEntity(1L, "test@example.com");

        Application result = mapper.toDomain(entity);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("test@example.com", result.getEmail());
        assertEquals(BigDecimal.valueOf(5000), result.getAmount());
        assertEquals(LocalDate.of(2025, 12, 31), result.getTerm());
        assertEquals("12345678", result.getIdentityDocument());
        assertEquals(1L, result.getLoanTypeId());
        assertEquals(100L, result.getStateId());
    }

    @Test
    void toDomain_entityWithNullId_mapsCorrectly() {
        var entity = buildApplicationEntity(null, "test@example.com");

        Application result = mapper.toDomain(entity);

        assertNotNull(result);
        assertNull(result.getId());
        assertEquals("test@example.com", result.getEmail());
        assertEquals(BigDecimal.valueOf(5000), result.getAmount());
    }

    // ---------- Round trip tests -----------

    @Test
    void roundTrip_domainToEntityToDomain_preservesData() {
        var originalDomain = buildApplication(1L, "test@example.com");

        ApplicationEntity entity = mapper.toEntity(originalDomain);
        Application resultDomain = mapper.toDomain(entity);

        assertNotNull(resultDomain);
        assertEquals(originalDomain.getId(), resultDomain.getId());
        assertEquals(originalDomain.getEmail(), resultDomain.getEmail());
        assertEquals(originalDomain.getAmount(), resultDomain.getAmount());
        assertEquals(originalDomain.getTerm(), resultDomain.getTerm());
        assertEquals(originalDomain.getIdentityDocument(), resultDomain.getIdentityDocument());
        assertEquals(originalDomain.getLoanTypeId(), resultDomain.getLoanTypeId());
        assertEquals(originalDomain.getStateId(), resultDomain.getStateId());
    }

    @Test
    void roundTrip_entityToDomainToEntity_preservesData() {
        var originalEntity = buildApplicationEntity(1L, "test@example.com");

        Application domain = mapper.toDomain(originalEntity);
        ApplicationEntity resultEntity = mapper.toEntity(domain);

        assertNotNull(resultEntity);
        assertEquals(originalEntity.getId(), resultEntity.getId());
        assertEquals(originalEntity.getEmail(), resultEntity.getEmail());
        assertEquals(originalEntity.getAmount(), resultEntity.getAmount());
        assertEquals(originalEntity.getTerm(), resultEntity.getTerm());
        assertEquals(originalEntity.getIdentityDocument(), resultEntity.getIdentityDocument());
        assertEquals(originalEntity.getLoanTypeId(), resultEntity.getLoanTypeId());
        assertEquals(originalEntity.getStateId(), resultEntity.getStateId());
    }
}
