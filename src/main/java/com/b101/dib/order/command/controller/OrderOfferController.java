package com.b101.dib.order.command.controller;

import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.order.command.service.OrderOfferService;
import com.b101.dib.order.domain.Order;
import com.b101.dib.order.query.dto.OrderDetailDto;
import com.b101.dib.order.query.service.OrderInternalQueryService;
import com.b101.dib.payment.command.service.PaymentCommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auctions")
@RequiredArgsConstructor
@Slf4j
public class OrderOfferController {
    private final OrderOfferService orderOfferService;
    private final OrderInternalQueryService orderInternalQueryService;
    private final PaymentCommandService paymentCommandService;

    @PostMapping("/{auctionId}/orders/accept")
    public ResponseEntity<Map<String, Object>> accept(@RequestHeader("X-Member-Id") Long memberId,
                                                      @PathVariable("auctionId") Long auctionId) {
        Order order = orderOfferService.accept(memberId, auctionId);
        String paymentResult = "PAID";
        try {
            paymentCommandService.autoCharge(order.getOrderId());
        } catch (BusinessException e) {
            log.warn("차순위 자동 결제 실패 orderId={} code={}", order.getOrderId(), e.getErrorCode());
            paymentResult = e.getErrorCode().name();
        }
        OrderDetailDto current = orderInternalQueryService.find(order.getOrderId());
        HashMap<String, Object> map = new HashMap<>();
        map.put("message", "차순위 낙찰 수락 성공");
        map.put("order", current);
        map.put("paymentResult", paymentResult);
        return ResponseEntity.status(HttpStatus.CREATED).body(map);
    }
}
