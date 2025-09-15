package com.crediya.loan.sqs.sender;

import com.crediya.loan.model.application.gateways.GenerateReporting;
import com.crediya.loan.sqs.sender.config.SQSSenderPropertisReporting;
import com.crediya.loan.usecase.shared.Messages;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GenerateReportSqsAdapter implements GenerateReporting {

    private static final String ATTR_DATA_TYPE_STRING = "String";
    private static final String ATTR_DATA_TYPE_NUMBER = "Number";

    private final SqsPublisher publisher;
    private final SQSSenderPropertisReporting properties;
    private final ObjectMapper objectMapper;

    /** Construye el JSON: {"status":"APPROVED","approvedAmount":50056.00} */
    private String buildApprovedBody(BigDecimal amount) {
        try {
            if (amount == null) {
                throw new IllegalArgumentException("approvedAmount no puede ser null");
            }
            BigDecimal normalized = amount.setScale(2, RoundingMode.HALF_UP);
            return objectMapper.writeValueAsString(
                    Map.of(
                            "status", "APPROVED",
                            "approvedAmount", normalized
                    )
            );
        } catch (Exception e) {
            throw new IllegalStateException(Messages.NOT_FOUND_ENVENT, e);
        }
    }

    @Override
    public Mono<String> sendApproved(BigDecimal amount) {
        BigDecimal normalized = amount.setScale(2, RoundingMode.HALF_UP);
        String body = buildApprovedBody(normalized);

        Map<String, MessageAttributeValue> attrs = Map.of(
                "contentType", attrString("application/json"),
                "status",      attrString("APPROVED"),
                "approvedAmount", attrNumber(normalized) // para filtros/lecturas rápidas
        );

        log.info("[SQS-SENDER] Enviando mensaje a SQS...");
        log.info("[SQS-SENDER] Queue URL   : {}", properties.queueUrl());
        log.info("[SQS-SENDER] Body JSON   : {}", body);
        log.info("[SQS-SENDER] Attributes  : {}", attrs);

        return publisher.publish(properties.queueUrl(), body, attrs)
                .doOnNext(messageId -> log.info("[SQS-SENDER] Mensaje enviado con ID: {}", messageId))
                .doOnError(e -> log.error("[SQS-SENDER] Error enviando mensaje a SQS: {}", e.getMessage(), e));
    }

    private static MessageAttributeValue attrString(String value) {
        return MessageAttributeValue.builder()
                .dataType(ATTR_DATA_TYPE_STRING)
                .stringValue(value)
                .build();
    }

    private static MessageAttributeValue attrNumber(BigDecimal value) {
        return MessageAttributeValue.builder()
                .dataType(ATTR_DATA_TYPE_NUMBER)
                .stringValue(value.stripTrailingZeros().toPlainString()) // e.g., "50056.00" -> "50056"
                .build();
    }
}
