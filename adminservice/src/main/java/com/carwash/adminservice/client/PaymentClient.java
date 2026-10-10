package com.carwash.adminservice.client;

import com.carwash.adminservice.dto.PaymentSummaryDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;
import java.util.Map;

@FeignClient(name = "payment-service")
public interface PaymentClient {

    @GetMapping("/api/payments")
    List<Map<String, Object>> getAllPayments(@RequestHeader("Authorization") String token);

    @GetMapping("/api/payments/admin/summary")
    PaymentSummaryDto getPaymentSummary(@RequestHeader("Authorization") String token);
}
