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
    PRODUCT_PENDING(HttpStatus.BAD_REQUEST, "상품이 아직 심사 중입니다."),
    PRODUCT_ON_AUCTION(HttpStatus.BAD_REQUEST, "이미 경매가 진행 중인 상품입니다."),
    PRODUCT_ALREADY_SOLD(HttpStatus.CONFLICT, "판매 완료된 상품은 삭제할 수 없습니다"),
    PRODUCT_ALREADY_DELETED(HttpStatus.NOT_FOUND, "이미 삭제된 상품입니다"),
    NOT_MY_PRODUCT(HttpStatus.FORBIDDEN, "내가 등록한 상품이 아닙니다."),
    PRODUCT_MODERATION_NOT_ALLOWED(HttpStatus.CONFLICT, "검수 대기 상품만 승인 또는 거부할 수 있습니다"),
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "카테고리를 찾을 수 없습니다"),
    INVALID_FILTER(HttpStatus.BAD_REQUEST, "필터 값이 올바르지 않습니다"),
    INVALID_CURSOR(HttpStatus.BAD_REQUEST, "커서 값이 올바르지 않습니다"),
    TOO_MUCH_IMAGES(HttpStatus.BAD_REQUEST, "이미지는 10장까지만 등록할 수 있습니다."),

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
    NICKNAME_DUPLICATED(HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "비밀번호 정책을 충족하지 않습니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."),

    // 본인 인증
    INVALID_PHONE(HttpStatus.BAD_REQUEST, "휴대전화번호 형식이 올바르지 않습니다."),
    RATE_LIMITED(HttpStatus.TOO_MANY_REQUESTS, "요청이 너무 많습니다."),
    INVALID_VERIFICATION_ID(HttpStatus.BAD_REQUEST, "휴대전화 인증 요청 정보가 올바르지 않습니다."),
    INVALID_VERIFICATION(HttpStatus.BAD_REQUEST, "유효하지 않은 인증입니다."),
    INVALID_CODE(HttpStatus.BAD_REQUEST, "인증번호가 올바르지 않습니다."),
    VERIFICATION_EXPIRED(HttpStatus.GONE, "인증번호가 만료되었습니다."),
    ATTEMPTS_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "인증 시도 횟수를 초과했습니다."),

    // 계정 상태
    ACCOUNT_SUSPENDED(HttpStatus.FORBIDDEN, "정지된 계정입니다."),
    ACCOUNT_BLOCKED(HttpStatus.FORBIDDEN, "사용할 수 없는 계정입니다."),

    // 문의
    QUESTION_NOT_FOUND(HttpStatus.NOT_FOUND, "문의를 찾을 수 없습니다"),
    
    // 신고
    SELF_REPORT_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "본인 또는 본인 상품은 신고할 수 없습니다"),
    DUPLICATE_REPORT(HttpStatus.CONFLICT, "이미 신고한 대상입니다"),
    REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "신고를 찾을 수 없습니다"),

    // 이상입찰
    DUPLICATE_LABEL(HttpStatus.CONFLICT, "이미 라벨이 확정된 입찰자입니다"),

    // 주문
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다"),
    DELIVERY_NOT_COMPLETED(HttpStatus.CONFLICT, "배송이 완료된 주문만 구매 확정할 수 있습니다"),
    ALREADY_CONFIRMED(HttpStatus.CONFLICT, "이미 구매 확정된 주문입니다"),
    NO_WINNING_BID(HttpStatus.CONFLICT, "낙찰자가 없는 경매입니다"),
    DUPLICATE_ORDER(HttpStatus.CONFLICT, "이미 주문이 생성된 경매입니다"),

    // 결제수단
    PAYMENT_METHOD_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 등록된 결제수단이 있습니다."),
    BILLING_KEY_ISSUE_FAILED(HttpStatus.BAD_GATEWAY, "결제수단 등록에 실패했습니다."),
    PAYMENT_METHOD_NOT_FOUND(HttpStatus.NOT_FOUND, "등록된 결제수단을 찾을 수 없습니다."),

    // 결제
    DUPLICATE_PAYMENT(HttpStatus.CONFLICT, "이미 처리된 결제입니다."),
    PAYMENT_DEADLINE_EXPIRED(HttpStatus.CONFLICT, "결제 기한이 만료되었습니다."),
    TOSS_CONFIRM_FAILED(HttpStatus.BAD_GATEWAY, "결제 승인에 실패했습니다."),
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "결제 정보를 찾을 수 없습니다."),

    // 회원
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다.");
    private final HttpStatus status;
    private final String message;

}
