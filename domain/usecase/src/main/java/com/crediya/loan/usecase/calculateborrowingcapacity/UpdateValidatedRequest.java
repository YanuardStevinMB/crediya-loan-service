package com.crediya.loan.usecase.calculateborrowingcapacity;

import com.crediya.loan.model.application.AnswersApplicationSqs;
import com.crediya.loan.model.application.RequestStatusUpdate;
import com.crediya.loan.model.states.gateways.StatesRepository;
import com.crediya.loan.usecase.requeststatuschange.RequestStatusChangeUseCase;
import com.crediya.loan.usecase.shared.ConfigurationException;
import com.crediya.loan.usecase.shared.Messages;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.logging.Logger;

@RequiredArgsConstructor
public class UpdateValidatedRequest {

    private static final Logger LOG = Logger.getLogger(UpdateValidatedRequest.class.getName());

    private final StatesRepository statesRepository;
    private final RequestStatusChangeUseCase requestStatusChangeUseCase;

    public Mono<String> execute(AnswersApplicationSqs answersApplicationSqs) {
        return statesRepository.findByCode(answersApplicationSqs.getCode())
                .switchIfEmpty(Mono.error(
                        new ConfigurationException(Messages.stateNotFound(answersApplicationSqs.getCode()))
                ))
                .flatMap(state -> {
                    RequestStatusUpdate update = RequestStatusUpdate.builder()
                            .id(answersApplicationSqs.getId())
                            .stateId(state.getId())
                            .build();

                    LOG.info(() -> "[UpdateValidatedRequest] Procesando solicitud id=" + update.getId()
                            + " → nuevo estado=" + state.getCode());

                    return requestStatusChangeUseCase.execute(update);
                });
    }
}
