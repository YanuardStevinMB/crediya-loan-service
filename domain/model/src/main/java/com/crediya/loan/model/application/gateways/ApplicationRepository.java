package com.crediya.loan.model.application.gateways;

import com.crediya.loan.model.application.Application;
import reactor.core.publisher.Mono;

public interface ApplicationRepository {

Mono<Application> save(Application application);
}
