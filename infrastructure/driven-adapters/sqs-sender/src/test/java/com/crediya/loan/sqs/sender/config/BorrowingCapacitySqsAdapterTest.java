package com.crediya.loan.sqs.sender.config;

import com.crediya.loan.model.application.Application;
import com.crediya.loan.model.application.ApplicationApproved;
import com.crediya.loan.sqs.sender.BorrowingCapacitySqsAdapter;
import com.crediya.loan.sqs.sender.SqsPublisher;
import com.crediya.loan.sqs.sender.config.SQSSenderPropertisBorrowing;
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
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BorrowingCapacitySqsAdapterTest {

    @Mock
    private SqsPublisher publisher;

    @Mock
    private SQSSenderPropertisBorrowing properties;

    private ObjectMapper objectMapper;
    private BorrowingCapacitySqsAdapter adapter;

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper();
        adapter = new BorrowingCapacitySqsAdapter(publisher, properties, objectMapper);
    }

    private Application buildApp(Long id) {
        return Application.builder()
                .id(id)
                .amount(BigDecimal.valueOf(10000))
                .term(LocalDate.now().plusMonths(12))
                .email("user@mail.com")
                .identityDocument("123456")
                .stateId(1L)
                .loanTypeId(2L)
                .build();
    }

    private ApplicationApproved buildApprovedLoan() {
        return ApplicationApproved.builder()
                .amount(BigDecimal.valueOf(5000))
                .interestRate(BigDecimal.valueOf(12.5))
                .termMonths(24L)
                .build();
    }

    @Test
    void sendBorrowingCapacity_shouldSendMessageWithApprovedLoans() {
        when(properties.queueUrl()).thenReturn("http://sqs-borrowing");
        when(publisher.publish(anyString(), anyString(), anyMap())).thenReturn(Mono.just("ok-id"));

        var app = buildApp(123L);
        var approvedLoans = List.of(buildApprovedLoan());
        List<Map<String, Object>> activeLoans = List.of(Map.of("loanId", 1));

        StepVerifier.create(adapter.sendBorrowingCapacity(app, BigDecimal.valueOf(2000), approvedLoans, activeLoans))
                .expectNext("ok-id")
                .verifyComplete();

        ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, MessageAttributeValue>> attrsCaptor = ArgumentCaptor.forClass(Map.class);

        verify(publisher).publish(eq("http://sqs-borrowing"), jsonCaptor.capture(), attrsCaptor.capture());

        String sentJson = jsonCaptor.getValue();
        assertTrue(sentJson.contains("\"requestId\""));
        assertTrue(sentJson.contains("123456")); // identityDocument
        assertTrue(sentJson.contains("user@mail.com"));
        assertTrue(sentJson.contains("10000")); // loanAmount
        assertTrue(sentJson.contains("2000")); // salary
        assertTrue(sentJson.contains("approvedLoans") || sentJson.contains("activeLoans"));

        Map<String, MessageAttributeValue> attrs = attrsCaptor.getValue();
        String correlationId = attrs.get("X-Correlation-Id").stringValue();
        assertTrue(correlationId.startsWith("SOL-" + Year.now().getValue()));
    }

    @Test
    void sendBorrowingCapacity_shouldUseDefaultId_whenAppIdIsNull() {
        when(properties.queueUrl()).thenReturn("http://sqs-borrowing");
        when(publisher.publish(anyString(), anyString(), anyMap())).thenReturn(Mono.just("msg"));

        var app = buildApp(null);

        StepVerifier.create(adapter.sendBorrowingCapacity(app, BigDecimal.TEN, List.of(), List.of()))
                .expectNext("msg")
                .verifyComplete();

        ArgumentCaptor<Map<String, MessageAttributeValue>> attrsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(publisher).publish(eq("http://sqs-borrowing"), anyString(), attrsCaptor.capture());

        String correlationId = attrsCaptor.getValue().get("X-Correlation-Id").stringValue();
        assertTrue(correlationId.endsWith("000000"), "El requestId debe terminar en 000000 cuando id es null");
    }

    @Test
    void sendBorrowingCapacity_shouldReturnError_whenSerializationFails() throws Exception {
        ObjectMapper brokenMapper = mock(ObjectMapper.class);
        when(brokenMapper.writeValueAsString(any()))
                .thenThrow(new JsonProcessingException("fail") {});

        adapter = new BorrowingCapacitySqsAdapter(publisher, properties, brokenMapper);

        StepVerifier.create(adapter.sendBorrowingCapacity(buildApp(1L), BigDecimal.ONE, List.of(), List.of()))
                .expectErrorMatches(err -> err instanceof IllegalStateException &&
                        err.getMessage().contains("Error serializando payload"))
                .verify();

        verifyNoInteractions(publisher);
    }
}
