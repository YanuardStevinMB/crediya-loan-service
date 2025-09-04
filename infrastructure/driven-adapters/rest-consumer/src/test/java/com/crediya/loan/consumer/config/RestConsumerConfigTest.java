package com.crediya.loan.consumer.config;

import io.netty.handler.timeout.ReadTimeoutException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class RestConsumerConfigTest {

    private MockWebServer server;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    private RestConsumerConfig newConfig(String baseUrl, int timeoutMs) {
        return new RestConsumerConfig(baseUrl, timeoutMs);
    }

    @Test
    void getWebClient_shouldUseBaseUrl_andDefaultContentTypeHeader_onPOST() throws InterruptedException {
        // Arrange
        var baseUrl = server.url("/").toString();
        var cfg = newConfig(baseUrl, 1_000);
        WebClient client = cfg.getWebClient(WebClient.builder());

        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"ok\":true}")
                .addHeader("Content-Type", "application/json"));

        // Act
        StepVerifier.create(
                        client.post()
                                .uri("/echo")
                                .bodyValue("{\"x\":1}")
                                .retrieve()
                                .bodyToMono(String.class)
                )
                .expectNext("{\"ok\":true}")
                .verifyComplete();

        // Assert (baseUrl aplicado + header por defecto)
        RecordedRequest req = server.takeRequest(2, TimeUnit.SECONDS);
        assertNotNull(req);
        assertEquals("/echo", req.getPath());
        assertEquals("POST", req.getMethod());

        // El defaultHeader debe estar presente
        String contentType = req.getHeader("Content-Type");
        assertNotNull(contentType, "Content-Type debe estar presente por defaultHeader");
        assertTrue(contentType.startsWith(MediaType.APPLICATION_JSON_VALUE));
    }

    @Test
    void getWebClient_shouldApplyReadTimeout() {
        // Arrange: respuesta que tarda MÁS que el timeout configurado
        var baseUrl = server.url("/").toString();
        var timeoutMs = 100; // corto para forzar timeout
        var cfg = newConfig(baseUrl, timeoutMs);
        WebClient client = cfg.getWebClient(WebClient.builder());

        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("slow-ok")
                .setBodyDelay(500, TimeUnit.MILLISECONDS)  // cuerpo se retrasa 500ms
                .addHeader("Content-Type", "text/plain"));

        // Act + Assert: debe fallar por timeout de lectura
        StepVerifier.create(
                        client.post()
                                .uri("/slow")
                                .bodyValue("x")
                                .retrieve()
                                .bodyToMono(String.class)
                                .timeout(Duration.ofSeconds(5)) // evita que el test se quede colgado si algo cambia
                )
                .expectErrorSatisfies(err -> {
                    // Dependiendo de la versión de Reactor/Netty, puede venir envuelto.
                    // Aceptamos varias rutas de error, pero verificamos que la causa final sea ReadTimeoutException
                    assertTrue(hasCause(err, ReadTimeoutException.class),
                            "Se esperaba ReadTimeoutException en la cadena de causas, pero fue: " + err);
                })
                .verify();
    }

    @Test
    void classHasExpectedAnnotations() {
        assertTrue(RestConsumerConfig.class.isAnnotationPresent(Configuration.class),
                "Debe estar anotada con @Configuration");

        ConfigurationProperties cp = RestConsumerConfig.class.getAnnotation(ConfigurationProperties.class);
        assertNotNull(cp, "Debe tener @ConfigurationProperties");
        assertEquals("adapter.restconsumer", cp.prefix(),
                "El prefix de @ConfigurationProperties debe ser 'adapter.restconsumer'");
    }

    @Test
    void beanMethod_shouldReturnAWorkingWebClient() {
        var baseUrl = server.url("/").toString();
        var cfg = newConfig(baseUrl, 1_000);
        WebClient client = cfg.getWebClient(WebClient.builder());
        assertNotNull(client);

        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("pong"));

        StepVerifier.create(
                        client.get()
                                .uri("/ping")
                                .retrieve()
                                .bodyToMono(String.class)
                )
                .expectNext("pong")
                .verifyComplete();
    }

    // ---- helpers ----
    private static boolean hasCause(Throwable t, Class<? extends Throwable> expected) {
        Throwable cur = t;
        int guard = 0;
        while (cur != null && guard++ < 10) {
            if (expected.isInstance(cur)) return true;
            cur = cur.getCause();
        }
        return false;
    }
}
