package com.yafd.orderservice.client;

import com.yafd.orderservice.dto.external.RedeemVoucherResponse;
import com.yafd.orderservice.dto.external.ValidateVoucherResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class VoucherServiceClient {

    private final RestTemplate restTemplate;

    @Value("${services.voucher-url}")
    private String voucherServiceUrl;

    public ValidateVoucherResponse validateVoucher(String code, BigDecimal orderAmount) {
        String url = voucherServiceUrl + "/api/vouchers/validate";
        Map<String, Object> request = Map.of(
                "code", code,
                "orderAmount", orderAmount
        );
        return restTemplate.postForObject(url, request, ValidateVoucherResponse.class);
    }

    public RedeemVoucherResponse redeemVoucher(String code, Long orderId) {
        String url = voucherServiceUrl + "/api/vouchers/redeem";
        Map<String, Object> request = Map.of(
                "code", code,
                "orderId", orderId
        );
        return restTemplate.postForObject(url, request, RedeemVoucherResponse.class);
    }
}
