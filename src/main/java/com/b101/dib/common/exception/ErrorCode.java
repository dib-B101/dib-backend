package com.b101.dib.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 공통
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "권한이 없습니다"),

    // 상품
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다"),
    PRODUCT_NOT_DELETABLE(HttpStatus.CONFLICT, "판매 완료된 상품은 삭제할 수 없습니다"),
    PRODUCT_ALREADY_DELETED(HttpStatus.NOT_FOUND, "이미 삭제된 상품입니다"),
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "카테고리를 찾을 수 없습니다"),
    INVALID_FILTER(HttpStatus.BAD_REQUEST, "필터 값이 올바르지 않습니다"),
    INVALID_CURSOR(HttpStatus.BAD_REQUEST, "커서 값이 올바르지 않습니다"),

    // 경매
    AUCTION_NOT_FOUND(HttpStatus.NOT_FOUND, "경매를 찾을 수 없습니다"),
    AUCTION_ALREADY_DELETED(HttpStatus.NOT_FOUND, "이미 삭제된 경매입니다"),
    AUCTION_NOT_EDITABLE(HttpStatus.CONFLICT, "SCHEDULED 상태의 경매만 수정하거나 삭제할 수 있습니다"),
    AUCTION_SCHEDULE_INVALID(HttpStatus.BAD_REQUEST, "경매 시작 및 종료 시간이 올바르지 않습니다"),
    AUCTION_STARTED(HttpStatus.CONFLICT, "이미 시작된 경매입니다"),
    VERSION_CONFLICT(HttpStatus.CONFLICT, "경매 정보가 변경되었습니다. 최신 정보를 다시 조회해 주세요"),
    PRODUCT_NOT_OWNED(HttpStatus.FORBIDDEN, "본인 소유의 상품만 경매에 등록할 수 있습니다"),
    INVALID_AUCTION(HttpStatus.BAD_REQUEST, "일반 경매 요청이 올바르지 않습니다"),
    
    // 회원 가입 및 로그인
    INVALID_EMAIL(HttpStatus.BAD_REQUEST, "이메일 형식이 올바르지 않습니다."),
    EMAIL_DUPLICATED(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    PHONE_DUPLICATED(HttpStatus.CONFLICT, "이미 사용 중인 전화번호입니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."),

    // 본인 인증
    INVALID_VERIFICATION(HttpStatus.BAD_REQUEST, "유효하지 않은 인증입니다."),
    INVALID_CODE(HttpStatus.BAD_REQUEST, "인증번호가 올바르지 않습니다."),
    VERIFICATION_EXPIRED(HttpStatus.BAD_REQUEST, "인증번호가 만료되었습니다."),
    ATTEMPTS_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "인증 시도 횟수를 초과했습니다."),

    // 계정 상태
    ACCOUNT_SUSPENDED(HttpStatus.FORBIDDEN, "정지된 계정입니다."),
    ACCOUNT_BLOCKED(HttpStatus.FORBIDDEN, "사용할 수 없는 계정입니다."),

    // 회원
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String message;

}
