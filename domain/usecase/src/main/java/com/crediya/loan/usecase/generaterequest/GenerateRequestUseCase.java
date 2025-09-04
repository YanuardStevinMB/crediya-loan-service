package com.crediya.loan.usecase.generaterequest;

import com.crediya.loan.model.application.Application;
import com.crediya.loan.model.application.gateways.ApplicationRepository;
import com.crediya.loan.model.loantype.gateways.LoanTypeRepository;
import com.crediya.loan.model.states.gateways.StatesRepository;
import com.crediya.loan.usecase.generaterequest.generaterequest.LoanTypeValidator;
import com.crediya.loan.usecase.generaterequest.generaterequest.VerifyUserUseCase;
import com.crediya.loan.usecase.generaterequest.shared.ConfigurationException;
import com.crediya.loan.usecase.generaterequest.shared.Messages;
import com.crediya.loan.usecase.generaterequest.ApplicationValidator;
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

    public Mono<Application> execute(Application app) {
        return Mono.defer(() -> {
            ApplicationValidator.validateAndNormalize(app); // Validación in-memory
            LOG.fine("GenerateRequestUseCase.execute() - inicio");

            return verifyUser(app)
                    .then(validateLoanType(app))
                    .then(assignInitialStateAndSave(app))
                    .doOnError(e -> LOG.warning(() -> "Error en generate request: " + e.getMessage()))
                    .doOnSuccess(ok -> LOG.fine("GenerateRequestUseCase.execute() - éxito"));
        });
    }

    // ---------------- MÉTODOS PRIVADOS ----------------

    private Mono<Boolean> verifyUser(Application app) {
        return verifyUserUseCase.execute(app.getIdentityDocument(), app.getEmail())
                .doOnNext(valid -> LOG.fine("Usuario verificado para documento=" + app.getIdentityDocument()));
    }

    private Mono<Application> validateLoanType(Application app) {
        return loanTypeRepository.findById(app.getLoanTypeId())
                .switchIfEmpty(Mono.error(
                        new ConfigurationException(Messages.stateNotFound(Messages.LOAN_TYPE_NO_EXIST))
                ))
                .flatMap(loanType -> LoanTypeValidator.validateAmount(app, loanType))
                .doOnSuccess(ok -> LOG.fine("Tipo de préstamo validado: " + app.getLoanTypeId()));
    }

    private Mono<Application> assignInitialStateAndSave(Application app) {
        return statesRepository.findByCode(DEFAULT_STATE_CODE)
                .switchIfEmpty(Mono.error(
                        new ConfigurationException(Messages.stateNotFound(DEFAULT_STATE_CODE))
                ))
                .flatMap(state -> {
                    app.setStateId(state.getId());
                    LOG.fine(() -> "Estado inicial asignado: " + state.getCode());
                    return applicationRepository.save(app)
                            .doOnSuccess(saved -> LOG.info(() ->
                                    "Solicitud creada id=" + saved.getId()
                                            + ", state=" + state.getCode()
                            ));
                });
    }
}
