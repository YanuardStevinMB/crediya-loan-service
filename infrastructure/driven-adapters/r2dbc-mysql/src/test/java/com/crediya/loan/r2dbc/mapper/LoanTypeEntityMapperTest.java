package com.crediya.loan.r2dbc.mapper;

import com.crediya.loan.model.loantype.LoanType;
import com.crediya.loan.r2dbc.entity.LoanTypeEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class LoanTypeEntityMapperTest {

    LoanTypeEntityMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new LoanTypeEntityMapper() {}; // Implementación anónima para testing
    }

    private LoanType buildLoanType(Long id, String name) {
        return LoanType.builder()
                .id(id)
                .name(name)
                .amountMin(BigDecimal.valueOf(1000))
                .amountMax(BigDecimal.valueOf(50000))
                .interestRate(BigDecimal.valueOf(12.5))
                .automaticValidation(true)
                .build();
    }

    private LoanTypeEntity buildLoanTypeEntity(Long id, String name) {
        return LoanTypeEntity.builder()
                .id(id)
                .name(name)
                .amountMin(BigDecimal.valueOf(1000))
                .amountMax(BigDecimal.valueOf(50000))
                .interestRate(BigDecimal.valueOf(12.5))
                .automaticValidation(true)
                .build();
    }

    // ---------- toDomain -----------

    @Test
    void toDomain_null_returnsNull() {
        LoanType result = mapper.toDomain(null);
        assertNull(result);
    }

    @Test
    void toDomain_validEntity_mapsCorrectly() {
        var entity = buildLoanTypeEntity(1L, "Personal Loan");

        LoanType result = mapper.toDomain(entity);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Personal Loan", result.getName());
        assertEquals(BigDecimal.valueOf(1000), result.getAmountMin());
        assertEquals(BigDecimal.valueOf(50000), result.getAmountMax());
        assertEquals(BigDecimal.valueOf(12.5), result.getInterestRate());
        assertTrue(result.getAutomaticValidation());
    }

    @Test
    void toDomain_entityWithNullId_mapsCorrectly() {
        var entity = buildLoanTypeEntity(null, "Personal Loan");

        LoanType result = mapper.toDomain(entity);

        assertNotNull(result);
        assertNull(result.getId());
        assertEquals("Personal Loan", result.getName());
    }

    @Test
    void toDomain_entityWithFalseValidation_mapsCorrectly() {
        var entity = buildLoanTypeEntity(1L, "Manual Loan");
        entity.setAutomaticValidation(false);

        LoanType result = mapper.toDomain(entity);

        assertNotNull(result);
        assertFalse(result.getAutomaticValidation());
    }

    // ---------- toEntity -----------

    @Test
    void toEntity_null_returnsNull() {
        LoanTypeEntity result = mapper.toEntity(null);
        assertNull(result);
    }

    @Test
    void toEntity_validDomain_mapsCorrectly() {
        var domain = buildLoanType(1L, "Personal Loan");

        LoanTypeEntity result = mapper.toEntity(domain);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Personal Loan", result.getName());
        assertEquals(BigDecimal.valueOf(1000), result.getAmountMin());
        assertEquals(BigDecimal.valueOf(50000), result.getAmountMax());
        assertEquals(BigDecimal.valueOf(12.5), result.getInterestRate());
        assertTrue(result.getAutomaticValidation());
    }

    @Test
    void toEntity_domainWithNullId_mapsCorrectly() {
        var domain = buildLoanType(null, "Personal Loan");

        LoanTypeEntity result = mapper.toEntity(domain);

        assertNotNull(result);
        assertNull(result.getId());
        assertEquals("Personal Loan", result.getName());
    }

    // ---------- Round trip tests -----------

    @Test
    void roundTrip_domainToEntityToDomain_preservesData() {
        var originalDomain = buildLoanType(1L, "Personal Loan");

        LoanTypeEntity entity = mapper.toEntity(originalDomain);
        LoanType resultDomain = mapper.toDomain(entity);

        assertNotNull(resultDomain);
        assertEquals(originalDomain.getId(), resultDomain.getId());
        assertEquals(originalDomain.getName(), resultDomain.getName());
        assertEquals(originalDomain.getAmountMin(), resultDomain.getAmountMin());
        assertEquals(originalDomain.getAmountMax(), resultDomain.getAmountMax());
        assertEquals(originalDomain.getInterestRate(), resultDomain.getInterestRate());
        assertEquals(originalDomain.getAutomaticValidation(), resultDomain.getAutomaticValidation());
    }

    @Test
    void roundTrip_entityToDomainToEntity_preservesData() {
        var originalEntity = buildLoanTypeEntity(1L, "Personal Loan");

        LoanType domain = mapper.toDomain(originalEntity);
        LoanTypeEntity resultEntity = mapper.toEntity(domain);

        assertNotNull(resultEntity);
        assertEquals(originalEntity.getId(), resultEntity.getId());
        assertEquals(originalEntity.getName(), resultEntity.getName());
        assertEquals(originalEntity.getAmountMin(), resultEntity.getAmountMin());
        assertEquals(originalEntity.getAmountMax(), resultEntity.getAmountMax());
        assertEquals(originalEntity.getInterestRate(), resultEntity.getInterestRate());
        assertEquals(originalEntity.getAutomaticValidation(), resultEntity.getAutomaticValidation());
    }
}
