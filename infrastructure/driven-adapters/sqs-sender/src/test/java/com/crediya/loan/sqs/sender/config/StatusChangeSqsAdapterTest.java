package com.crediya.loan.sqs.sender.config;

import com.crediya.loan.model.application.ApplicationDataCompleted;
import com.crediya.loan.sqs.sender.SqsPublisher;
import com.crediya.loan.sqs.sender.StatusChangeSqsAdapter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;

import java.math.BigDecimal;
import java.time.Year;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatusChangeSqsAdapterTest {

    @Mock
    private SqsPublisher publisher;

    @Mock
    private SQSStatusChangeProperties properties;

    private ObjectMapper objectMapper;
    private StatusChangeSqsAdapter adapter;

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper();
        adapter = new StatusChangeSqsAdapter(publisher, properties, objectMapper);
    }

    private ApplicationDataCompleted buildApp(Long id, String state) {
        return ApplicationDataCompleted.builder()
                .id(id)
                .amount(BigDecimal.valueOf(5000))
                .email("user@mail.com")
                .identityDocument("123456")
                .state(state)
                .loan("PERSONAL")
                .stateId(10L)
                .loanTypeId(20L)
                .build();
    }

    @Test
    void sendStatusChange_shouldSendApprovedMessage() {
        when(properties.queueUrl()).thenReturn("http://sqs-url");
        var app = buildApp(123L, "APROBADA");
        when(publisher.publish(anyString(), anyString(), anyMap())).thenReturn(Mono.just("msg-id"));

        StepVerifier.create(adapter.sendStatusChange(app))
                .expectNext("msg-id")
                .verifyComplete();

        ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, MessageAttributeValue>> attrsCaptor = ArgumentCaptor.forClass(Map.class);

        verify(publisher).publish(eq("http://sqs-url"), jsonCaptor.capture(), attrsCaptor.capture());

        String sentJson = jsonCaptor.getValue();
        assertTrue(sentJson.contains("APROBADO"));
        assertTrue(sentJson.contains("Su desembolso estará disponible"));
        assertTrue(sentJson.contains("user@mail.com"));

        Map<String, MessageAttributeValue> attrs = attrsCaptor.getValue();
        String requestId = attrs.get("X-Correlation-Id").stringValue();
        assertTrue(requestId.startsWith("SOL-" + Year.now().getValue()));
    }

    @Test
    void sendStatusChange_shouldSendRejectedMessage() {
        when(properties.queueUrl()).thenReturn("http://sqs-url");
        var app = buildApp(456L, "rechazada");
        when(publisher.publish(anyString(), anyString(), anyMap())).thenReturn(Mono.just("ok"));

        StepVerifier.create(adapter.sendStatusChange(app))
                .expectNext("ok")
                .verifyComplete();

        ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
        verify(publisher).publish(eq("http://sqs-url"), jsonCaptor.capture(), anyMap());

        String sentJson = jsonCaptor.getValue();
        assertTrue(sentJson.contains("RECHAZADO"));
        assertTrue(sentJson.contains("Puede volver a aplicar"));
    }

    @Test
    void sendStatusChange_shouldSendGenericMessage_whenStateUnknown() {
        when(properties.queueUrl()).thenReturn("http://sqs-url");
        var app = buildApp(789L, "EN_REVISION");
        when(publisher.publish(anyString(), anyString(), anyMap())).thenReturn(Mono.just("sent"));

        StepVerifier.create(adapter.sendStatusChange(app))
                .expectNext("sent")
                .verifyComplete();

        ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
        verify(publisher).publish(eq("http://sqs-url"), jsonCaptor.capture(), anyMap());

        String sentJson = jsonCaptor.getValue();
        assertTrue(sentJson.contains("EN_REVISION"));
        assertTrue(sentJson.contains("El estado de su solicitud ha sido actualizado."));
    }

    @Test
    void sendStatusChange_shouldReturnError_whenSerializationFails() throws Exception {
        ObjectMapper brokenMapper = mock(ObjectMapper.class);
        when(brokenMapper.writeValueAsString(any()))
                .thenThrow(new JsonProcessingException("boom") {});

        adapter = new StatusChangeSqsAdapter(publisher, properties, brokenMapper);

        StepVerifier.create(adapter.sendStatusChange(buildApp(1L, "APROBADO")))
                .expectErrorMatches(err -> err instanceof IllegalStateException &&
                        err.getMessage().contains("Error serializando payload"))
                .verify();

        verifyNoInteractions(publisher);
        // 👇 no stubbing de properties.queueUrl() aquí, porque nunca se usa
    }
}
