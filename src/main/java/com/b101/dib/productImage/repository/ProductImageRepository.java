package com.b101.dib.productImage.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.b101.dib.productImage.domain.ProductImage;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long>{

}
