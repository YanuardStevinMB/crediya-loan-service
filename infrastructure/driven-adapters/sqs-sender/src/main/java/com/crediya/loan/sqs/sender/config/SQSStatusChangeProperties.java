package com.crediya.loan.sqs.sender.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "adapter.sqs")
public record SQSStatusChangeProperties(
     String region,
     String queueUrl,
     String endpoint){
}
