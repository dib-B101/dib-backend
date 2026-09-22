package com.b101.dib.productImage.query.controller;

import com.b101.dib.productImage.storage.ProductImageStorage;
import com.b101.dib.productImage.storage.StoredImage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

// 저장된 상품 이미지를 내려준다. 예전에는 정적 리소스 핸들러가 로컬 디렉터리를 바로 노출했는데,
// S3 로 옮기면서 저장소 구현에 위임하도록 바꿨다. 경로는 그대로라 프론트·AI 는 영향 없다.
// 파일명이 UUID 라 열람 권한은 따로 두지 않는다(기존과 동일).
@RestController
@RequiredArgsConstructor
public class ProductImageFileController {

    private final ProductImageStorage productImageStorage;

    @GetMapping(ProductImageStorage.PUBLIC_PATH + "{fileName}")
    public ResponseEntity<Resource> find(@PathVariable("fileName") String fileName) {
        StoredImage image = productImageStorage.load(fileName);
        if (image == null) {
            return ResponseEntity.notFound().build();
        }
        ResponseEntity.BodyBuilder response = ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(image.contentType()))
                // 파일명이 UUID 라 내용이 바뀌지 않는다 — 앱이 재다운로드하지 않게 길게 준다
                .cacheControl(CacheControl.maxAge(Duration.ofDays(30)).cachePublic());
        if (image.contentLength() >= 0) {
            response.contentLength(image.contentLength());
        }
        return response.body(image.body());
    }
}
