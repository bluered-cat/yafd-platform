package com.yafd.orderservice.config;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.util.Timeout;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    /**
     * RestTemplate backed by a pooled Apache HttpClient 5 connection manager.
     *
     * Default RestTemplate (SimpleClientHttpRequestFactory) opens a brand-new
     * TCP connection for every call.  The order-service makes 5+ sequential
     * inter-service calls per request (menu, account × 3, payment), so under
     * concurrent load the handshake overhead compounds and thread exhaustion
     * causes cascade 502s that take minutes to clear.
     *
     * Connection pooling fixes both problems:
     *   • connections are reused → no per-call TCP handshake
     *   • hard timeouts → slow downstream services fail fast, freeing threads
     *
     * maxTotal          – total pooled connections across all downstream services
     * defaultMaxPerRoute – max connections to any single service (menu/account/…)
     * connectTimeout    – max time to establish a TCP connection   (3 s)
     * responseTimeout   – max time to wait for first response byte (8 s)
     * connectionRequestTimeout – max queue wait for a pooled connection (3 s)
     */
    @Bean
    public RestTemplate restTemplate() {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(50);
        cm.setDefaultMaxPerRoute(10);

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(Timeout.ofSeconds(3))
                .setResponseTimeout(Timeout.ofSeconds(8))
                .setConnectionRequestTimeout(Timeout.ofSeconds(3))
                .build();

        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(cm)
                .setDefaultRequestConfig(requestConfig)
                .build();

        return new RestTemplate(new HttpComponentsClientHttpRequestFactory(httpClient));
    }
}
