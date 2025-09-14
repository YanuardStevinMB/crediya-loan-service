package com.crediya.loan.usecase.requeststatuschange.requeststatus;

import com.crediya.loan.model.application.RequestStatusUpdate;
import com.crediya.loan.model.application.gateways.ApplicationRepository;
import com.crediya.loan.model.states.gateways.StatesRepository;
import com.crediya.loan.usecase.shared.ConfigurationException;
import com.crediya.loan.usecase.shared.DataValidation;
import com.crediya.loan.usecase.shared.Messages;
import reactor.core.publisher.Mono;

import java.util.logging.Logger;

public class ValidateRequestStatusUseCase {

    private static final Logger LOG = Logger.getLogger(ValidateRequestStatusUseCase.class.getName());

    private final ApplicationRepository applicationRepository;
    private final StatesRepository statesRepository;

    public ValidateRequestStatusUseCase(ApplicationRepository applicationRepository,
                                        StatesRepository statesRepository) {
        this.applicationRepository = applicationRepository;
        this.statesRepository = statesRepository;
    }

    /**
     * Valida:
     *  - Que la solicitud exista (por id)
     *  - Que el estado exista y sea permitido (APROBADO o RECHAZADO)
     * Retorna true si todo es válido.
     */
    public Mono<Boolean> execute(RequestStatusUpdate request) {
        if (request == null || request.getId() == null) {
            return Mono.error(new ConfigurationException(Messages.INVALID_IDENTIFICADOR_APPLICATION));
        }
        if (request.getStateId() == null) {
            return Mono.error(new ConfigurationException(Messages.INVALID_STATE));
        }

        // 1) Existe la solicitud
        Mono<Void> applicationOk = applicationRepository.findById(request.getId())
                .switchIfEmpty(Mono.error(new ConfigurationException(Messages.INVALID_IDENTIFICADOR_APPLICATION)))
                .doOnNext(a -> LOG.fine(() -> "[ValidateRequestStatus] Existe solicitud id=" + request.getId()))
                .then();

        // 2) Estado válido (existe + código permitido)
        Mono<Void> stateOk = statesRepository.findById(request.getStateId())
                .switchIfEmpty(Mono.error(new ConfigurationException(Messages.INVALID_STATE)))
                .flatMap(state -> {
                    String code = state.getCode();
                    boolean allowed = DataValidation.APROB_STATUS_CODE.equals(code)
                            || DataValidation.RECH_STATUS_CODE.equals(code);
                    return allowed
                            ? Mono.<Void>empty()
                            : Mono.error(new ConfigurationException(Messages.INVALID_STATE));
                })
                .doOnSuccess(v -> LOG.fine(() -> "[ValidateRequestStatus] Estado permitido id=" + request.getStateId()));

        return Mono.when(applicationOk, stateOk)
                .thenReturn(true);
    }
}
