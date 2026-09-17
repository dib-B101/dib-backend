package com.b101.dib.product.command.service;

import com.b101.dib.auction.repository.AuctionRepository;
import com.b101.dib.auction.domain.Auction;
import com.b101.dib.auction.domain.AuctionStatus;
import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import com.b101.dib.product.command.dto.ProductCreateRequest;
import com.b101.dib.product.domain.Product;
import com.b101.dib.product.domain.ProductCondition;
import com.b101.dib.product.repository.ProductRepository;
import com.b101.dib.productImage.repository.ProductImageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProductCommandServiceImplTest {

    @Mock ProductRepository productRepository;
    @Mock ProductImageRepository productImageRepository;
    @Mock AuctionRepository auctionRepository;
    @InjectMocks ProductCommandServiceImpl productCommandService;

    @Test
    void createsProductWithReleaseYearInsteadOfAuctionTime() {
        ProductCreateRequest request = validRequest();
        MockMultipartFile jpeg = new MockMultipartFile(
                "images", "camera.jpg", "image/jpeg",
                new byte[] {(byte) 0xff, (byte) 0xd8, (byte) 0xff, 0x00}
        );

        Product product = productCommandService.create(17L, request, List.of(jpeg));

        assertThat(product.getMemberId()).isEqualTo(17L);
        assertThat(product.getReleaseYear()).isEqualTo(1982);
        assertThat(product.getStatus()).isEqualTo(com.b101.dib.product.domain.ProductStatus.REGISTERED);
        verify(productRepository).save(any(Product.class));
        ArgumentCaptor<Auction> auctionCaptor = ArgumentCaptor.forClass(Auction.class);
        verify(auctionRepository).save(auctionCaptor.capture());
        assertThat(auctionCaptor.getValue().getStatus()).isEqualTo(AuctionStatus.SCHEDULED);
        assertThat(auctionCaptor.getValue().getStartPrice()).isEqualTo(30_000L);
        assertThat(auctionCaptor.getValue().getAuctionTime()).isEqualTo(300);
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
