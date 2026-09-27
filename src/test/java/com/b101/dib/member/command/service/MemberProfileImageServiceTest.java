package com.b101.dib.member.command.service;

import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.member.command.dto.UpdateProfileRequest;
import com.b101.dib.member.command.dto.UpdateProfileResponse;
import com.b101.dib.member.domain.Member;
import com.b101.dib.member.repository.MemberRepository;
import com.b101.dib.productImage.storage.ProductImageStorage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MemberProfileImageServiceTest {
    @Mock MemberRepository memberRepository;
    @Mock MemberCommandService memberCommandService;
    @Mock ProductImageStorage imageStorage;

    @Test
    void uploadsAndReplacesProfileImage() {
        var image = jpeg();
        var member = new Member();
        member.setProfileImageUrl(ProductImageStorage.PUBLIC_PATH + "old.jpg");
        var newUrl = ProductImageStorage.PUBLIC_PATH + "new.jpg";
        var response = new UpdateProfileResponse(1L, "새닉네임", newUrl, LocalDateTime.now());
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(imageStorage.storeAll(List.of(image))).willReturn(List.of(newUrl));
        given(memberCommandService.updateProfile(1L, new UpdateProfileRequest("새닉네임", newUrl)))
                .willReturn(response);

        assertThat(new MemberProfileImageService(memberRepository, memberCommandService, imageStorage)
                .updateImage(1L, "새닉네임", image)).isEqualTo(response);
        verify(imageStorage).deleteAll(List.of(ProductImageStorage.PUBLIC_PATH + "old.jpg"));
    }

    @Test
    void rejectsIncorrectImageContentBeforeWriting() {
        var image = new MockMultipartFile("image", "fake.jpg", "image/jpeg", "not a jpeg".getBytes());

        assertThatThrownBy(() -> new MemberProfileImageService(memberRepository, memberCommandService, imageStorage)
                .updateImage(1L, null, image)).isInstanceOf(BusinessException.class);
        verify(imageStorage, never()).storeAll(List.of(image));
    }

    @Test
    void removesNewImageWhenProfileUpdateFails() {
        var image = jpeg();
        var member = new Member();
        var newUrl = ProductImageStorage.PUBLIC_PATH + "new.jpg";
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(imageStorage.storeAll(List.of(image))).willReturn(List.of(newUrl));
        given(memberCommandService.updateProfile(1L, new UpdateProfileRequest(null, newUrl)))
                .willThrow(new IllegalStateException("update failed"));

        assertThatThrownBy(() -> new MemberProfileImageService(memberRepository, memberCommandService, imageStorage)
                .updateImage(1L, null, image)).isInstanceOf(IllegalStateException.class);
        verify(imageStorage).deleteAll(List.of(newUrl));
    }

    private MockMultipartFile jpeg() {
        return new MockMultipartFile("image", "profile.jpg", "image/jpeg",
                new byte[] {(byte) 0xff, (byte) 0xd8, (byte) 0xff, 0x00});
    }
}
