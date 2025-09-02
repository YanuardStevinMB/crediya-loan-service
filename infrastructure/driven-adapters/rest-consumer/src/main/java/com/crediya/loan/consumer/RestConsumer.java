package com.crediya.loan.consumer;

import com.crediya.loan.consumer.dto.UserExistRequestDto;
import com.crediya.loan.consumer.dto.UserExistResponseDto;
import com.crediya.loan.usecase.generaterequest.gateway.DocumentVerificationGateway;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestConsumer implements DocumentVerificationGateway {

    private final WebClient client;

    @Override
    @CircuitBreaker(name = "userExist")
    public Mono<Boolean> verify(String documentNumber, String email) {
        var request = UserExistRequestDto.builder()
                .document(documentNumber)
                .email(email)
                .build();

        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication().getCredentials().toString())
                .flatMap(token -> {
                    log.info("[RestConsumer.verify] Preparando request → documentNumber={}, email={}", documentNumber, email);

                    return client.post()
                            .uri("/api/v1/users/exist")
                            .header("Authorization", "Bearer " + token)
                            .bodyValue(request)
                            .retrieve()
                            // ⬇️ Aquí interceptamos errores 4xx
                            .onStatus(status -> status.is4xxClientError(), response -> {
                                log.warn("[RestConsumer.verify] La API devolvió 4xx, interpretando como usuario no encontrado.");
                                return response.bodyToMono(String.class)
                                        .flatMap(body -> {
                                            log.warn("[RestConsumer.verify] Respuesta de error: {}", body);
                                            return Mono.error(new IllegalArgumentException(
                                                    "Los datos ingresados del usuario no son los que están en el sistema."
                                            ));
                                        });
                            })
                            .bodyToMono(UserExistResponseDto.class)
                            .doOnSubscribe(sub -> log.info("[RestConsumer.verify] Request enviado a /api/v1/users/exist"))
                            .doOnNext(resp -> log.info("[RestConsumer.verify] Respuesta recibida: success={}", resp.isSuccess()))
                            .map(UserExistResponseDto::isSuccess)
                            .onErrorResume(IllegalArgumentException.class, ex -> {
                                // ⚠️ Si quieres devolver false en vez de propagar excepción
                                log.warn("[RestConsumer.verify] Usuario no encontrado → {}", ex.getMessage());
                                return Mono.just(false);
                            })
                            .doOnError(err -> log.error("[RestConsumer.verify] Error al consumir /api/v1/users/exist", err));
                });
    }
}