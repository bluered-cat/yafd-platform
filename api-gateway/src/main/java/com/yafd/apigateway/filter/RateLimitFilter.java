package com.yafd.apigateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class RateLimitFilter implements GlobalFilter, Ordered {

    @Value("${gateway.rate-limit.browsing-rate:50}")
    private int browsingRate;

    @Value("${gateway.rate-limit.order-rate:20}")
    private int orderRate;

    // Simple in-memory token bucket: key -> [count, windowStart]
    private final Map<String, long[]> buckets = new ConcurrentHashMap<>();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        String clientIp = exchange.getRequest().getRemoteAddress() != null
                ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                : "unknown";

        // Determine rate limit based on path
        int limit;
        String key;
        if (path.startsWith("/api/orders")) {
            // Per-user rate limiting for orders
            String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
            key = "order:" + (userId != null ? userId : clientIp);
            limit = orderRate;
        } else if (path.startsWith("/api/restaurants") || path.startsWith("/api/menu-items")) {
            // Per-IP rate limiting for browsing
            key = "browse:" + clientIp;
            limit = browsingRate;
        } else {
            // Default per-IP
            key = "default:" + clientIp;
            limit = browsingRate;
        }

        long now = System.currentTimeMillis();
        long windowMs = 1000; // 1-second window

        long[] bucket = buckets.compute(key, (k, v) -> {
            if (v == null || now - v[1] > windowMs) {
                return new long[]{1, now};
            }
            v[0]++;
            return v;
        });

        if (bucket[0] > limit) {
            log.warn("Rate limit exceeded for key: {} (count: {}, limit: {})", key, bucket[0], limit);
            exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            return exchange.getResponse().setComplete();
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -50; // Run after auth filter but before routing
    }
}
