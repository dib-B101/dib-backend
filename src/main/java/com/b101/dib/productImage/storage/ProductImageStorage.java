package com.b101.dib.productImage.storage;

import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;
import java.util.List;

// 상품 이미지 저장소. 구현은 두 가지다.
//   local — 컨테이너 로컬 디스크. 단일 인스턴스(IDE·compose) 전용
//   s3    — 공용 버킷. Pod 가 2대 이상이면 반드시 이쪽 (dib.storage.provider=s3)
// 저장 위치가 바뀌어도 공개 URL 은 PUBLIC_PATH 로 같다. 프론트·AI 검수 계약이 안 바뀐다.
public interface ProductImageStorage {

    String PUBLIC_PATH = "/api/v1/product-images/files/";

    // 전부 저장하고 공개 URL 목록을 반환. 중간에 실패하면 이미 올린 것을 지우고 던진다
    List<String> storeAll(List<MultipartFile> images);

    void deleteAll(Collection<String> imageUrls);

    // AI 검수 서버가 받아갈 수 있는 절대 URL. 이미 절대 URL 이면 그대로 둔다
    String internalUrl(String imageUrl);

    // 저장된 파일. 없으면 null
    StoredImage load(String fileName);
}
