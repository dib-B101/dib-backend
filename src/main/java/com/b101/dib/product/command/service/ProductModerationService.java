package com.b101.dib.product.command.service;

public interface ProductModerationService {

    // AI 검수를 부르고 판정을 반영한다. 커밋 후에 호출되므로 트랜잭션을 직접 열고 닫는다
    void review(Long productId);
}
