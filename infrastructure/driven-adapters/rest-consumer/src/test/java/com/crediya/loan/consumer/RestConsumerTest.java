package com.crediya.loan.consumer;

import com.crediya.loan.consumer.dto.LoadUsersResponseDto;
import com.crediya.loan.consumer.dto.UserDto;
import com.crediya.loan.consumer.dto.UserExistResponseDto;
import com.crediya.loan.consumer.mapper.UserLoadMapper;
import com.crediya.loan.model.user.User;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.core.context.ReactiveSecurityContextHolder.withSecurityContext;

@ExtendWith(MockitoExtension.class)
class RestConsumerTest {

    private MockWebServer server;
    private WebClient client;

    private UserLoadMapper mapper; // mock
    private RestConsumer restConsumer;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();

        client = WebClient.builder()
                .baseUrl(server.url("/").toString())
                .build();

        mapper = mock(UserLoadMapper.class);
        restConsumer = new RestConsumer(client, mapper);
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    // ===== Helpers =====
    private SecurityContextImpl scWithToken(String token) {
        var auth = new UsernamePasswordAuthenticationToken("user", token);
        return new SecurityContextImpl(auth);
    }

    // ===== verify(...) =====

    @Test
    void verify_shouldReturnTrue_whenApiSaysSuccess_andSendBearerHeader() throws InterruptedException {
        // Respuesta 200 OK con success=true
        var body = "{\"success\":true}";
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(body)
                .addHeader("Content-Type", "application/json"));

        var token = "tok-abc";
        var sc = scWithToken(token);

        StepVerifier.create(
                        restConsumer.verify("123", "a@b.c")
                                .contextWrite(withSecurityContext(Mono.just(sc)))
                )
                .expectNext(true)
                .verifyComplete();

        // Verifica request
        RecordedRequest req = server.takeRequest();
        assertEquals("/api/v1/users/exist", req.getPath());
        assertEquals("POST", req.getMethod());
        assertEquals("Bearer " + token, req.getHeader("Authorization"));
    }

    @Test
    void verify_shouldReturnFalse_on4xxMappedAsUserNotFound() {
        // 404 -> onStatus(is4xx) -> IllegalArgumentException -> onErrorResume -> false
        server.enqueue(new MockResponse()
                .setResponseCode(404)
                .setBody("not found")
                .addHeader("Content-Type", "text/plain"));

        var sc = scWithToken("tok-404");

        StepVerifier.create(
                        restConsumer.verify("999", "x@y.z")
                                .contextWrite(withSecurityContext(Mono.just(sc)))
                )
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void verify_shouldError_on5xx() {
        // 500 -> WebClientResponseException (no onStatus para 5xx)
        server.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("boom")
                .addHeader("Content-Type", "text/plain"));

        var sc = scWithToken("tok-500");

        StepVerifier.create(
                        restConsumer.verify("555", "e@e.e")
                                .contextWrite(withSecurityContext(Mono.just(sc)))
                )
                .expectErrorSatisfies(err -> {
                    assertTrue(err instanceof WebClientResponseException);
                    var we = (WebClientResponseException) err;
                    assertEquals(500, we.getRawStatusCode());
                })
                .verify();
    }

    // ===== loadUsers() =====

    @Test
    void loadUsers_shouldMapAndEmitUsers_andSendBearerHeader() throws InterruptedException {
        var body = """
                {
                  "success": true,
                  "data": [
                    {"firstName":"Ana","lastName":"Diaz","identityDocument":"CC1","baseSalary": 1200.50},
                    {"firstName":"Luis","lastName":"Vega","identityDocument":"CC2","baseSalary": 2000.00}
                  ]
                }
                """;
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(body)
                .addHeader("Content-Type", "application/json"));

        // Mock del mapper: construye el dominio desde el DTO recibido
        when(mapper.toDomain(ArgumentMatchers.any(UserDto.class)))
                .thenAnswer(inv -> {
                    UserDto dto = inv.getArgument(0);
                    return User.builder()
                            .firstName(dto.getFirstName())
                            .lastName(dto.getLastName())
                            .identityDocument(dto.getIdentityDocument())
                            .baseSalary(dto.getBaseSalary())
                            .build();
                });

        var token = "tok-users";
        var sc = scWithToken(token);

        StepVerifier.create(
                        restConsumer.loadUsers()
                                .contextWrite(withSecurityContext(Mono.just(sc)))
                )
                .expectNextMatches(u ->
                        u.getFirstName().equals("Ana") &&
                                u.getLastName().equals("Diaz") &&
                                u.getIdentityDocument().equals("CC1") &&
                                new BigDecimal("1200.50").compareTo(u.getBaseSalary()) == 0
                )
                .expectNextMatches(u ->
                        u.getFirstName().equals("Luis") &&
                                u.getLastName().equals("Vega") &&
                                u.getIdentityDocument().equals("CC2") &&
                                new BigDecimal("2000.00").compareTo(u.getBaseSalary()) == 0
                )
                .verifyComplete();

        RecordedRequest req = server.takeRequest();
        assertEquals("/api/v1/usuarios", req.getPath());
        assertEquals("GET", req.getMethod());
        assertEquals("Bearer " + token, req.getHeader("Authorization"));
    }

    @Test
    void loadUsers_shouldCompleteEmpty_whenDataIsEmptyArray() {
        var body = """
                { "success": true, "data": [] }
                """;
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(body)
                .addHeader("Content-Type", "application/json"));

        var sc = scWithToken("tok-empty");

        StepVerifier.create(
                        restConsumer.loadUsers()
                                .contextWrite(withSecurityContext(Mono.just(sc)))
                )
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void loadUsers_shouldCompleteEmpty_whenDataIsNull() {
        var body = """
                { "success": true }
                """;
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(body)
                .addHeader("Content-Type", "application/json"));

        var sc = scWithToken("tok-null");

        StepVerifier.create(
                        restConsumer.loadUsers()
                                .contextWrite(withSecurityContext(Mono.just(sc)))
                )
                .expectNextCount(0)
                .verifyComplete();
    }
}
