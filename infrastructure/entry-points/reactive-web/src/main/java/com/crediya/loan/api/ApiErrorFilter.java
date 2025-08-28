package com.crediya.loan.api;

import com.crediya.loan.usecase.generaterequest.shared.ValidationException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerFilterFunction;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class ApiErrorFilter implements HandlerFilterFunction<ServerResponse, ServerResponse> {

    @Override
    public Mono<ServerResponse> filter(ServerRequest req, HandlerFunction<ServerResponse> next) {
        return next.handle(req)
                // --- específicas básicas ---
                .onErrorResume(ValidationException.class,
                        ex -> respond(req, HttpStatus.BAD_REQUEST, ex.getMessage(),
                                Map.of("field", ex.getField())))

                .onErrorResume(ConstraintViolationException.class,
                        ex -> respond(req, HttpStatus.BAD_REQUEST, "Datos de entrada inválidos",
                                Map.of("violations", violationsToList(ex))))

                .onErrorResume(IllegalArgumentException.class,
                        ex -> respond(req, HttpStatus.BAD_REQUEST, ex.getMessage(), null))

                // --- unwrap (reactor) + fallback ---
                .onErrorResume(t -> {
                    Throwable e = Exceptions.unwrap(t);
                    if (e instanceof ValidationException ve) {
                        return respond(req, HttpStatus.BAD_REQUEST, ve.getMessage(),
                                Map.of("field", ve.getField()));
                    } else if (e instanceof ConstraintViolationException ve) {
                        return respond(req, HttpStatus.BAD_REQUEST, "Datos de entrada inválidos",
                                Map.of("violations", violationsToList(ve)));
                    } else if (e instanceof IllegalArgumentException iae) {
                        return respond(req, HttpStatus.BAD_REQUEST, iae.getMessage(), null);
                    }
                    return respond(req, HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error inesperado", null);
                })
                .switchIfEmpty(ServerResponse.noContent().build());
    }

    // ---------- helpers mínimos (autosuficientes) ----------

    private Mono<ServerResponse> respond(ServerRequest req, HttpStatus status,
                                         String message, Map<String, ?> data) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", OffsetDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        body.put("path", req.path());
        body.put("method", req.methodName());
        if (data != null && !data.isEmpty()) {
            body.putAll(data);
        }
        return ServerResponse.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body);
    }

    private List<Map<String, Object>> violationsToList(ConstraintViolationException ex) {
        return ex.getConstraintViolations()
                .stream()
                .map(this::toMap)
                .collect(Collectors.toList());
    }

    private Map<String, Object> toMap(ConstraintViolation<?> v) {
        String field = v.getPropertyPath() != null ? v.getPropertyPath().toString() : null;
        Object rejected = v.getInvalidValue();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("field", field);
        m.put("message", v.getMessage());
        m.put("rejected", rejected);
        return m;
    }
}
