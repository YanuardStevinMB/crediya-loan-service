package com.crediya.loan.api.controller;


import com.crediya.loan.api.applicationMapper.ApplicationMapper;
import com.crediya.loan.api.dto.ApiResponse;
import com.crediya.loan.api.dto.ApplicationResponseDto;
import com.crediya.loan.api.dto.ApplicationSaveDto;
import com.crediya.loan.usecase.generaterequest.GenerateRequestUseCase;
import com.crediya.loan.usecase.getpendingapplications.GetPendingApplicationsUseCase;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class ApplicationHandler {

    private final GenerateRequestUseCase generateRequestUseCase;
    private final GetPendingApplicationsUseCase getPendingApplicationsUseCase;
    private final ApplicationMapper mapper;
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
                .map(mapper::toModel)
                .flatMap(generateRequestUseCase::execute)
                .map(mapper::toResponseDto)
                .flatMap((ApplicationResponseDto dto) -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(ApiResponse.ok(dto, "Usuario creado correctamente", path))
                );

    }

    public Mono<ServerResponse> getPendingApplications(ServerRequest request) {
        final String path = request.path();

        int page = Integer.parseInt(request.queryParam("page").orElse("0"));
        int size = Integer.parseInt(request.queryParam("size").orElse("10"));
        String filter = request.queryParam("filter").orElse("");

        return getPendingApplicationsUseCase.execute(page, size, filter)
                .map(mapper::toResponseDto)
                .collectList()
                .flatMap(list -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(ApiResponse.ok(list, "Solicitudes pendientes", path))
                );
    }



}
