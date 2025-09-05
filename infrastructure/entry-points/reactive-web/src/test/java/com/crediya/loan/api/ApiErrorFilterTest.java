package com.crediya.loan.api;

import com.crediya.loan.usecase.shared.ValidationException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApiErrorFilterTest {

    @Mock
    private HandlerFunction<ServerResponse> handlerFunction;

    @Mock
    private ServerRequest serverRequest;

    @Mock
    private ServerResponse serverResponse;

    private ApiErrorFilter apiErrorFilter;

    @BeforeEach
    void setUp() {
        apiErrorFilter = new ApiErrorFilter();
        when(serverRequest.path()).thenReturn("/test");
        when(serverRequest.methodName()).thenReturn("GET");
    }


    @Test
    void filter_WhenValidationException_ShouldReturnBadRequest400() {
        // Mockeamos la excepción para evitar depender del constructor real
        ValidationException ex = mock(ValidationException.class);
        when(ex.getMessage()).thenReturn("Campo inválido");
        when(ex.getField()).thenReturn("amount");

        when(handlerFunction.handle(serverRequest)).thenReturn(Mono.error(ex));

        Mono<ServerResponse> result = apiErrorFilter.filter(serverRequest, handlerFunction);

        StepVerifier.create(result)
                .assertNext(resp -> assertThat(resp.statusCode().value()).isEqualTo(HttpStatus.BAD_REQUEST.value()))
                .verifyComplete();
    }

    @Test
    void filter_WhenConstraintViolationException_ShouldReturnBadRequest400() {
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        when(path.toString()).thenReturn("email");
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn("must not be blank");
        when(violation.getInvalidValue()).thenReturn("");

        ConstraintViolationException ex = new ConstraintViolationException(Set.of(violation));

        when(handlerFunction.handle(serverRequest)).thenReturn(Mono.error(ex));

        Mono<ServerResponse> result = apiErrorFilter.filter(serverRequest, handlerFunction);

        StepVerifier.create(result)
                .assertNext(resp -> assertThat(resp.statusCode().value()).isEqualTo(HttpStatus.BAD_REQUEST.value()))
                .verifyComplete();
    }

    @Test
    void filter_WhenIllegalArgumentException_ShouldReturnBadRequest400() {
        when(handlerFunction.handle(serverRequest))
                .thenReturn(Mono.error(new IllegalArgumentException("Argumento inválido")));

        Mono<ServerResponse> result = apiErrorFilter.filter(serverRequest, handlerFunction);

        StepVerifier.create(result)
                .assertNext(resp -> assertThat(resp.statusCode().value()).isEqualTo(HttpStatus.BAD_REQUEST.value()))
                .verifyComplete();
    }

    @Test
    void filter_WhenUnknownException_ShouldReturnInternalServerError500() {
        when(handlerFunction.handle(serverRequest))
                .thenReturn(Mono.error(new RuntimeException("Error desconocido")));

        Mono<ServerResponse> result = apiErrorFilter.filter(serverRequest, handlerFunction);

        StepVerifier.create(result)
                .assertNext(resp -> assertThat(resp.statusCode().value()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value()))
                .verifyComplete();
    }

    // ------- casos con unwrap(…) de Reactor -------

    @Test
    void filter_WhenWrappedValidationException_ShouldReturnBadRequest400() {
        ValidationException inner = mock(ValidationException.class);
        when(inner.getMessage()).thenReturn("Campo inválido");
        when(inner.getField()).thenReturn("amount");

        RuntimeException wrapper = new RuntimeException(inner);
        when(handlerFunction.handle(serverRequest)).thenReturn(Mono.error(wrapper));

        try (MockedStatic<Exceptions> mocked = mockStatic(Exceptions.class)) {
            mocked.when(() -> Exceptions.unwrap(wrapper)).thenReturn(inner);

            Mono<ServerResponse> result = apiErrorFilter.filter(serverRequest, handlerFunction);

            StepVerifier.create(result)
                    .assertNext(resp -> assertThat(resp.statusCode().value()).isEqualTo(HttpStatus.BAD_REQUEST.value()))
                    .verifyComplete();
        }
    }

    @Test
    void filter_WhenWrappedConstraintViolationException_ShouldReturnBadRequest400() {
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        when(path.toString()).thenReturn("email");
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn("invalid");
        when(violation.getInvalidValue()).thenReturn("x");

        ConstraintViolationException inner = new ConstraintViolationException(Set.of(violation));
        RuntimeException wrapper = new RuntimeException(inner);

        when(handlerFunction.handle(serverRequest)).thenReturn(Mono.error(wrapper));

        try (MockedStatic<Exceptions> mocked = mockStatic(Exceptions.class)) {
            mocked.when(() -> Exceptions.unwrap(wrapper)).thenReturn(inner);

            Mono<ServerResponse> result = apiErrorFilter.filter(serverRequest, handlerFunction);

            StepVerifier.create(result)
                    .assertNext(resp -> assertThat(resp.statusCode().value()).isEqualTo(HttpStatus.BAD_REQUEST.value()))
                    .verifyComplete();
        }
    }

    @Test
    void filter_WhenWrappedIllegalArgumentException_ShouldReturnBadRequest400() {
        IllegalArgumentException inner = new IllegalArgumentException("Argumento inválido");
        RuntimeException wrapper = new RuntimeException(inner);

        when(handlerFunction.handle(serverRequest)).thenReturn(Mono.error(wrapper));

        try (MockedStatic<Exceptions> mocked = mockStatic(Exceptions.class)) {
            mocked.when(() -> Exceptions.unwrap(wrapper)).thenReturn(inner);

            Mono<ServerResponse> result = apiErrorFilter.filter(serverRequest, handlerFunction);

            StepVerifier.create(result)
                    .assertNext(resp -> assertThat(resp.statusCode().value()).isEqualTo(HttpStatus.BAD_REQUEST.value()))
                    .verifyComplete();
        }
    }

    @Test
    void filter_WhenWrappedUnknownException_ShouldReturnInternalServerError500() {
        NullPointerException inner = new NullPointerException("NPE");
        RuntimeException wrapper = new RuntimeException(inner);

        when(handlerFunction.handle(serverRequest)).thenReturn(Mono.error(wrapper));

        try (MockedStatic<Exceptions> mocked = mockStatic(Exceptions.class)) {
            mocked.when(() -> Exceptions.unwrap(wrapper)).thenReturn(inner);

            Mono<ServerResponse> result = apiErrorFilter.filter(serverRequest, handlerFunction);

            StepVerifier.create(result)
                    .assertNext(resp -> assertThat(resp.statusCode().value()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value()))
                    .verifyComplete();
        }
    }
}
