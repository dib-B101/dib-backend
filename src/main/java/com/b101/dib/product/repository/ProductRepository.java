package com.b101.dib.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.b101.dib.product.domain.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
}

