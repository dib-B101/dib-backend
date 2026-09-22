package com.b101.dib.product.command.event;

import com.b101.dib.product.command.config.ProductModerationExecutorConfig;
import com.b101.dib.product.command.service.ProductModerationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.concurrent.Executor;

// 커밋된 뒤에만 검수를 부른다. 커밋 전 상태로 판정하면 롤백된 상품을 검수하게 된다
@Component
@Slf4j
public class ProductModerationEventListener {

    private final ProductModerationService productModerationService;
    private final Executor moderationExecutor;

    public ProductModerationEventListener(
            ProductModerationService productModerationService,
            @Qualifier(ProductModerationExecutorConfig.EXECUTOR_BEAN) Executor moderationExecutor) {
        this.productModerationService = productModerationService;
        this.moderationExecutor = moderationExecutor;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onModerationRequested(ProductModerationRequestedEvent event) {
        Long productId = event.productId();
        try {
            // AFTER_COMMIT 리스너는 등록 요청 스레드에서 돌아간다. 여기서 직접 부르면 등록 응답이 AI 응답을 기다린다
            moderationExecutor.execute(() -> {
                try {
                    productModerationService.review(productId);
                } catch (Exception e) {
                    // 커밋은 이미 끝났다. 상품은 PENDING 에 남아 관리자가 수동 처리할 수 있다
                    log.error("상품 검수 처리 실패 productId={} — PENDING 유지", productId, e);
                }
            });
        } catch (Exception e) {
            // 큐 포화 등으로 접수조차 못 했다. 예외를 등록 트랜잭션 쪽으로 흘리지 않는다
            log.error("상품 검수 접수 실패 productId={} — PENDING 유지", productId, e);
        }
    }
}
