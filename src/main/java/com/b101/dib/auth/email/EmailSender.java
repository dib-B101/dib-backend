package com.b101.dib.auth.email;

public interface EmailSender {

    /** 비밀번호 재설정 링크를 이메일로 전송한다. */
    void sendPasswordResetLink(String email, String resetLink);
}
