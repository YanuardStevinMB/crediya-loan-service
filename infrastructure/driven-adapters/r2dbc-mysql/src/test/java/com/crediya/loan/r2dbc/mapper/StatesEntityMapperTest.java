package com.crediya.loan.r2dbc.mapper;

import com.crediya.loan.model.states.States;
import com.crediya.loan.r2dbc.entity.StatesEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class StatesEntityMapperTest {

    StatesEntityMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new StatesEntityMapper() {}; // Implementación anónima para testing
    }

    private States buildStates(Long id, String code, String name) {
        return States.builder()
                .id(id)
                .code(code)
                .name(name)
                .description("State description")
                .build();
    }

    private StatesEntity buildStatesEntity(Long id, String code, String name) {
        return StatesEntity.builder()
                .id(id)
                .code(code)
                .name(name)
                .description("State description")
                .build();
    }

    // ---------- toEntity -----------

    @Test
    void toEntity_null_returnsNull() {
        StatesEntity result = mapper.toEntity(null);
        assertNull(result);
    }

    @Test
    void toEntity_validDomain_mapsCorrectly() {
        var domain = buildStates(1L, "PEN", "Pending");

        StatesEntity result = mapper.toEntity(domain);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("PEN", result.getCode());
        assertEquals("Pending", result.getName());
        assertEquals("State description", result.getDescription());
    }

    @Test
    void toEntity_domainWithNullId_mapsCorrectly() {
        var domain = buildStates(null, "PEN", "Pending");

        StatesEntity result = mapper.toEntity(domain);

        assertNotNull(result);
        assertNull(result.getId());
        assertEquals("PEN", result.getCode());
        assertEquals("Pending", result.getName());
    }

    // ---------- toDomain -----------

    @Test
    void toDomain_null_returnsNull() {
        States result = mapper.toDomain(null);
        assertNull(result);
    }

    @Test
    void toDomain_validEntity_mapsCorrectly() {
        var entity = buildStatesEntity(1L, "PEN", "Pending");

        States result = mapper.toDomain(entity);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("PEN", result.getCode());
        assertEquals("Pending", result.getName());
        assertEquals("State description", result.getDescription());
    }

    @Test
    void toDomain_entityWithNullId_mapsCorrectly() {
        var entity = buildStatesEntity(null, "PEN", "Pending");

        States result = mapper.toDomain(entity);

        assertNotNull(result);
        assertNull(result.getId());
        assertEquals("PEN", result.getCode());
        assertEquals("Pending", result.getName());
    }

    @Test
    void toDomain_entityWithNullDescription_mapsCorrectly() {
        var entity = buildStatesEntity(1L, "PEN", "Pending");
        entity.setDescription(null);

        States result = mapper.toDomain(entity);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("PEN", result.getCode());
        assertEquals("Pending", result.getName());
        assertNull(result.getDescription());
    }

    // ---------- Round trip tests -----------

    @Test
    void roundTrip_domainToEntityToDomain_preservesData() {
        var originalDomain = buildStates(1L, "PEN", "Pending");

        StatesEntity entity = mapper.toEntity(originalDomain);
        States resultDomain = mapper.toDomain(entity);

        assertNotNull(resultDomain);
        assertEquals(originalDomain.getId(), resultDomain.getId());
        assertEquals(originalDomain.getCode(), resultDomain.getCode());
        assertEquals(originalDomain.getName(), resultDomain.getName());
        assertEquals(originalDomain.getDescription(), resultDomain.getDescription());
    }

    @Test
    void roundTrip_entityToDomainToEntity_preservesData() {
        var originalEntity = buildStatesEntity(1L, "PEN", "Pending");

        States domain = mapper.toDomain(originalEntity);
        StatesEntity resultEntity = mapper.toEntity(domain);

        assertNotNull(resultEntity);
        assertEquals(originalEntity.getId(), resultEntity.getId());
        assertEquals(originalEntity.getCode(), resultEntity.getCode());
        assertEquals(originalEntity.getName(), resultEntity.getName());
        assertEquals(originalEntity.getDescription(), resultEntity.getDescription());
    }
}
