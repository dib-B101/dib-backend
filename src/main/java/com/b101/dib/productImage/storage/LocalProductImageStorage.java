package com.b101.dib.productImage.storage;

import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

// 컨테이너 로컬 디스크. Pod 가 1대일 때만 맞다 — 2대부터는 업로드한 Pod 에만 파일이 있어서
// 다른 Pod 로 간 조회 요청이 404 가 되고, Pod 가 재시작하면 통째로 사라진다. 배포에서는 S3 를 쓴다.
public class LocalProductImageStorage extends AbstractProductImageStorage {

    private final Path root;

    public LocalProductImageStorage(String imageDirectory, String internalBaseUrl) {
        super(internalBaseUrl);
        this.root = Path.of(imageDirectory).toAbsolutePath().normalize();
        try {
            Files.createDirectories(root);
        } catch (IOException exception) {
            throw new IllegalStateException("상품 이미지 저장 디렉터리를 만들 수 없습니다: " + root, exception);
        }
    }

    @Override
    protected void write(MultipartFile image, String fileName, String contentType) {
        try (InputStream input = image.getInputStream()) {
            Files.copy(input, root.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.IMAGE_STORAGE_FAILED);
        }
    }

    @Override
    protected void delete(String fileName) {
        try {
            Files.deleteIfExists(root.resolve(fileName));
        } catch (IOException ignored) {
            // DB 삭제를 막지 않는다. 남은 파일은 디스크를 조금 쓸 뿐이다
        }
    }

    @Override
    protected StoredImage read(String fileName, String contentType) {
        Path path = root.resolve(fileName);
        if (!Files.isReadable(path)) return null;
        try {
            return new StoredImage(new FileSystemResource(path), contentType, Files.size(path));
        } catch (IOException exception) {
            return null;
        }
    }
}
