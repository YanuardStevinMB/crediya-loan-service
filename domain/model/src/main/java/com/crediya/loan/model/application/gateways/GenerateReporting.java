package com.crediya.loan.model.application.gateways;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface GenerateReporting {
    Mono<String> sendApproved(BigDecimal amount);
}
