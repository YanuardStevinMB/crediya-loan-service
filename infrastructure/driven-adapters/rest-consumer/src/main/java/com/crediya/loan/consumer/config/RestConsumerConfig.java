package com.crediya.loan.consumer.config;

import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import static io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Slf4j
@Configuration
@ConfigurationProperties(prefix = "adapter.restconsumer")
public class RestConsumerConfig {

    private final String url;
    private final int timeout;

    public RestConsumerConfig(
            @Value("${adapter.restconsumer.url}") String url,
            @Value("${adapter.restconsumer.timeout}") int timeout) {
        this.url = url;
        this.timeout = timeout;
    }

    @Bean
    public WebClient getWebClient(WebClient.Builder builder) {
        log.info("[RestConsumerConfig] Inicializando WebClient con baseUrl={} y timeout={}ms", url, timeout);

        return builder
                .baseUrl(url) // 👈 base del microservicio
                .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .clientConnector(getClientHttpConnector())
                .build();
    }

    private ClientHttpConnector getClientHttpConnector() {
        return new ReactorClientHttpConnector(
                HttpClient.create()
                        .compress(true)
                        .keepAlive(true)
                        .option(CONNECT_TIMEOUT_MILLIS, timeout)
                        .doOnConnected(conn -> {
                            conn.addHandlerLast(new ReadTimeoutHandler(timeout, MILLISECONDS));
                            conn.addHandlerLast(new WriteTimeoutHandler(timeout, MILLISECONDS));
                        })
        );
    }
}
