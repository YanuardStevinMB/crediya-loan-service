package com.crediya.loan.sqs.sender;

import com.crediya.loan.model.application.ApplicationDataCompleted;
import com.crediya.loan.model.application.gateways.ApplicationSenderSqs;
import com.crediya.loan.sqs.sender.config.SQSSenderProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;

import java.time.Year;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StatusChangeSqsAdapter implements ApplicationSenderSqs {

    private final SqsPublisher publisher;
    private final SQSSenderProperties properties;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<String> sendStatusChange(ApplicationDataCompleted app) {
        // Armamos el requestId con el año y el ID de la solicitud
        String requestId = String.format("SOL-%d-%06d",
                Year.now().getValue(),
                app.getId() == null ? 0 : app.getId());

        // Normalizamos el estado
        String statusUpper = app.getState() == null ? "" : app.getState().toUpperCase(Locale.ROOT);
        String normalizedStatus =
                ("APROBADO".equals(statusUpper) || "APROBADA".equals(statusUpper)) ? "APROBADO" :
                        ("RECHAZADO".equals(statusUpper) || "RECHAZADA".equals(statusUpper)) ? "RECHAZADO" :
                                statusUpper;

        // Mensaje personalizado
        String customMessage = switch (normalizedStatus) {
            case "APROBADO" -> "Su desembolso estará disponible en las próximas 24 horas.";
            case "RECHAZADO" -> "Su solicitud fue rechazada. Puede volver a aplicar en 30 días.";
            default -> "El estado de su solicitud ha sido actualizado.";
        };

        // Payload final
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
            return Mono.error(new IllegalStateException("Error serializando payload", e));
        }

        // Enviamos con correlación = requestId
        return publisher.publish(properties.queueUrl(), json, Map.of(
                "X-Correlation-Id", MessageAttributeValue.builder()
                        .dataType("String")
                        .stringValue(requestId)
                        .build()
        ));
    }
}
