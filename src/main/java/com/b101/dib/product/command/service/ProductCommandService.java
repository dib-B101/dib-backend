package com.b101.dib.product.command.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.b101.dib.product.command.dto.CreateRequest;
import com.b101.dib.product.command.dto.UpdateRequest;
import com.b101.dib.product.domain.Product;

public interface ProductCommandService {

    Product create(Long memberId, CreateRequest createRequest, List<MultipartFile> images);

    Product update(Long myId, Long productId, UpdateRequest request);
    Product delete(Long myId, Long productId); 
}
