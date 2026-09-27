package com.b101.dib.member.command.service;

import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import com.b101.dib.member.command.dto.UpdateProfileRequest;
import com.b101.dib.member.command.dto.UpdateProfileResponse;
import com.b101.dib.member.repository.MemberRepository;
import com.b101.dib.productImage.storage.ProductImageStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class MemberProfileImageService {
    private static final long MAX_IMAGE_BYTES = 10L * 1024L * 1024L;

    private final MemberRepository memberRepository;
    private final MemberCommandService memberCommandService;
    private final ProductImageStorage imageStorage;

    public UpdateProfileResponse updateImage(Long memberId, String nickname, MultipartFile image) {
        validateImage(image);
        String previousImageUrl = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND))
                .getProfileImageUrl();
        String imageUrl = imageStorage.storeAll(List.of(image)).getFirst();
        try {
            UpdateProfileResponse result = memberCommandService.updateProfile(
                    memberId, new UpdateProfileRequest(nickname, imageUrl));
            if (previousImageUrl != null) imageStorage.deleteAll(List.of(previousImageUrl));
            return result;
        } catch (RuntimeException exception) {
            imageStorage.deleteAll(List.of(imageUrl));
            throw exception;
        }
    }

    private void validateImage(MultipartFile image) {
        if (image == null || image.isEmpty()) throw new BusinessException(ErrorCode.IMAGE_REQUIRED);
        if (image.getSize() > MAX_IMAGE_BYTES) throw new BusinessException(ErrorCode.FILE_TOO_LARGE);
        String type = image.getContentType();
        if (type == null) throw new BusinessException(ErrorCode.INVALID_CONTENT_TYPE);
        String normalizedType = type.toLowerCase(Locale.ROOT);
        if (normalizedType.equals("image/jpg")) normalizedType = "image/jpeg";
        try (InputStream input = image.getInputStream()) {
            byte[] header = input.readNBytes(12);
            boolean valid = switch (normalizedType) {
                case "image/jpeg" -> header.length >= 3 && (header[0] & 0xff) == 0xff
                        && (header[1] & 0xff) == 0xd8 && (header[2] & 0xff) == 0xff;
                case "image/png" -> header.length >= 8 && Arrays.equals(Arrays.copyOf(header, 8),
                        new byte[] {(byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a});
                case "image/webp" -> header.length >= 12 && new String(header, 0, 4).equals("RIFF")
                        && new String(header, 8, 4).equals("WEBP");
                default -> false;
            };
            if (!valid) throw new BusinessException(ErrorCode.INVALID_CONTENT_TYPE);
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.INVALID_CONTENT_TYPE);
        }
    }
}
