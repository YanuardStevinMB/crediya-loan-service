package com.crediya.loan.usecase.applicationupdate;

import com.crediya.loan.model.application.ApplicationDataCompleted;
import com.crediya.loan.model.application.RequestStatusUpdate;
import com.crediya.loan.model.application.gateways.ApplicationRepository;
import com.crediya.loan.model.application.gateways.GenerateReporting;
import com.crediya.loan.usecase.requeststatuschange.requeststatus.ValidateRequestStatusUseCase;
import com.crediya.loan.usecase.shared.DataValidation;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.logging.Logger;

@RequiredArgsConstructor
public class ApplicationUpdateUseCase {

    private static final Logger LOG = Logger.getLogger(ApplicationUpdateUseCase.class.getName());

    private final ApplicationRepository applicationRepository;
    private final ValidateRequestStatusUseCase validateRequestStatusUseCase;
    private final GenerateReporting generateReporting;

    public Mono<ApplicationDataCompleted> execute(RequestStatusUpdate request) {
        LOG.fine("ApplicationUpdateUseCase.execute() - inicio");

        return validateRequestStatusUseCase.execute(request)
                .then(applicationRepository.requestStatusChange(request))
                .flatMap(entity ->
                        processEntity(entity, request.getId(), request.getStateId())
                                .flatMap(ok -> ok

                                                ? Mono.just(entity)
                                                : Mono.error(new IllegalStateException(
                                                "[statusChange] Post-UPDATE: estado en BD != esperado"
                                        ))

                                )
                )
                .doOnNext(entity -> LOG.fine(() ->
                        "Solicitud " + request.getId() +" cambiada a estado " + request.getStateId()
                ))
                .doOnError(e -> LOG.warning(() ->
                        "Error en ApplicationUpdateUseCase.execute(): " + e.getMessage()
                ));
    }

    private Mono<Boolean> processEntity(ApplicationDataCompleted entity, Long id, Long newStateId) {
        LOG.info("[statusChange] Solicitud cargada id=" + entity.getId()+ ", stateId=" + entity.getStateId()+ ", email=" + entity.getEmail()+ ", doc=" + entity.getIdentityDocument());

        Long dbState = entity.getStateId();
        if (dbState == null || !dbState.equals(newStateId)) {
            LOG.warning("[statusChange] Post-UPDATE: estado en BD (" + dbState + ") != esperado (" + newStateId + "). id=" + id);
            return Mono.just(false);
        }

        if (DataValidation.APROB_STATUS.equals(entity.getState())) {
            LOG.info("[statusChange] Estado APROBADO detectado, generando reporte...");
            return generateReporting.sendApproved(entity.getAmount())
                    .thenReturn(true);
        }

        return Mono.just(true);
    }

}
