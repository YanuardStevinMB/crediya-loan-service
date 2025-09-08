package com.crediya.loan.usecase.requeststatuschange;

import com.crediya.loan.model.application.RequestStatusUpdate;
import com.crediya.loan.model.application.gateways.ApplicationRepository;
import com.crediya.loan.model.states.gateways.StatesRepository;
import com.crediya.loan.usecase.shared.ConfigurationException;
import com.crediya.loan.usecase.shared.DataValidation;
import com.crediya.loan.usecase.shared.Messages;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import java.util.logging.Logger;


@RequiredArgsConstructor
public class RequestStatusChangeUseCase {
    private static final Logger LOG = Logger.getLogger(RequestStatusChangeUseCase.class.getName());
    private final ApplicationRepository applicationRepository;
    private final StatesRepository statesRepository;

    public Mono<String> execute(RequestStatusUpdate requestStatusUpdate) {
        LOG.fine("RequestStatusChangeUseCase.execute() - inicio");
        return validatedState(requestStatusUpdate.getStateId())
                .flatMap(valid -> applicationRepository.requestStatusChange(requestStatusUpdate))
                .map(documento -> {
                    LOG.fine(() ->
                            "RequestStatusChangeUseCase.execute() - éxito. Solicitud "
                                    + requestStatusUpdate.getId() + " cambiada a estado "
                                    + requestStatusUpdate.getStateId() + " (documento=" + documento + ")"
                    );
                    return Messages.APPLICATION_UPDATED;
                })
                .doOnError(e -> LOG.warning(() ->
                        "Error en RequestStatusChangeUseCase.execute(): " + e.getMessage()
                ));
    }

    private Mono<Boolean> validatedState(Long stateId) {
        return statesRepository.findById(stateId)
                .switchIfEmpty(Mono.error(
                        new ConfigurationException(Messages.INVALID_STATE)
                ))
                .flatMap(state -> {
                    if (DataValidation.APROB_STATUS_CODE.equals(state.getCode())
                            || DataValidation.RECH_STATUS_CODE.equals(state.getCode())) {
                        return Mono.just(true);
                    }else {
                        return Mono.error(new ConfigurationException(
                                Messages.INVALID_STATE)
                        );
                    }
                });
    }
}
