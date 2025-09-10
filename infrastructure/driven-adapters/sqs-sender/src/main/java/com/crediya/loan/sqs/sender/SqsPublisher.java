package com.crediya.loan.sqs.sender;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Log4j2
public class SqsPublisher {

    private final SqsAsyncClient client;

    public Mono<String> publish(String queueUrl, String message, Map<String, MessageAttributeValue> attrs) {
        SendMessageRequest.Builder builder = SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody(message);

        if (attrs != null && !attrs.isEmpty()) {
            builder.messageAttributes(attrs);
        }

        return Mono.fromFuture(client.sendMessage(builder.build()))
                .map(res -> {
                    if (!res.sdkHttpResponse().isSuccessful()) {
                        throw new IllegalStateException("SQS send failed: " + res.sdkHttpResponse());
                    }
                    log.info("[SQS] ✅ Published to {} msgId={}", queueUrl, res.messageId());
                    return res.messageId();
                });
    }
}