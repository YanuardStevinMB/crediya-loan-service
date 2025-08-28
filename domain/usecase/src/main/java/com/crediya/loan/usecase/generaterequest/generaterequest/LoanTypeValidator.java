package com.crediya.loan.usecase.generaterequest.generaterequest;

import com.crediya.loan.model.application.Application;
import com.crediya.loan.model.loantype.LoanType;
import com.crediya.loan.usecase.generaterequest.shared.Messages;
import com.crediya.loan.usecase.generaterequest.shared.ValidationException;
import reactor.core.publisher.Mono;



public class LoanTypeValidator {

    public static Mono<Application> validateAmount(Application a, LoanType loanType) {
        return Mono.just(a)
                .filter(app ->
                        app.getAmount() != null &&
                                app.getAmount().compareTo(loanType.getAmountMin()) >= 0 &&
                                app.getAmount().compareTo(loanType.getAmountMax()) <= 0
                )
                .switchIfEmpty(Mono.error(
                        new ValidationException(
                                "amount",
                                Messages.amountNotAllowed(loanType.getAmountMin(), loanType.getAmountMax())
                        )
                ));
    }
}