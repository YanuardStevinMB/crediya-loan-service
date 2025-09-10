package com.crediya.loan.sqs.sender.config;

import com.crediya.loan.sqs.sender.SqsPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import software.amazon.awssdk.http.SdkHttpResponse;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SqsPublisherTest {

    @Mock
    private SqsAsyncClient client;

    private SqsPublisher publisher;

    @BeforeEach
    void setup() {
        publisher = new SqsPublisher(client);
    }

    @Test
    void publish_shouldReturnMessageId_andSendAttributes() {
        // given
        var response = SendMessageResponse.builder()
                .messageId("12345")
                .sdkHttpResponse(SdkHttpResponse.builder().statusCode(200).build())
                .build();

        when(client.sendMessage(any(SendMessageRequest.class)))
                .thenAnswer(inv -> CompletableFuture.completedFuture(response));

        var attrs = Map.of("attr1", MessageAttributeValue.builder()
                .dataType("String")
                .stringValue("value1")
                .build());

        // when
        Mono<String> result = publisher.publish("http://sqs-url", "hello", attrs);

        // then
        StepVerifier.create(result)
                .expectNext("12345")
                .verifyComplete();

        ArgumentCaptor<SendMessageRequest> captor = ArgumentCaptor.forClass(SendMessageRequest.class);
        verify(client).sendMessage(captor.capture());

        SendMessageRequest sent = captor.getValue();
        assertEquals("http://sqs-url", sent.queueUrl());
        assertEquals("hello", sent.messageBody());
        assertTrue(sent.messageAttributes().containsKey("attr1"));
        assertEquals("value1", sent.messageAttributes().get("attr1").stringValue());
    }

    @Test
    void publish_shouldWorkWithoutAttributes() {
        var response = SendMessageResponse.builder()
                .messageId("no-attrs")
                .sdkHttpResponse(SdkHttpResponse.builder().statusCode(200).build())
                .build();

        when(client.sendMessage(any(SendMessageRequest.class)))
                .thenAnswer(inv -> CompletableFuture.completedFuture(response));

        StepVerifier.create(publisher.publish("url", "msg", null))
                .expectNext("no-attrs")
                .verifyComplete();

        StepVerifier.create(publisher.publish("url", "msg", Map.of()))
                .expectNext("no-attrs")
                .verifyComplete();
    }

    @Test
    void publish_shouldThrowError_whenHttpResponseNotSuccessful() {
        var badResponse = SendMessageResponse.builder()
                .messageId("fail")
                .sdkHttpResponse(SdkHttpResponse.builder().statusCode(500).build())
                .build();

        when(client.sendMessage(any(SendMessageRequest.class)))
                .thenAnswer(inv -> CompletableFuture.completedFuture(badResponse));

        StepVerifier.create(publisher.publish("http://sqs-url", "msg", Map.of()))
                .expectErrorMatches(err -> err instanceof IllegalStateException &&
                        err.getMessage().contains("SQS send failed"))
                .verify();
    }
}
