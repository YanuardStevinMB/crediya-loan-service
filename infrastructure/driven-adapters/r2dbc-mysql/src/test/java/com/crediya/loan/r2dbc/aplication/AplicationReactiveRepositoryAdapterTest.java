package com.crediya.loan.r2dbc.aplication;

import com.crediya.loan.model.application.*;
import com.crediya.loan.r2dbc.dto.ApplicationApprovedDto;
import com.crediya.loan.r2dbc.dto.ApplicationDto;
import com.crediya.loan.r2dbc.entity.ApplicationEntity;
import com.crediya.loan.r2dbc.mapper.AplicationEntityMapper;
import com.crediya.loan.r2dbc.mapper.ApplicationApprovedMapper;
import com.crediya.loan.r2dbc.mapper.ApplicationDataCompletedMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivecommons.utils.ObjectMapper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AplicationReactiveRepositoryAdapterTest {

    @Mock AplicationReactiveRepository repository;
    @Mock AplicationEntityMapper aplicationEntityMapper;
    @Mock ObjectMapper mapper;
    @Mock ApplicationDataCompletedMapper applicationDataCompletedMapper;
    @Mock ApplicationApprovedMapper applicationApprovedMapper;

    AplicationReactiveRepositoryAdapter adapter;

    @BeforeEach
    void init() {
        adapter = new AplicationReactiveRepositoryAdapter(
                repository,
                aplicationEntityMapper,
                mapper,
                applicationDataCompletedMapper,
                applicationApprovedMapper
        );
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

    private ApplicationDataCompleted buildCompleted(Long id, Long stateId) {
        return ApplicationDataCompleted.builder()
                .id(id)
                .amount(BigDecimal.valueOf(3000))
                .email("user@mail.com")
                .identityDocument("CC123")
                .loan("PERSONAL")
                .state("PENDING")
                .stateId(stateId)
                .loanTypeId(1L)
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
                .expectNextMatches(saved -> saved.getId() != null && saved.getEmail().equals("test@example.com"))
                .verifyComplete();

        verify(aplicationEntityMapper).toEntity(application);
        verify(repository).save(entity);
        verify(aplicationEntityMapper).toDomain(persistedEntity);
    }

    @Test
    void save_repositoryError_propagatesError() {
        var application = buildApplication("test@example.com", BigDecimal.valueOf(5000));
        var entity = buildApplicationEntity(null, "test@example.com");

        when(aplicationEntityMapper.toEntity(application)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(Mono.error(new RuntimeException("Database error")));

        StepVerifier.create(adapter.save(application))
                .expectErrorMatches(ex -> ex instanceof RuntimeException && ex.getMessage().equals("Database error"))
                .verify();

        verify(aplicationEntityMapper).toEntity(application);
        verify(repository).save(entity);
        verify(aplicationEntityMapper, never()).toDomain(any());
    }

    // ---------- findApplicationsPaginated -----------

    @Test
    void findApplicationsPaginated_emptyResult_shouldReturnEmptyPage() {
        PendingApplicationsCriteria criteria = new PendingApplicationsCriteria(null, null, null, 1, 5);

        when(repository.dataApplicationPagined(null, null, null, 5, 0)).thenReturn(Flux.empty());
        when(repository.countApplications(null, null, null)).thenReturn(Mono.just(0L));

        StepVerifier.create(adapter.findApplicationsPaginated(criteria))
                .assertNext(page -> {
                    assertEquals(1, page.page());
                    assertEquals(5, page.size());
                    assertEquals(0L, page.totalPages());
                    assertTrue(page.content().isEmpty());
                })
                .verifyComplete();
    }

    @Test
    void findApplicationsPaginated_repoError_shouldPropagate() {
        PendingApplicationsCriteria criteria = new PendingApplicationsCriteria("PEN", "123", "a@b.com", 1, 10);

        when(repository.dataApplicationPagined(any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(Flux.error(new RuntimeException("DB error")));
        when(repository.countApplications(any(), any(), any())).thenReturn(Mono.just(10L));

        StepVerifier.create(adapter.findApplicationsPaginated(criteria))
                .expectErrorMatches(ex -> ex instanceof RuntimeException && ex.getMessage().equals("DB error"))
                .verify();
    }

    // ---------- requestStatusChange -----------

    @Test
    void requestStatusChange_shouldReturnDomain_whenStateMatches() {
        var update = new RequestStatusUpdate(1L, 20L);
        var dto = new ApplicationDto();
        dto.setId(1L);
        dto.setStateId(20L);

        var completed = buildCompleted(1L, 20L);

        when(repository.requestStatusChange(1L, 20L)).thenReturn(Mono.just(1));
        when(repository.dataApplication(1L)).thenReturn(Mono.just(dto));
        when(applicationDataCompletedMapper.toDomain(dto)).thenReturn(completed);

        StepVerifier.create(adapter.requestStatusChange(update))
                .expectNext(completed)
                .verifyComplete();

        verify(repository).requestStatusChange(1L, 20L);
        verify(repository).dataApplication(1L);
        verify(applicationDataCompletedMapper).toDomain(dto);
    }

    @Test
    void requestStatusChange_shouldError_whenEntityNotFound() {
        var update = new RequestStatusUpdate(2L, 30L);

        when(repository.requestStatusChange(2L, 30L)).thenReturn(Mono.just(1));
        when(repository.dataApplication(2L)).thenReturn(Mono.empty());

        StepVerifier.create(adapter.requestStatusChange(update))
                .expectError(IllegalStateException.class)
                .verify();
    }

    @Test
    void requestStatusChange_shouldError_whenStateMismatch() {
        var update = new RequestStatusUpdate(3L, 40L);
        var dto = new ApplicationDto();
        dto.setId(3L);
        dto.setStateId(99L); // mismatch

        when(repository.requestStatusChange(3L, 40L)).thenReturn(Mono.just(1));
        when(repository.dataApplication(3L)).thenReturn(Mono.just(dto));

        StepVerifier.create(adapter.requestStatusChange(update))
                .expectErrorMatches(ex -> ex instanceof IllegalStateException &&
                        ex.getMessage().contains("Estado no coincide"))
                .verify();
    }

    // ---------- approvedApplications -----------

    @Test
    void approvedApplications_shouldMapResults() {
        var dto = new ApplicationApprovedDto();
        dto.setAmount(BigDecimal.valueOf(2000));
        dto.setInterestRate(BigDecimal.valueOf(5));
        dto.setTermMonths(12L);

        var domain = new ApplicationApproved(BigDecimal.valueOf(2000), BigDecimal.valueOf(5), 12L);

        when(repository.applicationApproved("CC123")).thenReturn(Flux.just(dto));
        when(applicationApprovedMapper.toDomain(dto)).thenReturn(domain);

        StepVerifier.create(adapter.approvedApplications("CC123"))
                .expectNext(domain)
                .verifyComplete();

        verify(repository).applicationApproved("CC123");
        verify(applicationApprovedMapper).toDomain(dto);
    }

    @Test
    void approvedApplications_shouldReturnEmptyFlux_whenNoResults() {
        when(repository.applicationApproved("CC999")).thenReturn(Flux.empty());

        StepVerifier.create(adapter.approvedApplications("CC999"))
                .verifyComplete();

        verify(repository).applicationApproved("CC999");
        verifyNoInteractions(applicationApprovedMapper);
    }
}
