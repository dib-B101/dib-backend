package com.b101.dib.auth.command.dto;

public enum PhoneVerificationPurpose {
    SIGN_UP, // 회원가입
    FIND_EMAIL, // 이메일 찾기
    RESET_PASSWORD, // 비밀번호 재설정
    CHANGE_SENSITIVE // 개인정보 변경
}
