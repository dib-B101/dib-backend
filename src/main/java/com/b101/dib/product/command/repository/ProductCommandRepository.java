package com.b101.dib.product.command.repository;

import com.b101.dib.product.command.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductCommandRepository extends JpaRepository<Product, Long> {
}
