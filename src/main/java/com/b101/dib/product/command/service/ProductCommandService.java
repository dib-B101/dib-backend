package com.b101.dib.product.command.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.b101.dib.product.command.dto.ProductCreateRequest;
import com.b101.dib.product.command.dto.ModerateProductRequest;
import com.b101.dib.product.command.dto.ProductUpdateRequest;
import com.b101.dib.product.domain.Product;

public interface ProductCommandService {

    Product create(Long memberId, ProductCreateRequest createRequest, List<MultipartFile> images);

    Product update(Long myId, Long productId, ProductUpdateRequest request);
    Product delete(Long myId, Long productId);
    Product moderate(Long productId, ModerateProductRequest request);

	Product startAuction(Long myId, Long productId);
}
