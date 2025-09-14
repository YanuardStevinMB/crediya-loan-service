package com.crediya.loan.sqs.sender;

import com.crediya.loan.model.application.Application;
import com.crediya.loan.model.application.ApplicationApproved;
import com.crediya.loan.model.application.gateways.BorrowingCapacitySender;
import com.crediya.loan.model.calculateborrowingcapacity.AnswersApplicationSqs;
import com.crediya.loan.sqs.sender.config.SQSStatusChangeProperties;
import com.crediya.loan.sqs.sender.config.SQSSenderPropertisBorrowing;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.Year;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import static com.crediya.loan.usecase.shared.StatusChangeConstants.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class BorrowingCapacitySqsAdapter implements BorrowingCapacitySender {

    private final SqsPublisher publisher;
    private final SQSStatusChangeProperties properties;
    private final SQSSenderPropertisBorrowing sQSSenderPropertisBorrowing;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<String> sendBorrowingCapacity(Application app,
                                              BigDecimal baseSalary,
                                              List<ApplicationApproved> approvedLoans,
                                              List<Map<String, Object>> activeLoans ) {

        final String requestId = generateRequestId(app.getId());

        // Armado del payload de forma imperativa, pero sin side-effects sobre el flujo
        final Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("requestId", requestId);
        payload.put("id", app.getId());
        payload.put("identityDocument", app.getIdentityDocument());
        payload.put("emailClient", app.getEmail());
        payload.put("incomeTotal", baseSalary);
        payload.put("salary", baseSalary);
        payload.put("loanAmount", app.getAmount());
        payload.put("loanType", app.getLoanTypeId());
        // Nota: el código original enviaba approvedLoans en "activeLoans"
        payload.put("activeLoans", approvedLoans);
        payload.put("occurredAt", Instant.now().toString());

        return Mono.defer(() -> Mono.fromCallable(() -> objectMapper.writeValueAsString(payload))
                        .subscribeOn(Schedulers.boundedElastic()))
                .doOnSubscribe(s -> {
                    log.info("[SQS][SEND] Enviando borrowing-capacity a cola: {}", sQSSenderPropertisBorrowing.queueUrl());
                    log.info("[SQS][SEND] requestId={}", requestId);
                })
                .doOnNext(body -> log.debug("[SQS][SEND] Payload JSON: {}", body))
                .flatMap(body -> publisher.publish(
                        sQSSenderPropertisBorrowing.queueUrl(),
                        body,
                        attributes(requestId)
                ))
                .doOnSuccess(msgId -> log.info("[SQS][SEND] Publicado OK, messageId={}", msgId))
                .doOnError(e -> log.error("[SQS][SEND] Error publicando borrowing-capacity: {}", e.getMessage(), e));

    }

    @Override
    public Mono<String> sendRequestNotification(AnswersApplicationSqs msg) {
        final String requestId = generateRequestId(msg.getId());

        return Mono.defer(() -> Mono.fromCallable(() -> objectMapper.writeValueAsString(msg))
                        .subscribeOn(Schedulers.boundedElastic()))
                .doOnSubscribe(s -> {
                    log.info("[SQS][SEND] Enviando request-notification a cola: {}", properties.queueUrl());
                    log.info("[SQS][SEND] requestId={}", requestId);
                })
                .doOnNext(body -> log.debug("[SQS][SEND] Payload JSON: {}", body))
                .flatMap(body -> publisher.publish(
                        properties.queueUrl(),
                        body,
                        attributes(requestId)
                ))
                .doOnSuccess(msgId -> log.info("[SQS][SEND] Publicado OK, messageId={}", msgId))
                .doOnError(e -> log.error("[SQS][SEND] Error publicando request-notification: {}", e.getMessage(), e));
    }

    private Map<String, MessageAttributeValue> attributes(String requestId) {
        return Map.of(
                ATTR_CORRELATION_ID, MessageAttributeValue.builder()
                        .dataType(ATTR_DATA_TYPE_STRING)
                        .stringValue(requestId)
                        .build()
        );
    }

    private String generateRequestId(Long id) {
        return String.format(REQUEST_ID_PATTERN, Year.now().getValue(), id == null ? 0 : id);
    }
}
