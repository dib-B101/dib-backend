package com.b101.dib.product.command.service;

import com.b101.dib.product.command.dto.ProductModerationResponse;

public interface ProductModerationTxService {

    // AI 판정을 상품에 반영한다. HTTP 호출을 트랜잭션 안에 두지 않으려고 분리했다
    void applyVerdict(Long productId, ProductModerationResponse response);

    // 호출 자체가 실패했을 때. PENDING 에 머문 이유를 남겨 관리자가 수동 처리할 수 있게 한다
    void markCallFailed(Long productId);
}
