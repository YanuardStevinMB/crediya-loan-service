package com.crediya.loan.model.application.gateways;

import com.crediya.loan.model.application.Application;
import com.crediya.loan.model.application.ApplicationApproved;
import com.crediya.loan.model.calculateborrowingcapacity.AnswersApplicationSqs;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Puerto de salida para enviar la data de capacidad de endeudamiento.
 */
public interface BorrowingCapacitySender {
    Mono<String> sendBorrowingCapacity(Application app,
                                       BigDecimal baseSalary,
                                       List<ApplicationApproved> approvedLoans,
                                       List<Map<String, Object>> activeLoans);

        Mono<String> sendRequestNotification(AnswersApplicationSqs msg);

}
