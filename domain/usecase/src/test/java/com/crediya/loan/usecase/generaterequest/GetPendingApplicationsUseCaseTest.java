package com.crediya.loan.usecase.generaterequest;

import com.crediya.loan.model.application.ApplicationPagined;
import com.crediya.loan.model.application.PendingApplicationsCriteria;
import com.crediya.loan.model.application.gateways.ApplicationRepository;
import com.crediya.loan.model.shared.Page;
import com.crediya.loan.model.user.User;
import com.crediya.loan.usecase.generaterequest.gateway.UserManagementGateway;
import com.crediya.loan.usecase.getpendingapplications.GetPendingApplicationsUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;

class GetPendingApplicationsUseCaseTest {

    private ApplicationRepository repo;
    private UserManagementGateway gateway;
    private GetPendingApplicationsUseCase useCase;

    @BeforeEach
    void setUp() {
        repo = Mockito.mock(ApplicationRepository.class);
        gateway = Mockito.mock(UserManagementGateway.class);
        useCase = new GetPendingApplicationsUseCase(repo, gateway);
    }

    @Test
    void shouldEnrichApplicationsWithUserData() {
        // Arrange: usuario mock
        User user = new User();
        user.setIdentityDocument("123");
        user.setFirstName("Juan");
        user.setLastName("Perez");
        user.setBaseSalary(new BigDecimal("1500"));

        Mockito.when(gateway.loadUsers()).thenReturn(Flux.just(user));

        // App paginada con identityDocument = "123"
        ApplicationPagined app = ApplicationPagined.builder()
                .id(1L)
                .amount(new BigDecimal("5000"))
                .term(LocalDate.now())
                .email("test@example.com")
                .identityDocument("123")
                .state("Pendiente")
                .loan("Consumo")
                .build();

        Page<ApplicationPagined> page = Page.of(List.of(app), 1, 10, 1);

        Mockito.when(repo.findApplicationsPaginated(any()))
                .thenReturn(Mono.just(page));

        // Act
        Mono<Page<ApplicationPagined>> result = useCase.execute(new PendingApplicationsCriteria(null, null, null, 1, 10));

        // Assert
        StepVerifier.create(result)
                .assertNext(p -> {
                    ApplicationPagined enriched = p.content().get(0);
                    // validamos que se llenaron los datos
                    assert enriched.getFullName().equals("Juan Perez");
                    assert enriched.getBaseSalary().equals(new BigDecimal("1500"));
                })
                .verifyComplete();
    }

    @Test
    void shouldNotEnrichWhenUserNotFound() {
        // Arrange: lista de usuarios vacía
        Mockito.when(gateway.loadUsers()).thenReturn(Flux.empty());

        ApplicationPagined app = ApplicationPagined.builder()
                .id(2L)
                .identityDocument("999")
                .build();

        Page<ApplicationPagined> page = Page.of(List.of(app), 1, 10, 1);

        Mockito.when(repo.findApplicationsPaginated(any()))
                .thenReturn(Mono.just(page));

        // Act
        Mono<Page<ApplicationPagined>> result = useCase.execute(new PendingApplicationsCriteria(null, null, null, 1, 10));

        // Assert
        StepVerifier.create(result)
                .assertNext(p -> {
                    ApplicationPagined enriched = p.content().get(0);
                    // sigue nulo porque no hubo match
                    assert enriched.getFullName() == null;
                    assert enriched.getBaseSalary() == null;
                })
                .verifyComplete();
    }
}
