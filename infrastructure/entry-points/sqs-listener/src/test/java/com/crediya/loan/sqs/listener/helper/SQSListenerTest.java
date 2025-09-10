package com.crediya.loan.sqs.listener.helper;

import com.crediya.loan.model.application.AnswersApplicationSqs;
import com.crediya.loan.sqs.listener.SQSProcessor;
import com.crediya.loan.sqs.listener.config.SQSProperties;
import com.crediya.loan.usecase.calculateborrowingcapacity.UpdateValidatedRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.DeleteMessageResponse;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class SQSListenerTest {

    @Mock
    private SqsAsyncClient asyncClient;

    @Mock
    private SQSProperties sqsProperties;

    @Mock
    private UpdateValidatedRequest updateValidatedRequest;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        // Configuración fake de propiedades
        sqsProperties = new SQSProperties(
                "us-east-1",
                "http://localhost:4566",
                "http://localhost:4566/00000000000/queueName",
                20,
                30,
                10,
                1
        );

        objectMapper = new ObjectMapper();

        // Mensaje simulado en la cola
        var message = Message.builder()
                .body("{\"id\":123,\"code\":\"APROB\"}")
                .receiptHandle("receipt-123")
                .build();

        var deleteMessageResponse = DeleteMessageResponse.builder().build();
        var messageResponse = ReceiveMessageResponse.builder().messages(message).build();

        when(asyncClient.receiveMessage(any(ReceiveMessageRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(messageResponse));

        when(asyncClient.deleteMessage(any(DeleteMessageRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(deleteMessageResponse));

        // Mock del caso de uso → simula que procesa sin error
        when(updateValidatedRequest.execute(any(AnswersApplicationSqs.class)))
                .thenReturn(Mono.just("OK"));
    }

    @Test
    void listenerTest() {
        var processor = new SQSProcessor(updateValidatedRequest, objectMapper);

        var sqsListener = SQSListener.builder()
                .client(asyncClient)
                .properties(sqsProperties)
                .processor(processor)
                .operation("operation")
                .build();

        // Invocamos el método privado listen() con ReflectionTestUtils
        Flux<Void> flow = ReflectionTestUtils.invokeMethod(sqsListener, "listen");

        StepVerifier.create(flow)
                .expectComplete()
                .verify();
    }
}
