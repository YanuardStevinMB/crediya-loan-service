package com.crediya.loan.usecase.getpendingapplications;

import com.crediya.loan.model.application.Application;
import com.crediya.loan.model.application.gateways.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor
public class GetPendingApplicationsUseCase {

    private final ApplicationRepository repository;

    public Flux<Application> execute(int page, int size, String filter) {
        return repository.findPendingApplications(page, size, filter);
    }
}