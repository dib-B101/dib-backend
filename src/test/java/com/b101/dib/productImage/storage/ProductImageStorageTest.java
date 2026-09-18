package com.b101.dib.productImage.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ProductImageStorageTest {

    @TempDir Path tempDirectory;

    @Test
    void storesServableRelativeUrlAndDeletesOwnedFile() {
        ProductImageStorage storage = new ProductImageStorage(
                tempDirectory.toString(), "http://backend:8080/");
        MockMultipartFile image = new MockMultipartFile(
                "images", "camera.jpg", "image/jpeg",
                new byte[] {(byte) 0xff, (byte) 0xd8, (byte) 0xff, 0x00});

        String url = storage.storeAll(List.of(image)).get(0);
        Path saved = tempDirectory.resolve(url.substring(ProductImageStorage.PUBLIC_PATH.length()));

        assertThat(url).startsWith(ProductImageStorage.PUBLIC_PATH).endsWith(".jpg");
        assertThat(Files.exists(saved)).isTrue();
        assertThat(storage.internalUrl(url)).isEqualTo("http://backend:8080" + url);

        storage.deleteAll(List.of(url));
        assertThat(Files.exists(saved)).isFalse();
    }
}
