package com.crediya.loan.r2dbc.lonType;

import com.crediya.loan.model.loantype.LoanType;
import com.crediya.loan.r2dbc.entity.LoanTypeEntity;
import com.crediya.loan.r2dbc.loantype.LoanTypeReactiveRepositoryAdapter; // adapter en 'loantype'
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

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanTypeReactiveRepositoryAdapterTest {

    @Mock
    LoanTypeReactiveRepository repository; // repo en 'lonType' (como en tu proyecto)

    @Mock
    ObjectMapper mapper;

    @InjectMocks
    LoanTypeReactiveRepositoryAdapter adapter;

    @BeforeEach
    void init() {
        adapter = new LoanTypeReactiveRepositoryAdapter(repository, mapper);
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

    // ---------- findById -----------

    @Test
    void findById_notFound_returnsEmpty() {
        when(repository.findById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(adapter.findById(1L))
                .verifyComplete();

        verify(repository, times(1)).findById(1L);
        verifyNoInteractions(mapper);
    }

    @Test
    void findById_found_mapsAndReturns() {
        var entity = buildLoanTypeEntity(1L, "Personal Loan");
        var domain = buildLoanType(1L, "Personal Loan");

        when(repository.findById(1L)).thenReturn(Mono.just(entity));
        when(mapper.map(entity, LoanType.class)).thenReturn(domain);

        StepVerifier.create(adapter.findById(1L))
                .expectNextMatches(result ->
                        result.getId().equals(1L) &&
                                result.getName().equals("Personal Loan"))
                .verifyComplete();

        verify(repository, times(1)).findById(1L);
        verify(mapper, times(1)).map(entity, LoanType.class);
    }

    @Test
    void findById_repositoryError_propagatesError() {
        when(repository.findById(1L)).thenReturn(Mono.error(new RuntimeException("Database error")));

        StepVerifier.create(adapter.findById(1L))
                .expectErrorMatches(throwable ->
                        throwable instanceof RuntimeException &&
                                throwable.getMessage().equals("Database error"))
                .verify();

        verify(repository, times(1)).findById(1L);
        verifyNoInteractions(mapper);
    }

    @Test
    void findById_mapperError_propagatesError() {
        var entity = buildLoanTypeEntity(1L, "Personal Loan");

        when(repository.findById(1L)).thenReturn(Mono.just(entity));
        when(mapper.map(entity, LoanType.class)).thenThrow(new RuntimeException("Mapper error"));

        StepVerifier.create(adapter.findById(1L))
                .expectErrorMatches(throwable ->
                        throwable instanceof RuntimeException &&
                                throwable.getMessage().equals("Mapper error"))
                .verify();

        verify(repository, times(1)).findById(1L);
        verify(mapper, times(1)).map(entity, LoanType.class);
    }
}
