package com.crediya.loan.api.config;

import com.crediya.loan.api.ApiErrorFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@WebFluxTest
@ContextConfiguration(classes = { ConfigTest.TestRoutes.class })
@Import({ CorsConfig.class, SecurityHeadersConfig.class, ApiErrorFilter.class })
@TestPropertySource(properties = {
        // Para que el bean de CORS se construya bien en el slice test
        "cors.allowed-origins=http://localhost:4200"
})
class ConfigTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void corsConfigurationShouldAllowOrigins() {
        webTestClient.get()
                .uri("/__probe")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-Security-Policy",
                        "default-src 'self'; frame-ancestors 'self'; form-action 'self'")
                .expectHeader().valueEquals("Strict-Transport-Security", "max-age=31536000;")
                .expectHeader().valueEquals("X-Content-Type-Options", "nosniff")
                .expectHeader().valueEquals("Referrer-Policy", "strict-origin-when-cross-origin")
                .expectHeader().valueEquals("Cache-Control", "no-store")
                .expectHeader().valueEquals("Pragma", "no-cache")
                // IMPORTANTE: tu filtro pone "Server" = "" (header presente pero vacío)
                .expectHeader().valueEquals("Server", "");
    }

    @Configuration
    static class TestRoutes {
        @Bean
        RouterFunction<ServerResponse> probeRoute() {
            return route(GET("/__probe"), req -> ServerResponse.ok().bodyValue("OK"));
        }
    }
}
