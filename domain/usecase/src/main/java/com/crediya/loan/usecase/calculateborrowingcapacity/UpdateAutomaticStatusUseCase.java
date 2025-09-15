package com.crediya.loan.usecase.calculateborrowingcapacity;

import com.crediya.loan.model.application.RequestStatusUpdate;
import com.crediya.loan.model.application.gateways.ApplicationRepository;
import com.crediya.loan.model.application.gateways.BorrowingCapacitySender;
import com.crediya.loan.model.calculateborrowingcapacity.AnswersApplicationSqs;
import com.crediya.loan.model.states.gateways.StatesRepository;
import com.crediya.loan.usecase.applicationupdate.ApplicationUpdateUseCase;
import com.crediya.loan.usecase.shared.ConfigurationException;
import com.crediya.loan.usecase.shared.Messages;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.logging.Logger;

@RequiredArgsConstructor
public class UpdateAutomaticStatusUseCase {

    private static final Logger LOG = Logger.getLogger(UpdateAutomaticStatusUseCase.class.getName());

    private final StatesRepository statesRepository;
    private final ApplicationRepository applicationRepository;
    private final BorrowingCapacitySender answerSender;
    private final ApplicationUpdateUseCase applicationUpdateUseCase;



    public Mono<Void> execute(AnswersApplicationSqs msg) {
        if (msg.getId() == null || msg.getStatusCode() == null) {
            return Mono.error(new IllegalArgumentException(Messages.ID_REQUIRED_APPLICATION_AND_ID_REQUIRED_STATE));
        }
        LOG.info(() -> String.format("[UpdateAutomaticStatusUseCase] Procesando id=%s statusCode=%s", msg.getId(), msg.getStatusCode()));

        return statesRepository.findByCode(msg.getStatusCode())
                .switchIfEmpty(Mono.error(new ConfigurationException(Messages.stateNotFound(msg.getStatusCode()))))
                .flatMap(state -> {
                    RequestStatusUpdate update = RequestStatusUpdate.builder()
                            .id(msg.getId())
                            .stateId(state.getId())
                            .build();
                    LOG.info(() -> String.format("[UpdateAutomaticStatusUseCase] Actualizando estado id=%s → stateId=%s", msg.getId(), state.getId()));
                    return  applicationUpdateUseCase.execute(update)
                            .doOnSuccess(dto ->
                                    LOG.info(() -> String.format( "[UpdateAutomaticStatusUseCase] UPDATE OK id=%s stateId=%s",dto.getId(), dto.getStateId()))
                            )
                            .flatMap(dto -> publishIfNotFinal(msg));

                });
    }

    private Mono<Void> publishIfNotFinal(AnswersApplicationSqs msg) {
        LOG.info(() -> String.format("[UpdateAutomaticStatusUseCase] Publicando en SQS. id=%s", msg.getId()));
        return answerSender.sendRequestNotification(msg)
                .then()
                .doOnSuccess(v -> LOG.info(() -> String.format("[UpdateAutomaticStatusUseCase]  Publicado en SQS. id=%s",msg.getId())))
                .onErrorResume(ex -> {
                    LOG.warning(() -> String.format("[UpdateAutomaticStatusUseCase]  Error publicando en SQS. id=%s causa=%s",msg.getId(), ex.toString()));
                    return Mono.empty();
                });
    }
}
