package com.b101.dib.productImage.storage;

import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.io.InputStream;

// 공용 S3 버킷. Pod 가 몇 대로 늘어도 같은 파일을 본다.
// 버킷은 퍼블릭 액세스가 막혀 있으므로 조회는 백엔드가 대신 읽어 스트리밍한다
// (ProductImageFileController). 그래서 공개 URL 이 local 구현과 똑같이 유지된다.
@Slf4j
public class S3ProductImageStorage extends AbstractProductImageStorage {

    private final S3Client s3Client;
    private final String bucket;
    private final String keyPrefix;

    public S3ProductImageStorage(S3Client s3Client, String bucket, String keyPrefix, String internalBaseUrl) {
        super(internalBaseUrl);
        if (bucket == null || bucket.isBlank()) {
            // 빈 버킷으로 떠 봐야 상품 등록에서만 터진다. 기동 때 알아차리는 편이 낫다
            throw new IllegalStateException(
                    "dib.storage.provider=s3 인데 dib.storage.s3.bucket(DIB_S3_BUCKET)이 비어 있습니다");
        }
        this.s3Client = s3Client;
        this.bucket = bucket;
        this.keyPrefix = keyPrefix.isBlank() || keyPrefix.endsWith("/") ? keyPrefix : keyPrefix + "/";
    }

    @Override
    protected void write(MultipartFile image, String fileName, String contentType) {
        try (InputStream input = image.getInputStream()) {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key(fileName))
                            .contentType(contentType)
                            .contentLength(image.getSize())
                            .build(),
                    RequestBody.fromInputStream(input, image.getSize()));
        } catch (IOException | S3Exception exception) {
            log.error("S3 이미지 업로드 실패 bucket={} key={}", bucket, key(fileName), exception);
            throw new BusinessException(ErrorCode.IMAGE_STORAGE_FAILED);
        }
    }

    @Override
    protected void delete(String fileName) {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key(fileName)).build());
        } catch (S3Exception exception) {
            // DB 삭제를 막지 않는다. 남은 객체는 버킷 수명주기 규칙으로 정리한다
            log.warn("S3 이미지 삭제 실패 bucket={} key={}", bucket, key(fileName), exception);
        }
    }

    @Override
    protected StoredImage read(String fileName, String contentType) {
        try {
            ResponseInputStream<GetObjectResponse> object = s3Client.getObject(
                    GetObjectRequest.builder().bucket(bucket).key(key(fileName)).build());
            GetObjectResponse response = object.response();
            return new StoredImage(
                    new InputStreamResource(object),
                    response.contentType() == null ? contentType : response.contentType(),
                    response.contentLength() == null ? -1 : response.contentLength());
        } catch (NoSuchKeyException exception) {
            return null;
        }
    }

    private String key(String fileName) {
        return keyPrefix + fileName;
    }
}
