// infra: com.crediya.loan.sqs.sender.SQSSender
package com.crediya.loan.sqs.sender;

import com.crediya.loan.model.application.ApplicationDataCompleted;
import com.crediya.loan.model.application.gateways.ApplicationSenderSqs;
import com.crediya.loan.sqs.sender.config.SQSSenderProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.Year;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@Service
@Log4j2
@RequiredArgsConstructor
public class SQSSender implements ApplicationSenderSqs {

    private final SQSSenderProperties properties;
    private final SqsAsyncClient client;
    private final ObjectMapper objectMapper;

    // ===== 1) Envío + verificación por ACK (HTTP + MD5 del body) =====
    public Mono<String> send(String message) {
        return Mono.fromCallable(() -> buildRequest(message))
                .flatMap(req -> Mono.fromFuture(client.sendMessage(req)))
                .map(res -> {
                    boolean httpOk = res.sdkHttpResponse() != null && res.sdkHttpResponse().isSuccessful();
                    boolean md5Ok  = safeEqualsIgnoreCase(res.md5OfMessageBody(), md5Hex(message));

                    if (!httpOk || !md5Ok || res.messageId() == null) {
                        String detail = String.format("httpOk=%s, md5Ok=%s, msgId=%s, md5Resp=%s",
                                httpOk, md5Ok, res.messageId(), res.md5OfMessageBody());
                        log.warn("[SQS] ❌ Verificación de ACK fallida: {}", detail);
                        throw new IllegalStateException("SQS ACK verification failed: " + detail);
                    }

                    log.debug("[SQS] ✅ ACK ok: messageId={}, httpStatus={}, md5Ok={}",
                            res.messageId(),
                            (res.sdkHttpResponse() != null ? res.sdkHttpResponse().statusCode() : -1),
                            md5Ok);
                    return res.messageId();
                });
    }

    // ===== 2) Variante con atributo de correlación (útil para auditoría) =====
    private Mono<String> sendWithAttributes(String message, String correlationId) {
        return Mono.fromCallable(() -> buildRequestWithAttributes(message, correlationId))
                .flatMap(req -> Mono.fromFuture(client.sendMessage(req)))
                .map(res -> {
                    boolean httpOk = res.sdkHttpResponse() != null && res.sdkHttpResponse().isSuccessful();
                    boolean md5Ok  = safeEqualsIgnoreCase(res.md5OfMessageBody(), md5Hex(message));
                    // Si también quieres validar atributos:
                    // boolean attrsOk = res.md5OfMessageAttributes() != null; // opcional

                    if (!httpOk || !md5Ok || res.messageId() == null) {
                        String detail = String.format("httpOk=%s, md5Ok=%s, msgId=%s, md5Resp=%s",
                                httpOk, md5Ok, res.messageId(), res.md5OfMessageBody());
                        log.warn("[SQS] ❌ Verificación de ACK fallida (attrs): {}", detail);
                        throw new IllegalStateException("SQS ACK verification failed: " + detail);
                    }

                    log.info("[SQS] ✅ Enviado con X-Correlation-Id={} messageId={}", correlationId, res.messageId());
                    return res.messageId();
                });
    }

    @Override
    public Mono<String> sendStatusChange(ApplicationDataCompleted app) {
        return Mono.fromSupplier(() -> buildPayload(app))
                .map(this::toJson)
                .flatMap(json -> {
                    // Usamos el requestId como correlación para trazabilidad
                    String correlationId = extractRequestId(json);
                    return sendWithAttributes(json, correlationId);
                });
    }

    // ===== Builders =====

    private SendMessageRequest buildRequest(String message) {
        return SendMessageRequest.builder()
                .queueUrl(properties.queueUrl())
                .messageBody(message)
                .build();
    }

    private SendMessageRequest buildRequestWithAttributes(String message, String correlationId) {
        Map<String, MessageAttributeValue> attrs = Map.of(
                "X-Correlation-Id", MessageAttributeValue.builder()
                        .dataType("String")
                        .stringValue(correlationId)
                        .build()
        );

        return SendMessageRequest.builder()
                .queueUrl(properties.queueUrl())
                .messageBody(message)
                .messageAttributes(attrs)
                .build();
    }

    private String toJson(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            throw new IllegalStateException("Error serializando payload a JSON", e);
        }
    }

    private Map<String, Object> buildPayload(ApplicationDataCompleted app) {
        final String requestId = String.format("SOL-%d-%06d",
                Year.now().getValue(), app.getId() == null ? 0 : app.getId());

        final String statusUpper = app.getState() == null
                ? "ACTUALIZADA"
                : app.getState().toUpperCase(Locale.ROOT);

        final String normalizedStatus =
                ("APROBADO".equals(statusUpper) || "APROBADA".equals(statusUpper)) ? "APROBADO" :
                        ("RECHAZADO".equals(statusUpper) || "RECHAZADA".equals(statusUpper)) ? "RECHAZADO" :
                                statusUpper;

        final String customMessage = switch (normalizedStatus) {
            case "APROBADO"  -> "Su desembolso estará disponible en las próximas 24 horas.";
            case "RECHAZADO" -> "Su solicitud fue rechazada. Puede volver a aplicar en 30 días.";
            default          -> "El estado de su solicitud ha sido actualizado.";
        };

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("requestId", requestId);
        payload.put("status", normalizedStatus);
        payload.put("emailClient", app.getEmail());
        payload.put("identityDocument", app.getIdentityDocument());
        payload.put("loanAmount", app.getAmount());
        payload.put("loanType", app.getLoan());
        payload.put("customMessage", customMessage);
        payload.put("occurredAt", Instant.now().toString());
        return payload;
    }

    // ===== Utils =====
    private static String md5Hex(String body) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(body.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo calcular MD5", e);
        }
    }

    private static boolean safeEqualsIgnoreCase(String a, String b) {
        return a == null ? b == null : a.equalsIgnoreCase(b);
    }

    // extrae requestId del JSON (si prefieres evitar parsear, pásalo como arg desde buildPayload)
    private String extractRequestId(String json) {
        try {
            return objectMapper.readTree(json).path("requestId").asText("UNKNOWN");
        } catch (Exception e) {
            log.warn("No se pudo extraer requestId del JSON para correlación, uso 'UNKNOWN'");
            return "UNKNOWN";
        }
    }
}
