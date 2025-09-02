package com.crediya.loan.usecase.generaterequest;

import com.crediya.loan.model.application.Application;
import com.crediya.loan.model.application.gateways.ApplicationRepository;
import com.crediya.loan.model.loantype.gateways.LoanTypeRepository;
import com.crediya.loan.model.states.gateways.StatesRepository;
import com.crediya.loan.usecase.generaterequest.generaterequest.LoanTypeValidator;
import com.crediya.loan.usecase.generaterequest.generaterequest.VerifyUserUseCase;
import com.crediya.loan.usecase.generaterequest.shared.ConfigurationException;
import com.crediya.loan.usecase.generaterequest.shared.Messages;
import  com.crediya.loan.usecase.generaterequest.ApplicationValidator;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.logging.Logger;

@RequiredArgsConstructor
public class GenerateRequestUseCase {

    private static final Logger LOG = Logger.getLogger(GenerateRequestUseCase.class.getName());
    private static final String DEFAULT_STATE_CODE = "PEN"; // Pendiente de revisión

    private final ApplicationRepository applicationRepository;
    private final StatesRepository statesRepository;
    private final LoanTypeRepository loanTypeRepository;
    private final VerifyUserUseCase verifyUserUseCase;

    public Mono<Application> execute(Application a) {
        return Mono.defer(() -> {
            // Validación y normalización in-memory (sin I/O)
            ApplicationValidator.validateAndNormalize(a);

            LOG.fine("GenerateRequestUseCase.execute() - inicio");


            // 🔑 Primero valida el usuario y si pasa sigue el flujo
            return verifyUserUseCase.execute(a.getIdentityDocument(), a.getEmail())
                    .flatMap(valid ->
                            loanTypeRepository.findById(a.getLoanTypeId())
                                    .switchIfEmpty(Mono.error(
                                            new ConfigurationException(Messages.stateNotFound(Messages.LOAN_TYPE_NO_EXIST))
                                    ))
                                    .flatMap(loanType -> LoanTypeValidator.validateAmount(a, loanType))
                                    // 2) Traer estado inicial
                                    .flatMap(validApp ->
                                            statesRepository.findByCode(DEFAULT_STATE_CODE)
                                                    .switchIfEmpty(Mono.error(
                                                            new ConfigurationException(Messages.stateNotFound(DEFAULT_STATE_CODE))
                                                    ))
                                                    // 3) Asignar estado y guardar
                                                    .flatMap(state -> {
                                                        a.setStateId(state.getId());
                                                        LOG.fine(() -> "Estado inicial asignado: " + state.getCode());
                                                        return applicationRepository.save(a)
                                                                .doOnSuccess(saved -> LOG.info(() ->
                                                                        "Solicitud creada id=" + saved.getId()
                                                                                + ", state=" + state.getCode()
                                                                ));
                                                    })
                                    )
                    )
                    .doOnError(e -> LOG.warning(() ->
                            "GenerateRequestUseCase.execute() error: " + e.getMessage()))
                    .doOnSuccess(ok -> LOG.fine("GenerateRequestUseCase.execute() - éxito"));
        });
    }
}
