package com.b101.dib.product.command.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.b101.dib.product.domain.Product;

public interface ProductCommandRepository extends JpaRepository<Product, Long> {
}

