package com.b101.dib.productImage.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

// dib.storage.provider 로 저장소를 고른다. 기본은 local (IDE·docker compose).
// 배포는 s3 — Pod 가 2~6대로 오르내리므로 로컬 디스크는 답이 없다.
@Configuration
public class ProductImageStorageConfig {

    @Bean
    @ConditionalOnProperty(name = "dib.storage.provider", havingValue = "local", matchIfMissing = true)
    public ProductImageStorage localProductImageStorage(
            @Value("${dib.storage.product-image-dir:./data/product-images}") String imageDirectory,
            @Value("${dib.storage.internal-base-url:${dib.ai.callback-base-url:http://localhost:8080}}")
            String internalBaseUrl
    ) {
        return new LocalProductImageStorage(imageDirectory, internalBaseUrl);
    }

    // 자격증명은 Secret 이 아니라 IRSA(ServiceAccount dib-backend) 로 받는다 — 기본 제공자 체인이 집어간다
    @Bean
    @ConditionalOnProperty(name = "dib.storage.provider", havingValue = "s3")
    public S3Client productImageS3Client(@Value("${dib.storage.s3.region:ap-northeast-2}") String region) {
        return S3Client.builder().region(Region.of(region)).build();
    }

    @Bean
    @ConditionalOnProperty(name = "dib.storage.provider", havingValue = "s3")
    public ProductImageStorage s3ProductImageStorage(
            S3Client productImageS3Client,
            @Value("${dib.storage.s3.bucket}") String bucket,
            @Value("${dib.storage.s3.key-prefix:products/}") String keyPrefix,
            @Value("${dib.storage.internal-base-url:${dib.ai.callback-base-url:http://localhost:8080}}")
            String internalBaseUrl
    ) {
        return new S3ProductImageStorage(productImageS3Client, bucket, keyPrefix, internalBaseUrl);
    }
}
