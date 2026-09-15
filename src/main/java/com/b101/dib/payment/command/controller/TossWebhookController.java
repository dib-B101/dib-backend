package com.b101.dib.payment.command.controller;

import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.payment.command.service.TossWebhookService;
import com.b101.dib.payment.toss.TossWebhookEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
@Slf4j
public class TossWebhookController {
    private final TossWebhookService tossWebhookService;

    @PostMapping("/toss-payments")
    public ResponseEntity<Void> receive(@RequestBody TossWebhookEvent event) {
        try {
            tossWebhookService.handle(event);
        } catch (BusinessException e) {
            log.warn("웹훅 처리 실패 code={} event={}", e.getErrorCode(), event);
        } catch (Exception e) {
            log.error("웹훅 처리 중 예외 event={}", event, e);
        }
        return ResponseEntity.ok().build();
    }
}
