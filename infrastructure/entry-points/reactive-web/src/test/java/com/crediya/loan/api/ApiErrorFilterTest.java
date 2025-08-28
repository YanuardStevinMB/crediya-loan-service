package com.crediya.loan.api;

import com.crediya.loan.usecase.generaterequest.shared.Messages;
import com.crediya.loan.usecase.generaterequest.shared.ValidationException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;               // 👈
import org.springframework.test.context.ContextConfiguration;         // 👈
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;

@WebFluxTest
@ContextConfiguration(classes = { ApiErrorFilter.class, ApiErrorFilterTest.TestRoutes.class })  // 👈 clave
class ApiErrorFilterTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void validationException_returns400() {
        webTestClient.post().uri("/ex/validation")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{}")
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.error").isEqualTo("Bad Request")
                .jsonPath("$.message").isNotEmpty()
                .jsonPath("$.path").isEqualTo("/ex/validation")
                .jsonPath("$.method").isEqualTo("POST");
    }

    @Test
    void constraintViolation_returns400() {
        webTestClient.post().uri("/ex/violation")
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.message").isEqualTo("Datos de entrada inválidos")
                .jsonPath("$.violations").exists();
    }

    @Test
    void illegalArgument_returns400() {
        webTestClient.post().uri("/ex/iae")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.message").isEqualTo("Bad arg");
    }

    @Test
    void genericException_returns500() {
        webTestClient.post().uri("/ex/other")
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody()
                .jsonPath("$.status").isEqualTo(500)
                .jsonPath("$.message").isEqualTo("Ocurrió un error inesperado");
    }

    /** Rutas de prueba que disparan cada excepción, con el filtro aplicado */
    @Configuration   // 👈 imprescindible para que @Bean sea detectado
    static class TestRoutes {
        @Bean
        RouterFunction<ServerResponse> testRouter(ApiErrorFilter filter) {
            return route(POST("/ex/validation"),
                    req -> Mono.error(new ValidationException("amount", Messages.AMOUNT_INVALID)))
                    .andRoute(POST("/ex/violation"),
                            req -> Mono.error(new ConstraintViolationException(
                                    java.util.Set.<ConstraintViolation<?>>of())))
                    .andRoute(POST("/ex/iae"),
                            req -> Mono.error(new IllegalArgumentException("Bad arg")))
                    .andRoute(POST("/ex/other"),
                            req -> Mono.error(new RuntimeException("boom")))
                    .filter(filter);
        }
    }
}
