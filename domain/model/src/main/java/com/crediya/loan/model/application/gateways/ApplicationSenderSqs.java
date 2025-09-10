package com.crediya.loan.model.application.gateways;

import com.crediya.loan.model.application.ApplicationDataCompleted;
import reactor.core.publisher.Mono;

public interface ApplicationSenderSqs {
    Mono<String> sendStatusChange(ApplicationDataCompleted app);

}
