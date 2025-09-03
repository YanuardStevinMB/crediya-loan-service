package com.crediya.loan.usecase.getpendingapplications;

import com.crediya.loan.model.application.Application;
import com.crediya.loan.model.application.PendingApplicationsCriteria;
import com.crediya.loan.model.application.gateways.ApplicationRepository;
import com.crediya.loan.model.requestsandusers.RequestsAndUsers;
import com.crediya.loan.model.shared.Page;
import com.crediya.loan.model.user.User;
import com.crediya.loan.usecase.generaterequest.gateway.UserManagementGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@RequiredArgsConstructor
public class GetPendingApplicationsUseCase {

    private static final Logger LOG = Logger.getLogger(GetPendingApplicationsUseCase.class.getName());

    private final ApplicationRepository repo;
    private final UserManagementGateway gateway;

    private final List<User> cachedUsers = new ArrayList<>();

    public Mono<List<User>> loadAndCacheUsers() {
        return gateway.loadUsers()
                .collectList()
                .doOnNext(users -> {
                    cachedUsers.clear();
                    cachedUsers.addAll(users);
                    LOG.info(() -> "[loadAndCacheUsers] Usuarios cargados: " + users.size());
                });
    }

    public List<User> getCachedUsers() {
        return new ArrayList<>(cachedUsers);
    }

    public Mono<Page<RequestsAndUsers>> execute(PendingApplicationsCriteria c) {
        int p = Math.max(0, c.page());
        int s = c.size() <= 0 ? 20 : Math.min(c.size(), 100);
        String f = (c.filter() == null || c.filter().isBlank()) ? null : c.filter().trim();
        var normalized = new PendingApplicationsCriteria(p, s, f, c.stateId(),c.loanTypeId());

        return this.loadAndCacheUsers()
                .doOnNext(users -> LOG.info(() -> "[execute] Usuarios cargados desde gateway: " + users.size()))
                .flatMap(users -> {
                    LOG.info(() -> "[execute] Entrando a flatMap con " + users.size() + " usuarios en memoria");

                    return repo.findPending(normalized)
                            .doOnNext(page -> LOG.info(() ->
                                    "[execute] Página obtenida de repo con " + page.items().size() + " aplicaciones"))
                            .map(page -> {
                                List<RequestsAndUsers> enriched = (List<RequestsAndUsers>) page.items().stream()
                                        .map(app -> {
                                            User user = users.stream()
                                                    .filter(u -> u.getIdentityDocument().equals(app.getIdentityDocument()))
                                                    .findFirst()
                                                    .orElse(null);

                                            if (user == null) {
                                                LOG.warning(() -> "[execute] Usuario no encontrado para app " + app.getId());
                                            } else {
                                                LOG.fine(() -> "[execute] Enriqueciendo app " + app.getId()
                                                        + " con usuario " + user.getIdentityDocument());
                                            }

                                            return RequestsAndUsers.builder()
                                                    .id(app.getId())
                                                    .amount(app.getAmount())
                                                    .term(app.getTerm())
                                                    .email(app.getEmail())
                                                    .identityDocument(app.getIdentityDocument())
                                                    .stateId(app.getStateId())
                                                    .loanTypeId(app.getLoanTypeId())
                                                    .stateName(app.getStateName())
                                                    .typeLoan(app.getTypeLoan())
                                                    .fullName(user != null ? user.getFirstName() + " " + user.getLastName() : null)
                                                    .baseSalary(user != null ? user.getBaseSalary() : null)
                                                    .build();
                                        })
                                        .toList();

                                return new Page<>(
                                        enriched,
                                        page.total(),
                                        page.page(),
                                        page.size()
                                );
                            });
                });
    }
}