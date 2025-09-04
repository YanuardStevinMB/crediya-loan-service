package com.crediya.loan.api.config;

import com.crediya.loan.api.config.BearerServerSecurityContextRepository;
import com.crediya.loan.api.config.SecurityConfig;
import com.crediya.loan.security.JwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    @Mock
    private BearerServerSecurityContextRepository contextRepo;

    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        securityConfig = new SecurityConfig(contextRepo);
    }

    @Test
    void springSecurityFilterChain_shouldCreateValidFilterChain() {
        ServerHttpSecurity http = ServerHttpSecurity.http();

        SecurityWebFilterChain filterChain = securityConfig.springSecurityFilterChain(http);

        assertNotNull(filterChain, "El SecurityWebFilterChain no debería ser nulo");
        assertFalse(
                filterChain.getWebFilters().collectList().block().isEmpty(),
                "El filter chain debería tener filtros registrados"
        );
    }

    @Test
    void contextRepository_shouldBeInjected() {
        Object injected = ReflectionTestUtils.getField(securityConfig, "contextRepo");
        assertSame(contextRepo, injected, "contextRepo debe inyectarse correctamente");
    }

    @Test
    void springSecurityFilterChain_methodHasBeanAnnotation() throws NoSuchMethodException {
        assertTrue(
                SecurityConfig.class
                        .getMethod("springSecurityFilterChain", ServerHttpSecurity.class)
                        .isAnnotationPresent(Bean.class),
                "El método springSecurityFilterChain debe tener @Bean"
        );
    }

    @Test
    void classHasExpectedAnnotations() {
        // @Configuration
        assertTrue(
                SecurityConfig.class.isAnnotationPresent(Configuration.class),
                "SecurityConfig debe estar anotada con @Configuration"
        );

        // @EnableWebFluxSecurity
        assertTrue(
                SecurityConfig.class.isAnnotationPresent(EnableWebFluxSecurity.class),
                "SecurityConfig debe estar anotada con @EnableWebFluxSecurity"
        );

        // @EnableConfigurationProperties(JwtProperties.class)
        EnableConfigurationProperties ecp =
                SecurityConfig.class.getAnnotation(EnableConfigurationProperties.class);
        assertNotNull(ecp, "Debe tener @EnableConfigurationProperties");
        assertArrayEquals(
                new Class[]{JwtProperties.class},
                ecp.value(),
                "Debe habilitar JwtProperties en @EnableConfigurationProperties"
        );
    }
}
