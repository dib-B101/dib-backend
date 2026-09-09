package com.b101.dib.product.command.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.b101.dib.product.command.dto.CreateRequest;
import com.b101.dib.product.command.dto.UpdateRequest;

public interface ProductCommandService {

    Long create(Long memberId, CreateRequest createRequest, List<MultipartFile> images);

    void update(Long memberId, Long productId, UpdateRequest request);
    void delete(Long memberId, Long productId); 
}
