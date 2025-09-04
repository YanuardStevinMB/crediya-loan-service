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

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Logger;

@RequiredArgsConstructor
public class GetPendingApplicationsUseCase {

    private static final Logger LOG = Logger.getLogger(GetPendingApplicationsUseCase.class.getName());

    private final ApplicationRepository repo;
    private final UserManagementGateway gateway;

//    public Mono<Page<ApplicationPagined>> execute(PendingApplicationsCriteria c) {
//
//        return loadUsersAsMap()
//                .flatMap(usersMap ->
//                        repo.findApplicationsPaginated()=fetchApplications(normalized)
//                        .map(page -> enrichPageWithUsers(page, usersMap)));
//    }

    // ---------------- MÉTODOS PRIVADOS ----------------

    public Mono<Page<ApplicationPagined>> execute(PendingApplicationsCriteria criteria) {
        return repo.findApplicationsPaginated(criteria);
    }

    private Mono<Map<String, User>> loadUsersAsMap() {
        return gateway.loadUsers()
                .collectMap(User::getIdentityDocument, Function.identity())
                .doOnNext(map -> LOG.info(() -> "[loadUsersAsMap] Usuarios cargados: " + map.size()));
    }


}
