package com.crediya.loan.sqs.listener;

import com.crediya.loan.model.application.AnswersApplicationSqs;
import com.crediya.loan.usecase.calculateborrowingcapacity.UpdateValidatedRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.model.Message;

import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class SQSProcessor implements Function<Message, Mono<Void>> {

    private final UpdateValidatedRequest updateValidatedRequest;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> apply(Message message) {
        return Mono.fromCallable(() -> {
                    // mapear directamente el JSON al DTO
                    AnswersApplicationSqs dto = objectMapper.readValue(
                            message.body(),
                            AnswersApplicationSqs.class
                    );

                    log.info("[SQSProcessor] Mensaje recibido → id={} code={}", dto.getId(), dto.getCode());
                    return dto;
                })
                .flatMap(updateValidatedRequest::execute) // invocamos al caso de uso
                .doOnSuccess(r -> log.info("[SQSProcessor] ✅ Procesado correctamente"))
                .doOnError(e -> log.error("[SQSProcessor] ❌ Error procesando mensaje", e))
                .then(); // Mono<Void>
    }
}
