package com.crediya.loan.api.controller;


import com.crediya.loan.api.applicationMapper.ApplicationMapper;
import com.crediya.loan.api.applicationMapper.ApplicationPaginedMapper;
import com.crediya.loan.api.dto.ApiResponse;
import com.crediya.loan.api.dto.ApplicationResponseDto;
import com.crediya.loan.api.dto.ApplicationSaveDto;
import com.crediya.loan.api.dto.PagedResponseDto;
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
    private final ApplicationPaginedMapper applicationPaginedMapper;
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

    public Mono<ServerResponse> findApplications(ServerRequest request) {
        String estado    = request.queryParam("state").orElse(null);
        String documento = request.queryParam("document").orElse(null);
        String email     = request.queryParam("email").orElse(null);
        int page         = Integer.parseInt(request.queryParam("page").orElse("1"));
        int size         = Integer.parseInt(request.queryParam("size").orElse("10"));

        var criteria = new PendingApplicationsCriteria(estado, documento, email, page, size);

        return getPendingApplicationsUseCase.execute(criteria)
                .doOnSubscribe(sub -> log.info("[findApplications] Buscando aplicaciones con criteria={}", criteria))
                .doOnNext(p -> log.info("[findApplications] Resultados obtenidos: {} elementos (total={})",
                        p.content().size(), p.totalElements()))
                .map(p -> new PagedResponseDto<>(
                        p.page(),
                        p.size(),
                        p.totalElements(),
                        p.content().stream().map(applicationPaginedMapper::toResponseDto).toList()
                ))
                .flatMap(dto -> ServerResponse.ok().bodyValue(dto))
                .onErrorResume(ex -> {
                    log.error("[findApplications] Error al obtener aplicaciones", ex);
                    return ServerResponse.status(500).bodyValue("Error interno al obtener aplicaciones");
                });
    }




}
