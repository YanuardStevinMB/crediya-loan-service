package com.crediya.loan.sqs.listener;

import com.crediya.loan.sqs.listener.dto.AnswersApplicationSqsDto;
import com.crediya.loan.sqs.listener.mapper.AnswersApplicationSqsMapper;
import com.crediya.loan.usecase.calculateborrowingcapacity.UpdateAutomaticStatusUseCase;
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

    private final UpdateAutomaticStatusUseCase updateAutomaticStatus;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> apply(Message message) {
        return Mono.fromCallable(() -> {
                    AnswersApplicationSqsDto dto = objectMapper.readValue(message.body(), AnswersApplicationSqsDto.class);
                    log.info("[SQSProcessor] Mensaje recibido body={}", message.body());
                    return dto;
                })
                .map(AnswersApplicationSqsMapper::toDomain)
                .flatMap(updateAutomaticStatus::execute)
                .doOnSuccess(v -> log.info("[SQSProcessor] Procesado correctamente id={}", message.messageId()))
                .onErrorResume(e -> {
                    log.error("[SQSProcessor]  Error procesando mensaje. body={}", message.body(), e);
                    return Mono.empty();
                })
                .then();
    }
}
