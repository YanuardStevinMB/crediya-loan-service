package com.crediya.loan.r2dbc.aplication;

import com.crediya.loan.model.application.Application;
import com.crediya.loan.model.application.ApplicationPagined;
import com.crediya.loan.model.application.PendingApplicationsCriteria;
import com.crediya.loan.model.shared.Page;
import com.crediya.loan.r2dbc.entity.ApplicationEntity;
import com.crediya.loan.r2dbc.mapper.AplicationEntityMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AplicationReactiveRepositoryAdapterTest {

    @Mock AplicationReactiveRepository repository;
    @Mock AplicationEntityMapper aplicationEntityMapper;
    @Mock ObjectMapper mapper;
    @Mock DatabaseClient db;

    // lo instanciamos explícito para usar el ctor con DatabaseClient
    AplicationReactiveRepositoryAdapter adapter;

    @BeforeEach
    void init() {
        adapter = new AplicationReactiveRepositoryAdapter(repository, aplicationEntityMapper, mapper, db);
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
                                "test@example.com".equals(saved.getEmail()))
                .verifyComplete();

        verify(aplicationEntityMapper, times(1)).toEntity(application);
        verify(repository, times(1)).save(entity);
        verify(aplicationEntityMapper, times(1)).toDomain(persistedEntity);
        verifyNoMoreInteractions(repository);
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
                                "Database error".equals(throwable.getMessage()))
                .verify();

        verify(aplicationEntityMapper, times(1)).toEntity(application);
        verify(repository, times(1)).save(entity);
        verify(aplicationEntityMapper, never()).toDomain(any());
        verifyNoMoreInteractions(repository);
    }

    // ---------- findApplicationsPaginated -----------



    @Test
    void findApplicationsPaginated_emptyResult_shouldReturnEmptyPage() {
        PendingApplicationsCriteria criteria = mock(PendingApplicationsCriteria.class);
        when(criteria.page()).thenReturn(1);
        when(criteria.size()).thenReturn(5);
        when(criteria.state()).thenReturn(null);
        when(criteria.document()).thenReturn(null);
        when(criteria.email()).thenReturn(null);

        when(repository.dataApplicationPagined(null, null, null, 5, 0))
                .thenReturn(Flux.empty());
        when(repository.countApplications(null, null, null))
                .thenReturn(Mono.just(0L));

        StepVerifier.create(adapter.findApplicationsPaginated(criteria))
                .assertNext(page -> {
                    assertEquals(1, page.page());
                    assertEquals(5, page.size());
                    assertEquals(0L, page.totalPages());
                    assertTrue(page.content().isEmpty());
                })
                .verifyComplete();

        verify(repository).dataApplicationPagined(null, null, null, 5, 0);
        verify(repository).countApplications(null, null, null);
    }

    @Test
    void findApplicationsPaginated_repoError_shouldPropagate() {
        PendingApplicationsCriteria criteria = mock(PendingApplicationsCriteria.class);
        when(criteria.page()).thenReturn(1);
        when(criteria.size()).thenReturn(10);
        when(criteria.state()).thenReturn("PEN");
        when(criteria.document()).thenReturn("123");
        when(criteria.email()).thenReturn("a@b.com");

        when(repository.dataApplicationPagined(any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(Flux.error(new RuntimeException("DB error")));

        // aunque countApplications devolviera algo, el zip fallará por el error del Flux
        when(repository.countApplications(any(), any(), any()))
                .thenReturn(Mono.just(10L));

        StepVerifier.create(adapter.findApplicationsPaginated(criteria))
                .expectErrorMatches(ex -> ex instanceof RuntimeException &&
                        ex.getMessage().equals("DB error"))
                .verify();

        verify(repository).dataApplicationPagined("PEN", "123", "a@b.com", 10, 0);
        verify(repository).countApplications("PEN", "123", "a@b.com");
    }
}
