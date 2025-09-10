// usecase: com.crediya.loan.usecase.requeststatuschange.RequestStatusChangeUseCase
package com.crediya.loan.usecase.requeststatuschange;

import com.crediya.loan.model.application.ApplicationDataCompleted;
import com.crediya.loan.model.application.RequestStatusUpdate;
import com.crediya.loan.model.application.gateways.ApplicationRepository;
import com.crediya.loan.model.application.gateways.ApplicationSenderSqs;
import com.crediya.loan.usecase.requeststatuschange.requeststatus.ValidateRequestStatusUseCase;
import com.crediya.loan.usecase.shared.Messages;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.logging.Logger;

@RequiredArgsConstructor
public class RequestStatusChangeUseCase {

    private static final Logger LOG = Logger.getLogger(RequestStatusChangeUseCase.class.getName());

    private final ApplicationRepository applicationRepository;
    private final ValidateRequestStatusUseCase validateRequestStatusUseCase;
    private final ApplicationSenderSqs sqsSender;

    public Mono<String> execute(RequestStatusUpdate requestStatusUpdate) {
        LOG.fine("RequestStatusChangeUseCase.execute() - inicio");

        return validateRequestStatusUseCase.execute(requestStatusUpdate)
                .then(applicationRepository.requestStatusChange(requestStatusUpdate))
                .flatMap(entity ->
                        processEntity(entity, requestStatusUpdate.getId(), entity.getStateId())
                                .flatMap(ok -> ok
                                        ? Mono.just(Messages.APPLICATION_UPDATED)
                                        : Mono.error(new IllegalStateException(
                                        "[statusChange] Post-UPDATE: estado en BD != esperado")))
                )
                .doOnNext(msg -> LOG.fine(() ->
                        "Solicitud " + requestStatusUpdate.getId()
                                + " cambiada a estado " + requestStatusUpdate.getStateId()
                                + " (" + msg + ")"))
                .doOnError(e -> LOG.warning(() ->
                        "Error en RequestStatusChangeUseCase.execute(): " + e.getMessage()));
    }

    private Mono<Boolean> processEntity(
            ApplicationDataCompleted entity,
            Long id,
            Long newStateId
    ) {
        LOG.info("[statusChange] Solicitud cargada id=" + entity.getId()
                + ", stateId=" + entity.getStateId()
                + ", email=" + entity.getEmail()
                + ", doc=" + entity.getIdentityDocument());

        Long dbState = entity.getStateId();
        if (dbState == null || !dbState.equals(newStateId)) {
            LOG.warning("[statusChange] Post-UPDATE: estado en BD (" + dbState + ") != esperado (" + newStateId + "). id=" + id);
            return Mono.just(false);
        }

        // Delegar al adaptador: él construye el payload y lo envía
        return sqsSender.sendStatusChange(entity)
                .doOnNext(mid -> LOG.info("[statusChange] ✅ Enviado a SQS id=" + id
                        + ", stateId=" + newStateId + ", messageId=" + mid))
                .thenReturn(true)
                .onErrorResume(ex -> {
                    LOG.warning("[statusChange] Error publicando a SQS (continúo true). id=" + id + ", causa=" + ex);
                    return Mono.just(true);
                });
    }
}
