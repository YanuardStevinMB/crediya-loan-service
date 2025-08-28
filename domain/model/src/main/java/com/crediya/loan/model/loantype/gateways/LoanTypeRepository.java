package com.crediya.loan.model.loantype.gateways;

import com.crediya.loan.model.loantype.LoanType;
import reactor.core.publisher.Mono;

public interface LoanTypeRepository {

    Mono<LoanType> findById(Long loanTypeId);
}
