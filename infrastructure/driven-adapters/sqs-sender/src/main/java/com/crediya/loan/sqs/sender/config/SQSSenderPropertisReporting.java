package com.crediya.loan.sqs.sender.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "adapter.sqsreporting")
public record SQSSenderPropertisReporting (
    String region,
    String queueUrl,
    String endpoint
) {}
