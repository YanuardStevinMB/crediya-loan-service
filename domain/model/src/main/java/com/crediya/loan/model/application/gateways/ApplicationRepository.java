package com.crediya.loan.model.application.gateways;

import com.crediya.loan.model.application.Application;
import com.crediya.loan.model.application.ApplicationPagined;
import com.crediya.loan.model.application.PendingApplicationsCriteria;
import com.crediya.loan.model.requestsandusers.RequestsAndUsers;
import com.crediya.loan.model.shared.Page;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ApplicationRepository {

    Mono<Application> save(Application application);
    Mono<Page<ApplicationPagined>> findPending(PendingApplicationsCriteria criteria);

}
