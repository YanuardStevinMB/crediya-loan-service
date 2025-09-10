package com.crediya.loan.sqs.sender.config.config;

import com.crediya.loan.sqs.sender.config.SQSSenderConfig;
import com.crediya.loan.sqs.sender.config.SQSSenderProperties;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import software.amazon.awssdk.metrics.MetricPublisher;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

class SQSSenderConfigTest {

    private final SQSSenderConfig config = new SQSSenderConfig();

    @Test
    void configSqs_shouldBuildClient_withCustomEndpoint() {
        SQSSenderProperties props = new SQSSenderProperties(
                "us-east-1",
                "queue-url",               // queueUrl dummy
                "http://localhost:4566"    // endpoint correcto
        );
        MetricPublisher publisher = Mockito.mock(MetricPublisher.class);

        SqsAsyncClient client = config.configSqs(props, publisher);

        assertNotNull(client);
        assertEquals("us-east-1", client.serviceClientConfiguration().region().id());
        assertEquals(URI.create("http://localhost:4566"), client.serviceClientConfiguration().endpointOverride().get());
    }

    @Test
    void configSqs_shouldBuildClient_withNullEndpoint() {
        SQSSenderProperties props = new SQSSenderProperties(
                "us-west-2",
                "queue-url",   // siempre hay que pasarlo, aunque sea dummy
                null           // endpoint = null
        );
        MetricPublisher publisher = Mockito.mock(MetricPublisher.class);

        SqsAsyncClient client = config.configSqs(props, publisher);

        assertNotNull(client);
        assertEquals("us-west-2", client.serviceClientConfiguration().region().id());
        assertTrue(client.serviceClientConfiguration().endpointOverride().isEmpty(),
                "Cuando no se pasa endpoint, no debería configurarse override");
    }
}
