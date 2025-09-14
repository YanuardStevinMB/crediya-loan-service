package com.crediya.loan.usecase.calculateborrowingcapacity;

import com.crediya.loan.model.application.Application;
import com.crediya.loan.model.application.gateways.ApplicationRepository;
import com.crediya.loan.model.application.gateways.BorrowingCapacitySender;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.logging.Logger;

@RequiredArgsConstructor
public class CalculateBorrowingCapacityUseCase {

    private static final Logger LOG = Logger.getLogger(CalculateBorrowingCapacityUseCase.class.getName());

    private final ApplicationRepository applicationRepository;
    private final BorrowingCapacitySender borrowingCapacitySender;

    public Mono<Application> execute(Application app, BigDecimal baseSalary) {
        LOG.info(() -> "⚡ Ejecutando cálculo de capacidad de endeudamiento para solicitud id="  + app.getId() + " salario=" + baseSalary);

        return applicationRepository.approvedApplications(app.getIdentityDocument())
            .collectList()
            .flatMap(approvedList -> {
                if (approvedList.isEmpty()) {
                    LOG.warning("⚠ No se encontraron préstamos aprobados para identity=" + app.getIdentityDocument());
                }

                return borrowingCapacitySender.sendBorrowingCapacity(app, baseSalary, approvedList, Collections.emptyList())
                    .doOnNext(msgId -> LOG.info("[SQS] ✅ Mensaje enviado con ID=" + msgId))
                    .thenReturn(app);
            });
    }

}
