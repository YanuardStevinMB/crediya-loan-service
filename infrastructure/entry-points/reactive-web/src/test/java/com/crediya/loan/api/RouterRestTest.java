package com.crediya.loan.api;

import com.crediya.loan.api.controller.ApplicationHandler;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;   // ✅
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration; // ✅
import org.springframework.test.context.bean.override.mockito.MockitoBean; // ✅
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;

@WebFluxTest
@ContextConfiguration(classes = { RouterRest.class, RouterRestTest.TestConfig.class }) // ✅ carga explícita
class RouterRestTest {

    @Autowired
    private WebTestClient webTestClient;

    // ✅ reemplaza @MockBean (deprecado) por @MockitoBean
    @MockitoBean
    private ApplicationHandler handler;

    @Test
    void postSolicitud_routesToHandler_andReturnsOkJson() {
        Mockito.when(handler.createApplication(any(ServerRequest.class)))
                .thenReturn(ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(Map.of("ok", true)));

        webTestClient.post()
                .uri("/api/v1/solicitud")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"dummy\":\"payload\"}")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.ok").isEqualTo(true);
    }

    /** Bean necesario para que el Router compile: .filter(errorFilter) */
    @Configuration // ✅ importante para que sea detectada como configuración
    static class TestConfig {
        @Bean
        ApiErrorFilter apiErrorFilter() {
            return new ApiErrorFilter(); // passthrough suficiente para este test
        }
    }
}
