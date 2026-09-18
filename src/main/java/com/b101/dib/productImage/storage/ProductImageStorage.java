package com.b101.dib.productImage.storage;

import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Component
public class ProductImageStorage {

    public static final String PUBLIC_PATH = "/api/v1/product-images/files/";

    private final Path root;
    private final String internalBaseUrl;

    public ProductImageStorage(
            @Value("${dib.storage.product-image-dir:./data/product-images}") String imageDirectory,
            @Value("${dib.storage.internal-base-url:${dib.ai.callback-base-url:http://localhost:8080}}")
            String internalBaseUrl
    ) {
        this.root = Path.of(imageDirectory).toAbsolutePath().normalize();
        this.internalBaseUrl = internalBaseUrl.replaceAll("/+$", "");
        try {
            Files.createDirectories(root);
        } catch (IOException exception) {
            throw new IllegalStateException("상품 이미지 저장 디렉터리를 만들 수 없습니다: " + root, exception);
        }
    }

    public List<String> storeAll(List<MultipartFile> images) {
        List<String> stored = new ArrayList<>();
        try {
            for (MultipartFile image : images) {
                stored.add(store(image));
            }
            return List.copyOf(stored);
        } catch (RuntimeException exception) {
            deleteAll(stored);
            throw exception;
        }
    }

    public void deleteAll(Collection<String> imageUrls) {
        for (String imageUrl : imageUrls) {
            Path path = storedPath(imageUrl);
            if (path == null) continue;
            try {
                Files.deleteIfExists(path);
            } catch (IOException ignored) {
                // DB 삭제를 막지 않는다. 운영 저장소에서는 별도 정리 작업으로 재시도한다.
            }
        }
    }

    public String internalUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank() || imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
            return imageUrl;
        }
        return internalBaseUrl + (imageUrl.startsWith("/") ? imageUrl : "/" + imageUrl);
    }

    Path root() {
        return root;
    }

    private String store(MultipartFile image) {
        String extension = switch (image.getContentType().toLowerCase(Locale.ROOT)) {
            case "image/jpeg", "image/jpg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> throw new BusinessException(ErrorCode.INVALID_CONTENT_TYPE);
        };
        String fileName = UUID.randomUUID() + extension;
        Path target = root.resolve(fileName).normalize();
        if (!target.getParent().equals(root)) {
            throw new BusinessException(ErrorCode.IMAGE_STORAGE_FAILED);
        }
        try (InputStream input = image.getInputStream()) {
            Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
            return PUBLIC_PATH + fileName;
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.IMAGE_STORAGE_FAILED);
        }
    }

    private Path storedPath(String imageUrl) {
        if (imageUrl == null || !imageUrl.startsWith(PUBLIC_PATH)) return null;
        String fileName = imageUrl.substring(PUBLIC_PATH.length());
        Path path = root.resolve(fileName).normalize();
        return path.getParent().equals(root) ? path : null;
    }
}
