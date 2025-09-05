package com.crediya.loan.api.config;


import com.crediya.loan.security.JwtReactiveAuthenticationManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BearerSecurityContextRepositoryTest {

    @Mock
    private JwtReactiveAuthenticationManager authManager;

    @Mock
    private Authentication authentication;

    private BearerServerSecurityContextRepository repository;

    @BeforeEach
    void setUp() {
        repository = new BearerServerSecurityContextRepository(authManager);
    }

    @Test
    void save_shouldReturnEmptyMono() {
        ServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/"));
        SecurityContext context = mock(SecurityContext.class);

        StepVerifier.create(repository.save(exchange, context))
                .verifyComplete();

        // No debería hacer ninguna operación ya que es stateless
        verifyNoInteractions(authManager);
    }



    @Test
    void load_shouldReturnEmptyWhenNoAuthorizationHeader() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/").build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(repository.load(exchange))
                .verifyComplete();

        verifyNoInteractions(authManager);
    }

    @Test
    void load_shouldReturnEmptyWhenAuthorizationHeaderDoesNotStartWithBearer() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/")
                .header(HttpHeaders.AUTHORIZATION, "Basic credentials")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(repository.load(exchange))
                .verifyComplete();

        verifyNoInteractions(authManager);
    }


    @Test
    void load_shouldHandleAuthenticationFailure() {
        String token = "invalid.jwt.token";
        MockServerHttpRequest request = MockServerHttpRequest.get("/")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(Mono.error(new RuntimeException("Authentication failed")));

        StepVerifier.create(repository.load(exchange))
                .expectError(RuntimeException.class)
                .verify();

        verify(authManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void constructor_shouldSetAuthManager() {
        assertNotNull(repository);
        assertTrue(repository instanceof BearerServerSecurityContextRepository);
    }

    @Test
    void class_shouldHaveCorrectAnnotations() {
        assertTrue(BearerServerSecurityContextRepository.class.isAnnotationPresent(org.springframework.stereotype.Component.class));
    }
}