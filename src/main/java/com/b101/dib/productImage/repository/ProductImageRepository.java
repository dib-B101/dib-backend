package com.b101.dib.productImage.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.b101.dib.productImage.domain.ProductImage;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long>{

	List<ProductImage> findAllByProductId(Long productId);

}
