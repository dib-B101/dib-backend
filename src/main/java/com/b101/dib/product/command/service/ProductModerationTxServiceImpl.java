package com.b101.dib.product.command.service;

import com.b101.dib.auction.domain.Auction;
import com.b101.dib.auction.repository.AuctionRepository;
import com.b101.dib.notification.domain.Notification;
import com.b101.dib.notification.repository.NotificationRepository;
import com.b101.dib.product.command.dto.ProductModerationResponse;
import com.b101.dib.product.domain.Product;
import com.b101.dib.product.domain.ProductStatus;
import com.b101.dib.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

// 커밋 후 리스너에서 불리므로 항상 새 트랜잭션으로 연다
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductModerationTxServiceImpl implements ProductModerationTxService {

    private final ProductRepository productRepository;
    private final AuctionRepository auctionRepository;
    private final NotificationRepository notificationRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void applyVerdict(Long productId, ProductModerationResponse response) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null || product.getDeletedAt() != null) {
            return;
        }
        // 검수 결과가 도착하기 전에 판매자가 다시 수정했거나 관리자가 이미 처리했을 수 있다
        if (product.getStatus() != ProductStatus.PENDING) {
            log.info("검수 결과 무시 productId={} — 이미 {} 상태", productId, product.getStatus());
            return;
        }

        ProductStatus verdict = toProductStatus(response.getProductStatus());
        LocalDateTime now = LocalDateTime.now();

        product.setModerationReason(response.getReason());
        product.setModerationStage(response.getStage());
        product.setModerationContentHash(response.getContentHash());
        product.setModeratedAt(now);
        product.setStatus(verdict);
        product.setUpdatedAt(now);

        if (verdict == ProductStatus.REGISTERED) {
            createScheduledAuctionIfAbsent(productId, now);
        }
        if (verdict == ProductStatus.REGISTERED || verdict == ProductStatus.REJECTED) {
            notificationRepository.save(Notification.productModerated(
                    productId, product.getMemberId(), product.getTitle(), verdict));
        }
        log.info("상품 검수 반영 productId={} status={} stage={}", productId, verdict, response.getStage());
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markCallFailed(Long productId) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null || product.getDeletedAt() != null) {
            return;
        }
        if (product.getStatus() != ProductStatus.PENDING) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        product.setModerationStage("fallback");
        product.setModerationReason("자동 검수를 완료하지 못했습니다. 관리자 확인 후 처리됩니다.");
        // 해시를 비워 두면 다음 수정에서 반드시 재검수한다
        product.setModerationContentHash(null);
        product.setModeratedAt(now);
        product.setUpdatedAt(now);
    }

    // 검수 통과 시점에 경매 행을 만든다. 시작가·경매시간은 경매 시작 때 정한다
    private void createScheduledAuctionIfAbsent(Long productId, LocalDateTime now) {
        // 재검수를 거친 상품은 이전 승인 때 만든 경매 행이 이미 있다. 같은 상품에 두 행이 생기면 findByProductId 가 깨진다
        Auction existing = auctionRepository.findByProductId(productId);
        if (existing != null) {
            return;
        }
        auctionRepository.save(Auction.scheduled(productId, null, null, now));
    }

    private ProductStatus toProductStatus(String raw) {
        if (raw == null) {
            return ProductStatus.PENDING;
        }
        try {
            ProductStatus parsed = ProductStatus.valueOf(raw.trim());
            return switch (parsed) {
                case REGISTERED, PENDING, REJECTED -> parsed;
                // AI 가 검수와 무관한 상태를 주면 사람이 보게 둔다
                default -> ProductStatus.PENDING;
            };
        } catch (IllegalArgumentException e) {
            log.warn("알 수 없는 검수 상태 '{}' — PENDING 유지", raw);
            return ProductStatus.PENDING;
        }
    }
}
