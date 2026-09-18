package com.b101.dib.product.command.service;

import com.b101.dib.auction.repository.AuctionRepository;
import com.b101.dib.common.ai.AiServerClient;
import com.b101.dib.product.command.event.ProductModerationRequestedEvent;
import com.b101.dib.product.domain.ProductStatus;
import com.b101.dib.auction.domain.Auction;
import com.b101.dib.auction.domain.AuctionStatus;
import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import com.b101.dib.product.command.dto.ProductCreateRequest;
import com.b101.dib.product.domain.Product;
import com.b101.dib.product.domain.ProductCondition;
import com.b101.dib.product.repository.ProductRepository;
import com.b101.dib.productImage.repository.ProductImageRepository;
import com.b101.dib.productImage.storage.ProductImageStorage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProductCommandServiceImplTest {

    @Mock ProductRepository productRepository;
    @Mock ProductImageRepository productImageRepository;
    @Mock AuctionRepository auctionRepository;
    @Mock AiServerClient aiServerClient;
    @Mock ApplicationEventPublisher eventPublisher;
    @Mock ProductImageStorage productImageStorage;
    @InjectMocks ProductCommandServiceImpl productCommandService;

    @Test
    void createsProductWithReleaseYearInsteadOfAuctionTime() {
        // dib.ai.enabled=false 면 등록 즉시 승인 + 경매 생성이라는 기존 동작을 유지한다
        ProductCreateRequest request = validRequest();
        MockMultipartFile jpeg = new MockMultipartFile(
                "images", "camera.jpg", "image/jpeg",
                new byte[] {(byte) 0xff, (byte) 0xd8, (byte) 0xff, 0x00}
        );
        given(productImageStorage.storeAll(List.of(jpeg)))
                .willReturn(List.of("/api/v1/product-images/files/camera.jpg"));

        Product product = productCommandService.create(17L, request, List.of(jpeg));

        assertThat(product.getMemberId()).isEqualTo(17L);
        assertThat(product.getReleaseYear()).isEqualTo(1982);
        assertThat(product.getStatus()).isEqualTo(ProductStatus.REGISTERED);
        verify(productRepository).save(any(Product.class));
        ArgumentCaptor<Auction> auctionCaptor = ArgumentCaptor.forClass(Auction.class);
        verify(auctionRepository).save(auctionCaptor.capture());
        assertThat(auctionCaptor.getValue().getStatus()).isEqualTo(AuctionStatus.SCHEDULED);
        assertThat(auctionCaptor.getValue().getStartPrice()).isEqualTo(30_000L);
        assertThat(auctionCaptor.getValue().getAuctionTime()).isEqualTo(300);
    }

    // 검수가 켜지면 경매는 검수 통과 후에 만든다
    @Test
    void createsPendingProductWithoutAuctionWhenModerationEnabled() {
        ReflectionTestUtils.setField(productCommandService, "moderationEnabled", true);
        given(aiServerClient.isEnabled()).willReturn(true);
        MockMultipartFile jpeg = new MockMultipartFile(
                "images", "camera.jpg", "image/jpeg",
                new byte[] {(byte) 0xff, (byte) 0xd8, (byte) 0xff, 0x00}
        );
        given(productImageStorage.storeAll(List.of(jpeg)))
                .willReturn(List.of("/api/v1/product-images/files/camera.jpg"));

        Product product = productCommandService.create(17L, validRequest(), List.of(jpeg));

        assertThat(product.getStatus()).isEqualTo(ProductStatus.PENDING);
        ArgumentCaptor<Auction> auctionCaptor = ArgumentCaptor.forClass(Auction.class);
        verify(auctionRepository).save(auctionCaptor.capture());
        assertThat(auctionCaptor.getValue().getStartPrice()).isEqualTo(30_000L);
        assertThat(auctionCaptor.getValue().getAuctionTime()).isEqualTo(300);
        verify(eventPublisher).publishEvent(any(ProductModerationRequestedEvent.class));
    }

    @Test
    void rejectsMissingImage() {
        assertThatThrownBy(() -> productCommandService.create(17L, validRequest(), List.of()))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.IMAGE_REQUIRED));

        verify(productRepository, never()).save(any());
    }

    @Test
    void rejectsFileWhoseContentDoesNotMatchDeclaredImageType() {
        MockMultipartFile fakeJpeg = new MockMultipartFile(
                "images", "fake.jpg", "image/jpeg", "not-an-image".getBytes()
        );

        assertThatThrownBy(() -> productCommandService.create(17L, validRequest(), List.of(fakeJpeg)))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_CONTENT_TYPE));
    }

    private ProductCreateRequest validRequest() {
        return ProductCreateRequest.builder()
                .categoryId(3L)
                .title("필름 카메라")
                .description("정상 작동합니다.")
                .condition(ProductCondition.GOOD)
                .modelName("FM2")
                .releaseYear(1982)
                .marketPrice(120_000L)
                .startPrice(30_000L)
                .auctionTime(300)
                .build();
    }
}
