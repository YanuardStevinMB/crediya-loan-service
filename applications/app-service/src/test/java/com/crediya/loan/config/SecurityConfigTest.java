package com.crediya.loan.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@WebFluxTest
@Import({SecurityConfigTest.TestRoutes.class, SecurityConfig.class})
class SecurityConfigTest {

    @Autowired
    WebTestClient client;

    @Autowired
    SecurityWebFilterChain chain;

    @Test
    void beanSecurityChain_existe() {
        assertNotNull(chain);
    }

    @Test
    void get_permitAll() {
        client.get()
                .uri("/ping")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_PLAIN)
                .expectBody(String.class).isEqualTo("pong");
    }

    @Test
    void post_sinCsrf_token_ok() {
        client.post()
                .uri("/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"ok\":true}")
                .exchange()
                .expectStatus().isOk();
    }

    // Rutas mínimas para probar el filtro de seguridad
    @Configuration
    static class TestRoutes {
        @Bean
        RouterFunction<ServerResponse> routes() {
            return route(GET("/ping"),
                    req -> ServerResponse.ok()
                            .contentType(MediaType.TEXT_PLAIN)
                            .bodyValue("pong"))
                    .andRoute(POST("/submit"),
                            req -> ServerResponse.ok().build());
        }
    }
}