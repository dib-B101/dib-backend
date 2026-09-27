package com.b101.dib.category.query.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.b101.dib.product.domain.ProductAttributeCatalog;
import com.b101.dib.product.domain.ProductAttributeSpec;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class CategoryQueryDto {
    private Long categoryId;
    private String name;

    public List<ProductAttributeSpec> getAttributeSpecs() {
        return ProductAttributeCatalog.forCategory(name);
    }
}
