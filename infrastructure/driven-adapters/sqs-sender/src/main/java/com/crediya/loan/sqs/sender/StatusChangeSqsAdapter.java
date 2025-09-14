package com.crediya.loan.sqs.sender;

import com.crediya.loan.model.application.ApplicationDataCompleted;
import com.crediya.loan.model.application.gateways.ApplicationSenderSqs;
import com.crediya.loan.sqs.sender.config.SQSStatusChangeProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;

import java.time.Year;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import static com.crediya.loan.usecase.shared.StatusChangeConstants.*;


@Service
@RequiredArgsConstructor
public class StatusChangeSqsAdapter implements ApplicationSenderSqs {

    private final SqsPublisher publisher;
    private final SQSStatusChangeProperties properties;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<String> sendStatusChange(ApplicationDataCompleted app) {
        // RequestId: año + ID solicitud
        String requestId = String.format(REQUEST_ID_PATTERN,
                Year.now().getValue(),
                app.getId() == null ? 0 : app.getId());

        // Normalizar estado
        String statusUpper = app.getState() == null ? "" : app.getState().toUpperCase(NORMALIZE_LOCALE);
        String normalizedStatus =
                (STATUS_APROBADO.equals(statusUpper) || "APROBADA".equals(statusUpper)) ? STATUS_APROBADO :
                        (STATUS_RECHAZADO.equals(statusUpper) || "RECHAZADA".equals(statusUpper)) ? STATUS_RECHAZADO :
                                statusUpper;

        // Mensaje personalizado por estado
        String customMessage = switch (normalizedStatus) {
            case STATUS_APROBADO  -> MSG_ON_APROBADO;
            case STATUS_RECHAZADO -> MSG_ON_RECHAZADO;
            default               -> MSG_ON_UPDATED;
        };

        // Payload
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("requestId", requestId);
        payload.put("status", normalizedStatus);
        payload.put("emailClient", app.getEmail());
        payload.put("identityDocument", app.getIdentityDocument());
        payload.put("loanAmount", app.getAmount());
        payload.put("loanType", app.getLoan());
        payload.put("customMessage", customMessage);

        String json;
        try {
            json = objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            return Mono.error(new IllegalStateException(ERR_SERIALIZING_PAYLOAD, e));
        }

        // Publicar con correlación
        return publisher.publish(
                properties.queueUrl(),
                json,
                Map.of(
                        ATTR_CORRELATION_ID, MessageAttributeValue.builder()
                                .dataType(ATTR_DATA_TYPE_STRING)
                                .stringValue(requestId)
                                .build()
                )
        );
    }
}
