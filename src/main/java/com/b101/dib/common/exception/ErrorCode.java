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
    
    // 검색
    INVALID_SEARCH(HttpStatus.BAD_REQUEST, "검색 조건과 키워드가 유효하지 않습니다"),
    
    // 북마크(찜)
    BOOKMARK_NOT_FOUND(HttpStatus.NOT_FOUND, "북마크를 찾을 수 없습니다"),
    BOOKMARK_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "북마크가 이미 존재합니다"),

    // 경매
    AUCTION_NOT_FOUND(HttpStatus.NOT_FOUND, "경매를 찾을 수 없습니다"),
    AUCTION_ALREADY_DELETED(HttpStatus.NOT_FOUND, "이미 삭제된 경매입니다"),
    AUCTION_NOT_EDITABLE(HttpStatus.CONFLICT, "SCHEDULED 상태의 경매만 수정하거나 삭제할 수 있습니다"),
    AUCTION_SCHEDULE_INVALID(HttpStatus.BAD_REQUEST, "경매 시작 및 종료 시간이 올바르지 않습니다"),
    AUCTION_STARTED(HttpStatus.CONFLICT, "이미 시작된 경매입니다"),
    AUCTION_NOT_ACTIVE(HttpStatus.CONFLICT, "진행 중인 경매가 아닙니다"),
    BID_TOO_LOW(HttpStatus.BAD_REQUEST, "입찰 금액이 최소 입찰가보다 낮습니다"),
    BID_AMOUNT_TAKEN(HttpStatus.CONFLICT, "같은 금액의 입찰이 먼저 들어왔습니다"),
    SELLER_CANNOT_BID(HttpStatus.FORBIDDEN, "판매자는 자신의 경매에 입찰할 수 없습니다"),
    ALREADY_HIGHEST_BIDDER(HttpStatus.CONFLICT, "이미 최고 입찰자입니다"),
    VERSION_CONFLICT(HttpStatus.CONFLICT, "경매 정보가 변경되었습니다. 최신 정보를 다시 조회해 주세요"),
    PRODUCT_NOT_OWNED(HttpStatus.FORBIDDEN, "본인 소유의 상품만 경매에 등록할 수 있습니다"),
    INVALID_AUCTION(HttpStatus.BAD_REQUEST, "일반 경매 요청이 올바르지 않습니다"),
    
    // 라이브 방송
    LIVE_BROADCAST_NO_TITLE(HttpStatus.BAD_REQUEST, "라이브 방송 제목은 필수입니다"),
    LIVE_BROADCAST_NOT_FOUND(HttpStatus.NOT_FOUND, "라이브 방송을 찾을 수 없습니다"),
    LIVE_BROADCAST_NOT_OWNED(HttpStatus.FORBIDDEN, "본인 소유의 라이브 방송이 아닙니다"),
    LIVE_BROADCAST_ALREADY_STARTED(HttpStatus.BAD_REQUEST, "라이브 방송이 이미 시작되었습니다"),
    LIVE_BROADCAST_ALREADY_ENDED(HttpStatus.BAD_REQUEST, "라이브 방송이 이미 끝났습니다"),
    LIVE_BROADCAST_ALREADY_CANCELED(HttpStatus.BAD_REQUEST, "라이브 방송이 취소되었습니다"),
    
    // 회원 가입 및 로그인
    INVALID_EMAIL(HttpStatus.BAD_REQUEST, "이메일 형식이 올바르지 않습니다."),
    EMAIL_DUPLICATED(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    PHONE_DUPLICATED(HttpStatus.CONFLICT, "이미 사용 중인 전화번호입니다."),
    NICKNAME_DUPLICATED(HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "비밀번호 정책을 충족하지 않습니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "로그인이 만료되었습니다."),
    SESSION_REVOKED(HttpStatus.UNAUTHORIZED, "다른 기기 로그인 또는 보안 변경으로 세션이 종료되었습니다."),
    DEVICE_MISMATCH(HttpStatus.BAD_REQUEST, "등록된 기기 정보와 일치하지 않습니다."),
    INVALID_RESET_TOKEN(HttpStatus.BAD_REQUEST, "비밀번호 재설정 링크가 유효하지 않습니다."),

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

    // 결제 2탄 (만료·차순위·환불·웹훅)
    OFFER_EXPIRED(HttpStatus.CONFLICT, "차순위 낙찰 제안 기한이 만료되었습니다."),
    REFUND_NOT_ALLOWED(HttpStatus.CONFLICT, "현재 상태에서는 환불할 수 없습니다."),
    INVALID_EVENT(HttpStatus.BAD_REQUEST, "이벤트 형식을 확인해 주세요."),

    // 배송
    PAYMENT_REQUIRED(HttpStatus.CONFLICT, "결제 완료 후 이용할 수 있습니다."),
    INVALID_TRACKING(HttpStatus.BAD_REQUEST, "택배사 또는 송장번호를 확인해주세요."),
    UNSUPPORTED_CARRIER(HttpStatus.BAD_REQUEST, "지원하지 않는 택배사입니다. GET /api/v1/carriers 목록에서 선택해주세요."),
    CHATTING_CLOSED(HttpStatus.CONFLICT, "종료된 거래의 채팅에는 메시지를 보낼 수 없습니다"),
    SHIPMENT_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 송장이 등록되었습니다."),
    SHIPMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "배송 정보를 찾을 수 없습니다."),
    ADDRESS_REQUIRED(HttpStatus.CONFLICT, "배송지를 먼저 입력해야 합니다."),
    ADDRESS_NOT_EDITABLE(HttpStatus.CONFLICT, "현재 상태에서는 배송지를 변경할 수 없습니다."),

    // 정산
    SETTLEMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "정산 정보를 찾을 수 없습니다."),
    SETTLEMENT_NOT_READY(HttpStatus.CONFLICT, "정산 가능한 상태가 아닙니다."),
    PAYOUT_FAILED(HttpStatus.BAD_GATEWAY, "판매자 지급에 실패했습니다."),
    ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, "정산 계좌가 등록되지 않았습니다."),
    ACCOUNT_VERIFICATION_FAILED(HttpStatus.BAD_REQUEST, "정산 계좌 확인에 실패했습니다."),

    // 회원
    INVALID_ADDRESS(HttpStatus.BAD_REQUEST, "배송지 정보를 확인해 주세요."),
    ADDRESS_NOT_FOUND(HttpStatus.NOT_FOUND, "배송지를 찾을 수 없습니다."),
    ACTIVE_ORDER_EXISTS(HttpStatus.CONFLICT, "진행 중인 주문이 있어 탈퇴할 수 없습니다."),
    ACTIVE_AUCTION_EXISTS(HttpStatus.CONFLICT, "진행 중인 입찰 또는 판매가 있어 탈퇴할 수 없습니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다.");
    private final HttpStatus status;
    private final String message;

}
