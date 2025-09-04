package com.crediya.loan.consumer;

import com.crediya.loan.consumer.mapper.UserLoadMapper;
import com.crediya.loan.model.user.User;

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

class RestConsumerTest {

    private static MockWebServer mockBackEnd;
    private RestConsumer restConsumer;

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
        var webClient = WebClient.builder()
                .baseUrl(mockBackEnd.url("/").toString())
                .build();
        var userLoadMapper = Mockito.mock(UserLoadMapper.class);
        restConsumer = new RestConsumer(webClient, userLoadMapper);

        // mock para mapear respuesta de usuarios
        Mockito.when(userLoadMapper.toDomain(any()))
                .thenReturn(User.builder()
                        .identityDocument("123")
                        .firstName("Ana")
                        .lastName("Lopez")
                        .baseSalary(BigDecimal.valueOf(2500))
                        .build());
    }

    // helper para meter un token al contexto
    private <T> Mono<T> withToken(Mono<T> mono) {
        return mono.contextWrite(Context.of(
                org.springframework.security.core.context.ReactiveSecurityContextHolder.CONTEXT_KEY,
                new SecurityContextImpl(
                        new UsernamePasswordAuthenticationToken("user", "fake-token")
                )
        ));
    }

    @Test
    void verifyReturnsTrueWhenApiSuccess() {
        mockBackEnd.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("{\"success\": true}"));

        StepVerifier.create(withToken(restConsumer.verify("123", "ana@test.com")))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void verifyReturnsFalseWhenNotFound() {
        mockBackEnd.enqueue(new MockResponse()
                .setResponseCode(404)
                .setBody("{\"error\": \"not found\"}"));

        StepVerifier.create(withToken(restConsumer.verify("000", "notfound@test.com")))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void loadUsersReturnsMappedUsers() {
        String body = """
            { "data": [ { "identityDocument": "123", "firstName": "Ana", "lastName": "Lopez", "baseSalary": 2500 } ] }
            """;

        mockBackEnd.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(body));

        StepVerifier.create(withToken(restConsumer.loadUsers()))
                .expectNextMatches(u -> u.getIdentityDocument().equals("123")
                        && u.getFirstName().equals("Ana")
                        && u.getLastName().equals("Lopez")
                        && u.getBaseSalary().equals(BigDecimal.valueOf(2500)))
                .verifyComplete();
    }

    @Test
    void loadUsersReturnsEmptyWhenNoData() {
        mockBackEnd.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("{\"data\": []}"));

        StepVerifier.create(withToken(restConsumer.loadUsers()))
                .verifyComplete();
    }
}
