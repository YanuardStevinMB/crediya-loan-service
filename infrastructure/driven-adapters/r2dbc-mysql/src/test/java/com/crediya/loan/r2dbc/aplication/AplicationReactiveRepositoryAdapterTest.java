package com.crediya.loan.r2dbc.aplication;

import com.crediya.loan.model.application.Application;
import com.crediya.loan.r2dbc.entity.ApplicationEntity;
import com.crediya.loan.r2dbc.mapper.AplicationEntityMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivecommons.utils.ObjectMapper;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AplicationReactiveRepositoryAdapterTest {

    @Mock
    AplicationReactiveRepository repository;

    @Mock
    AplicationEntityMapper aplicationEntityMapper;

    @Mock
    ObjectMapper mapper;

    @InjectMocks
    AplicationReactiveRepositoryAdapter adapter;

    @BeforeEach
    void init() {
        adapter = new AplicationReactiveRepositoryAdapter(repository, aplicationEntityMapper, mapper);
    }

    private Application buildApplication(String email, BigDecimal amount) {
        return Application.builder()
                .email(email)
                .amount(amount)
                .term(LocalDate.now().plusMonths(6))
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
                .term(LocalDate.now().plusMonths(6))
                .identityDocument("12345678")
                .loanTypeId(1L)
                .stateId(100L)
                .build();
    }

    // ---------- save -----------

    @Test
    void save_nullApplication_throwsException() {
        // El mapper puede manejar null, así que mockearemos el comportamiento
        when(aplicationEntityMapper.toEntity(null)).thenReturn(null);
        when(repository.save(null)).thenReturn(Mono.error(new IllegalArgumentException("Entity cannot be null")));

        StepVerifier.create(adapter.save(null))
                .expectError()
                .verify();

        verify(aplicationEntityMapper, times(1)).toEntity(null);
    }

    @Test
    void save_validApplication_mapsAndPersists_ok() {
        var application = buildApplication("test@example.com", BigDecimal.valueOf(5000));
        var entity = buildApplicationEntity(null, "test@example.com");
        var persistedEntity = buildApplicationEntity(1L, "test@example.com");
        var mappedDomain = buildApplication("test@example.com", BigDecimal.valueOf(5000));
        mappedDomain.setId(1L);

        when(aplicationEntityMapper.toEntity(application)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(Mono.just(persistedEntity));
        when(aplicationEntityMapper.toDomain(persistedEntity)).thenReturn(mappedDomain);

        StepVerifier.create(adapter.save(application))
                .expectNextMatches(saved -> 
                    saved.getId() != null && 
                    saved.getId().equals(1L) &&
                    saved.getEmail().equals("test@example.com"))
                .verifyComplete();

        verify(aplicationEntityMapper, times(1)).toEntity(application);
        verify(repository, times(1)).save(entity);
        verify(aplicationEntityMapper, times(1)).toDomain(persistedEntity);
    }

    @Test
    void save_repositoryError_propagatesError() {
        var application = buildApplication("test@example.com", BigDecimal.valueOf(5000));
        var entity = buildApplicationEntity(null, "test@example.com");

        when(aplicationEntityMapper.toEntity(application)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(Mono.error(new RuntimeException("Database error")));

        StepVerifier.create(adapter.save(application))
                .expectErrorMatches(throwable -> 
                    throwable instanceof RuntimeException && 
                    throwable.getMessage().equals("Database error"))
                .verify();

        verify(aplicationEntityMapper, times(1)).toEntity(application);
        verify(repository, times(1)).save(entity);
        verify(aplicationEntityMapper, never()).toDomain(any());
    }


}
