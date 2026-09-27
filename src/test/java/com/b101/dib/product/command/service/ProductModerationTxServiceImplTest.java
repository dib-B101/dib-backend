package com.b101.dib.product.command.service;

import com.b101.dib.auction.repository.AuctionRepository;
import com.b101.dib.notification.domain.Notification;
import com.b101.dib.notification.repository.NotificationRepository;
import com.b101.dib.product.command.dto.ProductModerationResponse;
import com.b101.dib.product.domain.Product;
import com.b101.dib.product.domain.ProductStatus;
import com.b101.dib.product.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProductModerationTxServiceImplTest {
    @Mock ProductRepository productRepository;
    @Mock AuctionRepository auctionRepository;
    @Mock NotificationRepository notificationRepository;

    @Test
    void approvedProductNotifiesOwnerWithProductLink() {
        Product product = pendingProduct();
        given(productRepository.findById(11L)).willReturn(Optional.of(product));

        service().applyVerdict(11L, response("REGISTERED"));

        ArgumentCaptor<Notification> saved = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(saved.capture());
        assertThat(saved.getValue().getMemberId()).isEqualTo(22L);
        assertThat(saved.getValue().resourceType()).isEqualTo("PRODUCT");
        assertThat(saved.getValue().resourceId()).isEqualTo(11L);
        assertThat(saved.getValue().getTitle()).isEqualTo("상품 검수 승인");
        assertThat(product.getStatus()).isEqualTo(ProductStatus.REGISTERED);
    }

    @Test
    void rejectedProductNotifiesOwnerOnlyOnce() {
        Product product = pendingProduct();
        given(productRepository.findById(11L)).willReturn(Optional.of(product));

        service().applyVerdict(11L, response("REJECTED"));
        service().applyVerdict(11L, response("REJECTED"));

        ArgumentCaptor<Notification> saved = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(saved.capture());
        assertThat(saved.getValue().getTitle()).isEqualTo("상품 등록 거절");
        assertThat(product.getStatus()).isEqualTo(ProductStatus.REJECTED);
    }

    @Test
    void pendingVerdictDoesNotClaimReviewCompleted() {
        given(productRepository.findById(11L)).willReturn(Optional.of(pendingProduct()));

        service().applyVerdict(11L, response("PENDING"));

        verify(notificationRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    private ProductModerationTxServiceImpl service() {
        return new ProductModerationTxServiceImpl(productRepository, auctionRepository, notificationRepository);
    }

    private Product pendingProduct() {
        return Product.builder().productId(11L).memberId(22L).title("테스트 상품")
                .status(ProductStatus.PENDING).build();
    }

    private ProductModerationResponse response(String status) {
        ProductModerationResponse response = new ProductModerationResponse();
        response.setProductStatus(status);
        return response;
    }
}
