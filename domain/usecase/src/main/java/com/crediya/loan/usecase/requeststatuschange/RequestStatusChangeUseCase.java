package com.crediya.loan.usecase.requeststatuschange;

import com.crediya.loan.model.application.ApplicationDataCompleted;
import com.crediya.loan.model.application.RequestStatusUpdate;
import com.crediya.loan.model.application.gateways.ApplicationRepository;
import com.crediya.loan.model.application.gateways.ApplicationSenderSqs;
import com.crediya.loan.usecase.applicationupdate.ApplicationUpdateUseCase;
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
    private final ApplicationUpdateUseCase applicationUpdateUseCase;

    public Mono<String> execute(RequestStatusUpdate requestStatusUpdate) {
        LOG.fine("RequestStatusChangeUseCase.execute() - inicio");
        return applicationUpdateUseCase.execute(requestStatusUpdate)
                .flatMap(entity -> sendNotification(entity, requestStatusUpdate))
                .thenReturn(Messages.APPLICATION_UPDATED)
                .doOnNext(msg -> LOG.fine(() ->"Solicitud " + requestStatusUpdate.getId() +" cambiada a estado " + requestStatusUpdate.getStateId() +" (" + msg + ")"
                ))
                .doOnError(e -> LOG.warning(() ->"Error en RequestStatusChangeUseCase.execute(): " + e.getMessage()
                ));
    }

    /**
     * Envía la notificación a SQS.
     */
    private Mono<Void> sendNotification(ApplicationDataCompleted entity, RequestStatusUpdate request) {
        return sqsSender.sendStatusChange(entity)
                .doOnNext(mid -> LOG.info("[statusChange] Enviado a SQS id=" + request.getId() +", stateId=" + request.getStateId() + ", messageId=" + mid))
                .then()
                .onErrorResume(ex -> {
                    LOG.warning("[statusChange] Error publicando a SQS (continúo sin fallo). id=" +request.getId() + ", causa=" + ex);
                    return Mono.empty();
                });
    }
}
