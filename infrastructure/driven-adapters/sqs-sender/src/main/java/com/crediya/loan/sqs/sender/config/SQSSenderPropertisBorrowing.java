package com.crediya.loan.sqs.sender.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "adapter.sqsborrowing")
public record SQSSenderPropertisBorrowing (
    String region,
    String queueUrl,
    String endpoint
){}
