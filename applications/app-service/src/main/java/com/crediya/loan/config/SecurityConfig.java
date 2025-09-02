package com.crediya.loan.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
@EnableConfigurationProperties(com.crediya.loan.shared.security.JwtProperties.class)
@RequiredArgsConstructor

public class SecurityConfig {
    private final BearerServerSecurityContextRepository contextRepo;

//    @Bean
//    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
//        return http
//                .csrf(ServerHttpSecurity.CsrfSpec::disable)
//                .authorizeExchange(ex -> ex
//                        .anyExchange().permitAll()
//                )
//                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
//                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
//                .build();
//    }



    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)   // sin Basic
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)   // sin formulario
                .securityContextRepository(contextRepo)                  // tu Bearer repo
                .authorizeExchange(ex -> ex
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Swagger completo abierto
                        .pathMatchers("/v3/api-docs/**",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/webjars/**",
                                "/actuator/**").permitAll()
                        // Login abierto
                        .pathMatchers(HttpMethod.POST, "/api/v1/login").permitAll()
                        // Regla por rol
                        .pathMatchers(HttpMethod.POST, "/api/v1/solicitud").hasAnyRole("CLIENTE")
                        // Regla datos de usuario
                        .pathMatchers(HttpMethod.POST, "/api/v1/usuarios").hasAnyRole("ADMIN","ASESOR","CLIENTE")
                        // Resto autenticado
                        .anyExchange().authenticated()
                )
                // ⬇️ esto evita que Spring intente “/login”
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((exchange, ex) ->
                                Mono.fromRunnable(() -> exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED)))
                        .accessDeniedHandler((exchange, ex) ->
                                Mono.fromRunnable(() -> exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.FORBIDDEN)))
                )
                .build();
    }
}
