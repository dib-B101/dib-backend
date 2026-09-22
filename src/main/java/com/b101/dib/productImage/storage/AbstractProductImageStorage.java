package com.b101.dib.productImage.storage;

import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

// local / s3 구현이 공유하는 부분 — 파일명 생성, 확장자 검증, 공개 URL 조립, 실패 시 롤백.
// 실제 바이트를 어디에 쓰고 어디서 읽는지만 하위 클래스가 정한다.
abstract class AbstractProductImageStorage implements ProductImageStorage {

    private final String internalBaseUrl;

    protected AbstractProductImageStorage(String internalBaseUrl) {
        this.internalBaseUrl = internalBaseUrl.replaceAll("/+$", "");
    }

    @Override
    public List<String> storeAll(List<MultipartFile> images) {
        List<String> stored = new ArrayList<>();
        try {
            for (MultipartFile image : images) {
                String fileName = UUID.randomUUID() + extensionOf(image);
                write(image, fileName, contentTypeOf(fileName));
                stored.add(PUBLIC_PATH + fileName);
            }
            return List.copyOf(stored);
        } catch (RuntimeException exception) {
            deleteAll(stored);   // 일부만 올라간 상태로 두지 않는다
            throw exception;
        }
    }

    @Override
    public void deleteAll(Collection<String> imageUrls) {
        for (String imageUrl : imageUrls) {
            String fileName = fileNameOf(imageUrl);
            if (fileName == null) continue;
            delete(fileName);
        }
    }

    @Override
    public String internalUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank() || imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
            return imageUrl;
        }
        return internalBaseUrl + (imageUrl.startsWith("/") ? imageUrl : "/" + imageUrl);
    }

    @Override
    public StoredImage load(String fileName) {
        if (!isSafeFileName(fileName)) return null;
        return read(fileName, contentTypeOf(fileName));
    }

    protected abstract void write(MultipartFile image, String fileName, String contentType);

    // 실패해도 던지지 않는다. 저장소 정리 실패가 DB 삭제를 막으면 안 된다
    protected abstract void delete(String fileName);

    // 없으면 null
    protected abstract StoredImage read(String fileName, String contentType);

    // 우리가 붙인 PUBLIC_PATH 로 시작하는 URL 에서만 파일명을 뽑는다.
    // 외부 절대 URL(구 데이터)이나 경로 조작 시도는 null 로 걸러진다
    private static String fileNameOf(String imageUrl) {
        if (imageUrl == null || !imageUrl.startsWith(PUBLIC_PATH)) return null;
        String fileName = imageUrl.substring(PUBLIC_PATH.length());
        return isSafeFileName(fileName) ? fileName : null;
    }

    private static boolean isSafeFileName(String fileName) {
        return fileName != null && fileName.matches("[A-Za-z0-9-]+\\.(jpg|png|webp)");
    }

    private static String extensionOf(MultipartFile image) {
        String contentType = image.getContentType();
        if (contentType == null) throw new BusinessException(ErrorCode.INVALID_CONTENT_TYPE);
        return switch (contentType.toLowerCase(Locale.ROOT)) {
            case "image/jpeg", "image/jpg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> throw new BusinessException(ErrorCode.INVALID_CONTENT_TYPE);
        };
    }

    private static String contentTypeOf(String fileName) {
        if (fileName.endsWith(".png")) return "image/png";
        if (fileName.endsWith(".webp")) return "image/webp";
        return "image/jpeg";
    }
}
