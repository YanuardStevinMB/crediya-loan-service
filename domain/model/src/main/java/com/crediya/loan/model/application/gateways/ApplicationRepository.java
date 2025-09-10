package com.crediya.loan.model.application.gateways;

import com.crediya.loan.model.application.*;
import com.crediya.loan.model.shared.Page;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ApplicationRepository {

    Mono<Application> save(Application application);
    Mono<Page<ApplicationPagined>> findApplicationsPaginated(PendingApplicationsCriteria criteria);
    Mono<ApplicationDataCompleted> requestStatusChange(RequestStatusUpdate requestStatusUpdate);
    Mono<Application> findById(Long id);
    Flux<ApplicationApproved> approvedApplications(String identityDocument);
}
