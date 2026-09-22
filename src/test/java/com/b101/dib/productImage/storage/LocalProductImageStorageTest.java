package com.b101.dib.productImage.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LocalProductImageStorageTest {

    @TempDir Path tempDirectory;

    private LocalProductImageStorage storage() {
        return new LocalProductImageStorage(tempDirectory.toString(), "http://backend:8080/");
    }

    private static MockMultipartFile jpeg() {
        return new MockMultipartFile("images", "camera.jpg", "image/jpeg",
                new byte[] {(byte) 0xff, (byte) 0xd8, (byte) 0xff, 0x00});
    }

    @Test
    void storesServableRelativeUrlAndDeletesOwnedFile() {
        LocalProductImageStorage storage = storage();

        String url = storage.storeAll(List.of(jpeg())).get(0);
        Path saved = tempDirectory.resolve(url.substring(ProductImageStorage.PUBLIC_PATH.length()));

        assertThat(url).startsWith(ProductImageStorage.PUBLIC_PATH).endsWith(".jpg");
        assertThat(Files.exists(saved)).isTrue();
        assertThat(storage.internalUrl(url)).isEqualTo("http://backend:8080" + url);

        storage.deleteAll(List.of(url));
        assertThat(Files.exists(saved)).isFalse();
    }

    @Test
    void loadsStoredFileByName() {
        LocalProductImageStorage storage = storage();
        String url = storage.storeAll(List.of(jpeg())).get(0);
        String fileName = url.substring(ProductImageStorage.PUBLIC_PATH.length());

        StoredImage loaded = storage.load(fileName);

        assertThat(loaded).isNotNull();
        assertThat(loaded.contentType()).isEqualTo("image/jpeg");
        assertThat(loaded.contentLength()).isEqualTo(4);
    }

    @Test
    void returnsNullForUnknownOrUnsafeFileName() {
        LocalProductImageStorage storage = storage();

        assertThat(storage.load("00000000-0000-0000-0000-000000000000.jpg")).isNull();
        // 경로 조작은 파일명 검사에서 걸러진다 — 저장 디렉터리 밖은 못 읽는다
        assertThat(storage.load("../../application.yaml")).isNull();
        assertThat(storage.load("camera.sh")).isNull();
    }
}
