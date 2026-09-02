package com.b101.dib.product.command.service;

import com.b101.dib.product.command.dto.CreateRequest;
import com.b101.dib.product.command.dto.UpdateRequest;

public interface ProductCommandService {

    Long create(CreateRequest createRequest);

    void update(Long productId, UpdateRequest request);
}
