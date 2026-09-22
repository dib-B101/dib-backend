package com.b101.dib.product.command.event;

// 상품 등록·수정 트랜잭션이 커밋된 뒤 AI 검수를 부르기 위한 신호.
// 커밋 전 상태를 읽으면 안 되므로 식별자만 싣고 리스너가 다시 조회한다
public record ProductModerationRequestedEvent(Long productId) {
}
