package com.b101.dib.product.command.service;

import com.b101.dib.common.ai.AiServerClient;
import com.b101.dib.product.command.dto.ProductModerationRequest;
import com.b101.dib.product.command.dto.ProductModerationResponse;
import com.b101.dib.product.domain.Product;
import com.b101.dib.product.domain.ProductStatus;
import com.b101.dib.product.query.dto.ProductDetailDto;
import com.b101.dib.product.repository.ProductMapper;
import com.b101.dib.product.repository.ProductRepository;
import com.b101.dib.productImage.domain.ProductImage;
import com.b101.dib.productImage.repository.ProductImageRepository;
import com.b101.dib.productImage.storage.ProductImageStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

// HTTP 호출을 트랜잭션 밖에 두려고 트랜잭션 경계를 ProductModerationTxService 에만 둔다
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductModerationServiceImpl implements ProductModerationService {

    // AI 가 앞에서부터 6장만 본다
    private static final int MAX_REVIEW_IMAGES = 6;

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductMapper productMapper;
    private final AiServerClient aiServerClient;
    private final ProductModerationTxService productModerationTxService;
    private final ProductImageStorage productImageStorage;

    @Value("${dib.ai.moderation-enabled:false}")
    private boolean moderationEnabled;

    @Override
    public void review(Long productId) {
        if (!moderationEnabled || !aiServerClient.isEnabled()) {
            return;
        }
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null || product.getDeletedAt() != null) {
            return;
        }
        if (product.getStatus() != ProductStatus.PENDING) {
            return;
        }

        ProductModerationRequest request = ProductModerationRequest.builder()
                .productId(productId)
                .title(product.getTitle())
                .description(product.getDescription())
                .imageUrls(imageUrls(productId))
                .categoryName(categoryName(productId))
                .build();

        ProductModerationResponse response = aiServerClient.postForObject(
                AiServerClient.MODERATION_REVIEW_PATH, request, ProductModerationResponse.class,
                "상품 검수 " + productId);

        if (response == null || response.getProductStatus() == null) {
            // 판정을 못 받았으면 PENDING 에 머문다. 검수 장애가 상품 등록을 막아서는 안 된다
            log.warn("상품 검수 응답 없음 productId={} — PENDING 유지", productId);
            productModerationTxService.markCallFailed(productId);
            return;
        }
        productModerationTxService.applyVerdict(productId, response);
    }

    private List<String> imageUrls(Long productId) {
        List<ProductImage> images = productImageRepository.findAllByProductId(productId);
        return images.stream()
                .sorted(Comparator.comparing(ProductImage::getSequence,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .map(ProductImage::getImageUrl)
                .filter(url -> url != null && !url.isBlank())
                .map(productImageStorage::internalUrl)
                .limit(MAX_REVIEW_IMAGES)
                .toList();
    }

    // 판정 정확도를 높이는 참고 정보라 못 구하면 null 로 보낸다
    private String categoryName(Long productId) {
        ProductDetailDto detail = productMapper.findById(productId);
        return detail == null ? null : detail.getCategoryName();
    }
}
