package com.crediya.loan.api.controller;


import com.crediya.loan.api.applicationMapper.ApplicationMapper;
import com.crediya.loan.api.dto.ApiResponse;
import com.crediya.loan.api.dto.ApplicationResponseDto;
import com.crediya.loan.api.dto.ApplicationSaveDto;
import com.crediya.loan.api.dto.PagedResponseDto;
import com.crediya.loan.api.mapper.RequestsAndUsersMapper;
import com.crediya.loan.model.application.Application;
import com.crediya.loan.model.application.PendingApplicationsCriteria;
import com.crediya.loan.usecase.generaterequest.GenerateRequestUseCase;
import com.crediya.loan.usecase.getpendingapplications.GetPendingApplicationsUseCase;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static java.lang.Integer.parseInt;
@Slf4j
@Component
@RequiredArgsConstructor
public class ApplicationHandler {

    private final GenerateRequestUseCase generateRequestUseCase;
    private final GetPendingApplicationsUseCase getPendingApplicationsUseCase;
    private final ApplicationMapper applicationMapper ;
    private  final RequestsAndUsersMapper requestsAndUsersMapper;
    private final Validator validator;

    private <T> Mono<T> validate(T body) {
        var violations = validator.validate(body);
        if (!violations.isEmpty()) {
            return Mono.error(new ConstraintViolationException(violations));
        }
        return Mono.just(body);
    }

    public Mono<ServerResponse> createApplication(ServerRequest request) {
        final String path = request.path();


        return request.bodyToMono(ApplicationSaveDto.class)
                .flatMap(this::validate)
                .map(applicationMapper::toModel)
                .flatMap(generateRequestUseCase::execute)
                .map(applicationMapper::toResponseDto)
                .flatMap((ApplicationResponseDto dto) -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(ApiResponse.ok(dto, "Usuario creado correctamente", path))
                );

    }

    public Mono<ServerResponse> getPendingApplications(ServerRequest req) {
        int page  = parseIntOrDefault(req.queryParam("page").orElse(null), 0);
        int size  = parseIntOrDefault(req.queryParam("size").orElse(null), 10);

        String filter = req.queryParam("filter").orElse(null);
        Long loanTypeId = req.queryParam("loanTypeId").map(Long::valueOf).orElse(null);
        Long stateId = req.queryParam("state").map(Long::valueOf).orElse(null);

        var criteria = new PendingApplicationsCriteria(page, size, filter,stateId, loanTypeId);

        log.info("[USECASE] ListPendingApplications start criteria={}", criteria);

        return getPendingApplicationsUseCase.execute(criteria)
                .map(p -> {
                    var items = p.items().stream()
                            .map(requestsAndUsersMapper::toResponseDto) // Application -> ApplicationResponseDto
                            .toList();
                    return new PagedResponseDto<>(
                            p.page(),
                            p.size(),
                            p.total(),
                            items
                    );
                })
                .doOnNext(dto -> log.info("[USECASE] ListPendingApplications ok total={} items={} page={} size={}",
                        dto.getTotal_record_count(), dto.getRecords().size(), dto.getPage_number(), dto.getPage_size()))
                .flatMap(dto -> ServerResponse.ok()
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .bodyValue(dto))
                .onErrorResume(e -> {
                    log.error("[USECASE] ListPendingApplications error {}", e.toString(), e);
                    return ServerResponse.status(500)
                            .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                            .bodyValue("internal_error");
                });
    }

    private int parseIntOrDefault(String s, int def) {
        try { return (s == null || s.isBlank()) ? def : Integer.parseInt(s); }
        catch (NumberFormatException nfe) { return def; }
    }

}
