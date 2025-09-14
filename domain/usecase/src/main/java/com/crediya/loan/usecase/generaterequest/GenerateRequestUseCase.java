package com.crediya.loan.usecase.generaterequest;

import com.crediya.loan.model.application.Application;
import com.crediya.loan.model.application.gateways.ApplicationRepository;
import com.crediya.loan.model.loantype.gateways.LoanTypeRepository;
import com.crediya.loan.model.states.gateways.StatesRepository;
import com.crediya.loan.usecase.calculateborrowingcapacity.CalculateBorrowingCapacityUseCase;
import com.crediya.loan.usecase.generaterequest.generaterequest.ApplicationValidator;
import com.crediya.loan.usecase.generaterequest.generaterequest.LoanTypeValidator;
import com.crediya.loan.usecase.generaterequest.generaterequest.VerifyUserUseCase;
import com.crediya.loan.usecase.shared.ConfigurationException;
import com.crediya.loan.usecase.shared.DataValidation;
import com.crediya.loan.usecase.shared.Messages;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.logging.Logger;

@RequiredArgsConstructor
public class GenerateRequestUseCase {

    private static final Logger LOG = Logger.getLogger(GenerateRequestUseCase.class.getName());

    private final ApplicationRepository applicationRepository;
    private final StatesRepository statesRepository;
    private final LoanTypeRepository loanTypeRepository;
    private final VerifyUserUseCase verifyUserUseCase;
    private final CalculateBorrowingCapacityUseCase calculateBorrowingCapacityUseCase;

    public Mono<Application> execute(Application app) {
        return Mono.defer(() -> {
            ApplicationValidator.validateAndNormalize(app); // Validación in-memory
            LOG.fine("GenerateRequestUseCase.execute() - inicio");

            return verifyUser(app)
                    .flatMap(baseSalary ->
                            validateLoanType(app) // valida el LoanType
                                    .then(assignInitialStateAndSave(app, baseSalary)) // guarda y dispara flujo automático
                    )
                    .doOnError(e -> LOG.warning(() -> "Error en generate request: " + e.getMessage()))
                    .doOnSuccess(ok -> LOG.fine("GenerateRequestUseCase.execute() - éxito"));
        });
    }



    private Mono<BigDecimal> verifyUser(Application app) {
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

    private Mono<Application> assignInitialStateAndSave(Application app, BigDecimal baseSalary) {

        return statesRepository.findByCode(DataValidation.PENDING_STATUS_CODE)
                .switchIfEmpty(Mono.error(
                        new ConfigurationException(Messages.stateNotFound(DataValidation.PENDING_STATUS_CODE))
                ))
                .flatMap(state -> {
                    app.setStateId(state.getId());
                    LOG.fine(() -> "Estado inicial asignado: " + state.getCode());

                    return applicationRepository.save(app)
                            .flatMap(saved -> {
                                LOG.info(() -> "Solicitud creada id=" + saved.getId() + ", state=" + state.getCode());
                                return loanTypeRepository.findById(saved.getLoanTypeId())
                                        .flatMap(loanType -> {
                                            if (loanType.getAutomaticValidation()) {
                                                LOG.fine("LoanType  invocando cálculo automático");
                                                    return calculateBorrowingCapacityUseCase.execute(saved,baseSalary);
                                            }
                                            LOG.fine("LoanType con alto riesgo → no se dispara cálculo automático");
                                            return Mono.just(saved);
                                        });
                            });
                });
    }



}
