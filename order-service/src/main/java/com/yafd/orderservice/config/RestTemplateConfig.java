package com.yafd.orderservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    /**
     * RestTemplate with explicit timeouts to prevent thread exhaustion.
     *
     * Without timeouts, a slow downstream service (menu/account/payment/voucher)
     * causes order-service Tomcat threads to block indefinitely.  Under concurrent
     * load all threads fill up, the service stops accepting requests, and the ALB
     * returns 502s to callers — which then take many seconds to recover even after
     * load drops.  These timeouts ensure every inter-service call fails fast
     * instead of piling up blocked threads.
     *
     * connectTimeout – max time to establish the TCP connection (3 s)
     * readTimeout    – max time to wait for the first response byte (8 s)
     */
    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3_000);
        factory.setReadTimeout(8_000);
        return new RestTemplate(factory);
    }
}
