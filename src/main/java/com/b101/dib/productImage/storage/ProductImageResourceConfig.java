package com.b101.dib.productImage.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
public class ProductImageResourceConfig implements WebMvcConfigurer {

    private final String resourceLocation;

    public ProductImageResourceConfig(
            @Value("${dib.storage.product-image-dir:./data/product-images}") String imageDirectory
    ) {
        String location = Path.of(imageDirectory).toAbsolutePath().normalize().toUri().toString();
        this.resourceLocation = location.endsWith("/") ? location : location + "/";
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(ProductImageStorage.PUBLIC_PATH + "**")
                .addResourceLocations(resourceLocation);
    }
}
