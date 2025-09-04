package com.crediya.loan.consumer;

import com.crediya.loan.consumer.dto.LoadUsersResponseDto;
import com.crediya.loan.consumer.mapper.UserLoadMapper;
import com.crediya.loan.model.user.User;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.context.Context;

import java.io.IOException;
import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;

class RestConsumerTest {

    private static MockWebServer mockBackEnd;
    private RestConsumer restConsumer;
    private UserLoadMapper userLoadMapper;

    @BeforeAll
    static void setUp() throws IOException {
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockBackEnd.shutdown();
    }

    @BeforeEach
    void init() {
        var webClient = WebClient.builder().baseUrl(mockBackEnd.url("/").toString()).build();
        userLoadMapper = Mockito.mock(UserLoadMapper.class);
        restConsumer = new RestConsumer(webClient, userLoadMapper);
    }

    // 🔑 helper para inyectar contexto con token
    private <T> Mono<T> withSecurityContext(Mono<T> mono) {
        return mono.contextWrite(Context.of(
                org.springframework.security.core.context.ReactiveSecurityContextHolder.CONTEXT_KEY,
                new SecurityContextImpl(
                        new UsernamePasswordAuthenticationToken("user", "fake-token")
                )
        ));
    }

    @Test
    @DisplayName("verify() → devuelve true cuando API responde éxito")
    void verifyUserSuccess() {
        mockBackEnd.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("{\"success\": true}"));

        Mono<Boolean> result = withSecurityContext(
                restConsumer.verify("123456", "test@example.com")
        );

        StepVerifier.create(result)
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    @DisplayName("verify() → devuelve false cuando API responde 4xx")
    void verifyUserNotFound() {
        mockBackEnd.enqueue(new MockResponse()
                .setResponseCode(404)
                .setBody("{\"error\": \"not found\"}"));

        Mono<Boolean> result = withSecurityContext(
                restConsumer.verify("000", "notfound@example.com")
        );

        StepVerifier.create(result)
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    @DisplayName("loadUsers() → convierte respuesta a lista de usuarios")
    void loadUsersSuccess() {
        String body = """
            {
              "data": [
                { "identityDocument": "123", "firstName": "Ana", "lastName": "Lopez", "baseSalary": 2500 }
              ]
            }
            """;

        mockBackEnd.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(body));

        // Mockear mapper
        Mockito.when(userLoadMapper.toDomain(any()))
                .thenReturn(User.builder()
                        .identityDocument("123")
                        .firstName("Ana")
                        .lastName("Lopez")
                        .baseSalary(BigDecimal.valueOf(2500))
                        .build());

        StepVerifier.create(withSecurityContext(restConsumer.loadUsers()))
                .expectNextMatches(u -> u.getIdentityDocument().equals("123")
                        && u.getFirstName().equals("Ana")
                        && u.ge().equals("Lopez")
                        && u.getBaseSalary().equals(BigDecimal.valueOf(2500)))
                .verifyComplete();
    }

    @Test
    @DisplayName("loadUsers() → devuelve vacío si no hay usuarios")
    void loadUsersEmpty() {
        String body = "{ \"data\": [] }";

        mockBackEnd.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(body));

        StepVerifier.create(withSecurityContext(restConsumer.loadUsers()))
                .verifyComplete();
    }
}
