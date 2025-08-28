package com.crediya.loan.r2dbc.states;

import com.crediya.loan.model.states.States;
import com.crediya.loan.r2dbc.entity.StatesEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivecommons.utils.ObjectMapper;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatesReactiveRepositoryAdapterTest {

    @Mock
    StatesReactiveRepository repository;

    @Mock
    ObjectMapper mapper;

    @InjectMocks
    StatesReactiveRepositoryAdapter adapter;

    @BeforeEach
    void init() {
        adapter = new StatesReactiveRepositoryAdapter(repository, mapper);
    }

    private StatesEntity buildStatesEntity(Long id, String code, String name) {
        return StatesEntity.builder()
                .id(id)
                .code(code)
                .name(name)
                .description("State description")
                .build();
    }

    private States buildStates(Long id, String code, String name) {
        return States.builder()
                .id(id)
                .code(code)
                .name(name)
                .description("State description")
                .build();
    }

    // ---------- findByCode -----------

    @Test
    void findByCode_nullCode_callsRepository() {
        when(repository.findByCode(null)).thenReturn(Mono.empty());

        StepVerifier.create(adapter.findByCode(null))
                .verifyComplete();

        verify(repository, times(1)).findByCode(null);
        verifyNoInteractions(mapper);
    }

    @Test
    void findByCode_emptyCode_callsRepository() {
        when(repository.findByCode("")).thenReturn(Mono.empty());

        StepVerifier.create(adapter.findByCode(""))
                .verifyComplete();

        verify(repository, times(1)).findByCode("");
        verifyNoInteractions(mapper);
    }

    @Test
    void findByCode_notFound_returnsEmpty() {
        when(repository.findByCode("PEN")).thenReturn(Mono.empty());

        StepVerifier.create(adapter.findByCode("PEN"))
                .verifyComplete();

        verify(repository, times(1)).findByCode("PEN");
        verifyNoInteractions(mapper);
    }

    @Test
    void findByCode_found_mapsAndReturns() {
        var entity = buildStatesEntity(1L, "PEN", "Pending");
        var domain = buildStates(1L, "PEN", "Pending");

        when(repository.findByCode("PEN")).thenReturn(Mono.just(entity));
        when(mapper.map(entity, States.class)).thenReturn(domain);

        StepVerifier.create(adapter.findByCode("PEN"))
                .expectNextMatches(result -> 
                    result.getId().equals(1L) && 
                    result.getCode().equals("PEN") &&
                    result.getName().equals("Pending"))
                .verifyComplete();

        verify(repository, times(1)).findByCode("PEN");
        verify(mapper, times(1)).map(entity, States.class);
    }

    @Test
    void findByCode_repositoryError_propagatesError() {
        when(repository.findByCode("PEN")).thenReturn(Mono.error(new RuntimeException("Database error")));

        StepVerifier.create(adapter.findByCode("PEN"))
                .expectErrorMatches(throwable -> 
                    throwable instanceof RuntimeException && 
                    throwable.getMessage().equals("Database error"))
                .verify();

        verify(repository, times(1)).findByCode("PEN");
        verifyNoInteractions(mapper);
    }

    @Test
    void findByCode_mapperError_propagatesError() {
        var entity = buildStatesEntity(1L, "PEN", "Pending");

        when(repository.findByCode("PEN")).thenReturn(Mono.just(entity));
        when(mapper.map(entity, States.class)).thenThrow(new RuntimeException("Mapper error"));

        StepVerifier.create(adapter.findByCode("PEN"))
                .expectErrorMatches(throwable -> 
                    throwable instanceof RuntimeException && 
                    throwable.getMessage().equals("Mapper error"))
                .verify();

        verify(repository, times(1)).findByCode("PEN");
        verify(mapper, times(1)).map(entity, States.class);
    }

    @Test
    void findByCode_codeNormalization_worksCorrectly() {
        var entity = buildStatesEntity(1L, "PEN", "Pending");
        var domain = buildStates(1L, "PEN", "Pending");

        when(repository.findByCode("PEN")).thenReturn(Mono.just(entity));
        when(mapper.map(entity, States.class)).thenReturn(domain);

        // Test con código en mayúsculas
        StepVerifier.create(adapter.findByCode("PEN"))
                .expectNextMatches(result -> result.getCode().equals("PEN"))
                .verifyComplete();

        verify(repository, times(1)).findByCode("PEN");
        verify(mapper, times(1)).map(entity, States.class);
    }
}
