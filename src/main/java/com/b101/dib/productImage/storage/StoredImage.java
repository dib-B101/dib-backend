package com.b101.dib.productImage.storage;

import org.springframework.core.io.Resource;

// 저장소에서 꺼낸 이미지 한 장. contentLength 가 음수면 길이를 모른다는 뜻(청크 전송)
public record StoredImage(Resource body, String contentType, long contentLength) {
}
