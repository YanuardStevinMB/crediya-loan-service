package com.crediya.loan.usecase.getpendingapplications;

import com.crediya.loan.model.application.Application;
import com.crediya.loan.model.application.ApplicationPagined;
import com.crediya.loan.model.application.PendingApplicationsCriteria;
import com.crediya.loan.model.application.gateways.ApplicationRepository;
import com.crediya.loan.model.shared.Page;
import com.crediya.loan.model.user.User;
import com.crediya.loan.usecase.generaterequest.gateway.UserManagementGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@RequiredArgsConstructor
public class GetPendingApplicationsUseCase {

    private static final Logger LOG = Logger.getLogger(GetPendingApplicationsUseCase.class.getName());
    private final ApplicationRepository repo;
    private final UserManagementGateway gateway;
    private final List<User> usersCache = new ArrayList<>();

    public Mono<Page<ApplicationPagined>> execute(PendingApplicationsCriteria criteria) {
        return loadUsersAsList()
                .then(repo.findApplicationsPaginated(criteria))
                .map(page -> {
                    page.content().forEach(this::enrichWithUserData);
                    return page;
                });
    }

    /** Carga usuarios y los guarda en la lista temporal (siempre que se invoque el paginador). */
    private Mono<List<User>> loadUsersAsList() {
        return gateway.loadUsers()
                .collectList()
                .doOnNext(list -> {
                    usersCache.clear();
                    usersCache.addAll(list);
                    LOG.info(() -> "[loadUsersAsList] Usuarios cargados: " + usersCache.size());
                });
    }

    /** Completa ApplicationPagined con datos de usuario (fullName, baseSalary). */
    private ApplicationPagined enrichWithUserData(ApplicationPagined app) {
        if (app.getIdentityDocument() == null) return app;

        usersCache.stream()
                .filter(u -> app.getIdentityDocument().trim()
                        .equals(u.getIdentityDocument() != null ? u.getIdentityDocument().trim() : null))
                .findFirst()
                .ifPresent(u -> {
                    String fullName = Optional.ofNullable(u.getFirstName()).orElse("") +
                            " " +
                            Optional.ofNullable(u.getLastName()).orElse("");
                    app.setFullName(fullName.trim());
                    app.setBaseSalary(u.getBaseSalary());
                });
        return app;
    }
}
