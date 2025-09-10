package com.crediya.loan.sqs.sender;

import com.crediya.loan.model.application.Application;
import com.crediya.loan.model.application.ApplicationApproved;
import com.crediya.loan.model.application.gateways.BorrowingCapacitySender;
import com.crediya.loan.sqs.sender.config.SQSSenderProperties;
import com.crediya.loan.sqs.sender.config.SQSSenderPropertisBorrowing;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.Year;
import java.util.*;

@Service
@RequiredArgsConstructor
public class BorrowingCapacitySqsAdapter implements BorrowingCapacitySender {

    private final SqsPublisher publisher;
    private final SQSSenderPropertisBorrowing properties;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<String> sendBorrowingCapacity(Application app,
                                              BigDecimal baseSalary,
                                              List<ApplicationApproved> approvedLoans,
                                              List<Map<String, Object>> activeLoans) {
        String requestId = String.format("SOL-%d-%06d",
                Year.now().getValue(),
                app.getId() == null ? 0 : app.getId());

        // Armamos el payload
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("requestId", requestId);
        payload.put("id", app.getId());
        payload.put("identityDocument", app.getIdentityDocument());
        payload.put("emailClient", app.getEmail());
        payload.put("incomeTotal", baseSalary);
        payload.put("salary", baseSalary);
        payload.put("loanAmount", app.getAmount());
        payload.put("loanType", app.getLoanTypeId());
        payload.put("activeLoans", approvedLoans); // 👈 lista completa de préstamos aprobados
        payload.put("occurredAt", Instant.now().toString());

        String json;
        try {
            json = objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            return Mono.error(new IllegalStateException("❌ Error serializando payload", e));
        }

        // Publicamos en la cola SQS con correlación = requestId
        return publisher.publish(properties.queueUrl(), json, Map.of(
                "X-Correlation-Id", MessageAttributeValue.builder()
                        .dataType("String")
                        .stringValue(requestId)
                        .build()
        ));
    }

}
