# Backend Development Harness

백엔드 개발과 코드 리뷰 과정에서 팀이 합의한 원칙을 기록한다.
새 원칙은 아래 모듈 양식을 복사해 하나의 독립된 항목으로 추가한다.

---

## [원칙 이름]

- **상태:** 제안 | 적용 | 폐기
- **적용 범위:** 이 원칙이 적용되는 코드, 계층 또는 상황
- **결정:** 반드시 지킬 내용을 한 문장으로 작성
- **이유:** 이 방식을 선택한 배경과 해결하려는 문제
- **예외:** 허용되는 예외와 예외 적용 조건. 없으면 `없음`
- **예시:** 권장 코드 또는 짧은 적용 사례

---

## CQRS 패키지는 Command/Query를 기준으로 먼저 분리한다

- **상태:** 적용
- **적용 범위:** CQRS를 적용하는 백엔드 도메인 및 기능 패키지
- **결정:** 패키지는 계층보다 Command/Query를 먼저 나누고, 각 패키지 안에서 Controller, Service, DTO 등의 계층을 구분한다.
- **이유:** 하나의 유스케이스에 관련된 코드를 가까이 배치해 Command와 Query의 응집도를 높이고, 기능이 커져도 변경 범위와 탐색 경로를 명확하게 유지하기 위함이다.
- **예외:** Command와 Query가 분리되지 않은 단순 CRUD 기능에는 적용하지 않을 수 있다. 예외를 적용할 때는 해당 기능의 코드 리뷰에서 팀의 합의를 거친다.
- **예시:**

  ```text
  auction/
  ├── command/
  │   ├── controller/
  │   ├── service/
  │   └── dto/
  ├── query/
  │   ├── controller/
  │   ├── service/
  │   └── dto/
  └── domain/
  ```

  `service/command`처럼 계층을 먼저 나누는 구조보다 `command/service`처럼 Command/Query를 먼저 나누는 구조를 사용한다.

---

## 테이블 단위 도메인 패키지

- **상태:** 제안
- **적용 범위:** `com.b101.dib` 하위 패키지 구조 전체. Entity, Repository, Mapper, DTO, Service, Controller 배치 기준.
- **결정:** DB 테이블 하나마다 도메인 패키지 하나를 만들고(`dib.<테이블명 camelCase>`), 그 테이블의 Entity·조회 DTO·Mapper는 반드시 해당 패키지 안에 둔다.
- **이유:** 패키지 이름만 보면 어느 테이블을 다루는지 바로 알 수 있고, "이 DTO는 어디에 만들지"를 매번 판단하지 않아도 된다. 자식 테이블 DTO가 부모 DTO의 중첩 static class로 박혀 `ProductDetailDto$ImageItem` 같은 `$` 참조가 XML에 등장하는 것을 없앤다. ERD와 패키지 트리가 1:1로 대응되므로 테이블이 추가·삭제될 때 코드에서 찾아야 할 위치가 정해져 있다.
- **예외:** 없음. 자기 API가 없는 자식 테이블(`product_image`, `bid`, `live_chatting` 등)도 패키지를 만들며, 이 경우 Controller·Service 없이 `query.dto`(필요하면 `command.entity`)만 둔다.
- **예시:**

  ```text
  com.b101.dib
  ├── product
  │   ├── command/entity/Product
  │   └── query/dto/ProductDetailDto
  └── productImage
      ├── command/entity/ProductImage
      └── query/dto/ProductImageDto
  ```

  ```java
  // product/query/dto/ProductDetailDto.java
  import com.b101.dib.productImage.query.dto.ProductImageDto;

  private List<ProductImageDto> images;
  ```

  ```xml
  <collection property="images" ofType="com.b101.dib.productImage.query.dto.ProductImageDto">
  ```

---

## 조회 DTO 평탄화

- **상태:** 제안
- **적용 범위:** Query 계층(MyBatis)의 조회 응답 DTO. 각 도메인의 `query.dto` 패키지. Command 계층의 Entity·Request DTO에는 적용하지 않는다.
- **결정:** 조회 DTO는 1:1 관계 컬럼을 중첩 객체 대신 접두어를 붙인 평면 필드로 두고, 1:N 관계만 `List<하위 테이블 DTO>`로 둔다.
- **이유:** 중첩 DTO는 `<resultMap>`에 `<association>`을 써야 하고, 필드명·`javaType` 오타가 나면 예외 없이 null이 조용히 들어가 디버깅이 어렵다. 평탄화하면 조인 SQL의 컬럼 별칭(`c.name AS category_name`)과 `map-underscore-to-camel-case`만으로 `resultType` 매핑이 끝나 XML이 짧아지고, SQL과 DTO만 보고 대응 관계를 바로 읽을 수 있다. REST API 명세서 응답 예시 대부분이 `{questionId, memberId, title, ...}` 같은 평면 구조라 API 계약과도 일치한다.
- **예외:**
  1. 1:N 관계(상품 이미지 목록 등)는 `List<>` + `<collection>`을 쓴다. 하위 DTO는 [테이블 단위 도메인 패키지]에 따라 해당 테이블 패키지의 `query.dto`에 두고, 그 내부는 평면으로 둔다.
  2. REST API 명세서 응답 예시가 명시적으로 중첩 구조인 경우(예: 50번 주문 상세 `{order, auction, product, payment?, settlement?}`) 명세를 따른다. 명세를 평면으로 바꾸는 것이 더 단순하면 명세 담당과 먼저 협의한다.
- **예시:**

  ```java
  @Getter @Setter @NoArgsConstructor
  public class ProductDetailDto {
      private Long productId;
      private String title;
      private Long categoryId;
      private String categoryName;
      private Long sellerId;
      private String sellerNickname;
      private Double sellerScore;
      private List<ProductImageDto> images;
  }
  ```

  ```sql
  SELECT p.product_id, p.title,
         c.category_id, c.name AS category_name,
         m.member_id AS seller_id, m.nickname AS seller_nickname, m.score AS seller_score
  FROM product p
  JOIN category c ON c.category_id = p.category_id
  JOIN member   m ON m.member_id   = p.member_id
  ```

---

## Entity와 enum은 domain 패키지에 둔다

- **상태:** 적용
- **적용 범위:** 모든 도메인 패키지의 JPA Entity와 그 Entity가 쓰는 enum(`ProductStatus`, `AuctionType` 등)
- **결정:** Entity와 enum은 `command`·`query` 어느 쪽에도 넣지 않고 도메인 루트의 `domain` 패키지에 둔다.
- **이유:** enum은 Command(Entity 필드)와 Query(조회 DTO 필드) 양쪽에서 쓰인다. `command/entity`나 `query/dto` 아래에 두면 반대쪽이 상대 패키지를 import하게 되어 Command/Query 분리가 흐려진다. 테이블의 모양(Entity)과 값 목록(enum)은 어느 유스케이스에도 속하지 않는 "도메인 그 자체"이므로 중립 위치에 둔다.
- **예외:** 없음
- **예시:**

  ```text
  product/
  ├── domain/
  │   ├── Product.java            ← @Entity
  │   ├── ProductStatus.java      ← enum
  │   └── ProductCondition.java   ← enum
  ├── command/
  └── query/
  ```

  ```java
  // query/dto/ProductDetailDto.java
  import com.b101.dib.product.domain.ProductStatus;
  ```

---

## JPA Repository와 MyBatis Mapper는 repository 패키지에 함께 둔다

- **상태:** 적용
- **적용 범위:** 모든 도메인의 데이터 접근 인터페이스
- **결정:** `XxxRepository`(JpaRepository, Command용)와 `XxxMapper`(MyBatis, Query용)는 도메인 루트의 `repository` 패키지에 나란히 두고, XML은 `resources/mappers/XxxMapper.xml`, `namespace`는 `com.b101.dib.<도메인>.repository.XxxMapper`로 맞춘다.
- **이유:** "이 테이블에 접근하는 코드"가 한 폴더에 모여 있어야 SQL이 어디서 나가는지 한눈에 찾는다. `command/repository`, `query/mapper`로 흩어 두면 같은 테이블을 만지는 두 파일이 멀리 떨어지고, XML의 `namespace`가 Java 패키지 이동을 따라가지 못해 `Invalid bound statement`가 나기 쉽다. 저장소는 유스케이스가 아니라 테이블에 속한다.
- **예외:** 없음
- **예시:**

  ```text
  auction/
  └── repository/
      ├── AuctionRepository.java   extends JpaRepository<Auction, Long>
      └── AuctionMapper.java       @Mapper
  resources/mappers/AuctionMapper.xml
  ```

  ```xml
  <mapper namespace="com.b101.dib.auction.repository.AuctionMapper">
  ```

  Mapper 클래스를 옮기면 XML `namespace`도 같은 커밋에서 바꾼다.

---

## 조회 DTO는 목록용 QueryDto와 상세용 DetailDto로 나눈다

- **상태:** 적용
- **적용 범위:** `query/dto` 패키지의 MyBatis 조회 결과 DTO
- **결정:** 목록 응답은 `XxxQueryDto`, 단건 상세 응답은 `XxxDetailDto`로 이름을 고정하고, 둘 다 `@Getter @Setter @NoArgsConstructor`만 붙인다. API 응답 하나에 DTO 하나를 둔다.
- **이유:** 목록과 상세는 명세의 응답 필드가 다르다(목록은 가볍게, 상세는 조인 포함). 하나로 합치면 목록에서 안 읽은 필드가 null로 나가 프론트가 "없는 값인지 안 준 값인지" 구분하지 못한다. `@Setter @NoArgsConstructor`는 MyBatis가 빈 객체를 만들고 setter로 채우기 때문에 필수이고, `@Builder`는 MyBatis가 만들어 주는 객체라 필요 없다.
- **예외:** 명세의 목록 응답과 상세 응답 필드가 완전히 같으면 `QueryDto` 하나로 둘 수 있다.
- **예시:**

  ```text
  product/query/dto/
  ├── ProductQueryDto.java    ← GET /products         (목록 한 줄)
  └── ProductDetailDto.java   ← GET /products/{id}    (상세 + category·member 조인)
  ```

---

## MyBatis XML은 resultMap으로 컬럼과 필드를 명시한다

- **상태:** 적용
- **적용 범위:** `resources/mappers/*.xml`의 모든 `<select>`
- **결정:** `<select>`는 `resultType` 대신 `<resultMap>`을 쓰고, PK는 `<id>`, 나머지 컬럼은 `<result property="camel" column="snake"/>`로 한 줄씩 적는다. 조인 컬럼은 `AS` 별칭을 준 뒤 같은 방식으로 `<result>` 한 줄을 추가한다.
- **이유:** `map-underscore-to-camel-case`에만 의존하면 별칭·필드명이 어긋났을 때 예외 없이 null이 들어가 원인을 찾기 어렵다. `resultMap`에 컬럼↔필드가 전부 적혀 있으면 SQL과 DTO를 왕복하지 않고 XML 한 곳에서 대응을 확인할 수 있고, 리뷰어도 SELECT 컬럼과 `<result>` 줄 수가 맞는지만 보면 된다.
- **예외:** 없음
- **예시:**

  ```xml
  <resultMap id="ProductDetailMap" type="com.b101.dib.product.query.dto.ProductDetailDto">
      <id     property="productId"    column="product_id" />
      <result property="title"        column="title" />
      <result property="categoryId"   column="category_id" />
      <result property="categoryName" column="category_name" />
      <result property="memberId"     column="member_id" />
      <result property="nickname"     column="nickname" />
  </resultMap>

  <select id="findById" resultMap="ProductDetailMap">
      SELECT p.product_id, p.title,
             c.category_id, c.name AS category_name,
             m.member_id, m.nickname
      FROM product p
      JOIN category c ON c.category_id = p.category_id
      JOIN member   m ON m.member_id   = p.member_id
      WHERE p.product_id = #{productId}
  </select>
  ```

---

## Mapper 단건 조회는 DTO를 직접 반환하고 서비스에서 null을 검사한다

- **상태:** 적용
- **적용 범위:** MyBatis Mapper의 단건 조회 메서드와 이를 호출하는 Query Service
- **결정:** Mapper의 단건 조회는 `Optional<>`로 감싸지 않고 DTO를 그대로 반환하며, Service가 `null`이면 `BusinessException(ErrorCode.XXX_NOT_FOUND)`를 던진다.
- **이유:** MyBatis는 결과가 없으면 `null`을 돌려주는 것이 기본 동작이다. `Optional`을 끼우면 `orElseThrow` 한 줄이 줄지만 팀 안에서 두 방식이 섞이고, 존재하지 않는 자원의 처리(404)는 저장소가 아니라 서비스의 판단이므로 서비스에 명시적으로 둔다.
- **예외:** 없음
- **예시:**

  ```java
  // repository/ProductMapper.java
  ProductDetailDto findById(@Param("productId") Long productId);

  // query/service/ProductQueryServiceImpl.java
  ProductDetailDto dto = productMapper.findById(productId);
  if (dto == null) {
      throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
  }
  return dto;
  ```

---

## Entity는 연관관계 대신 FK 값을 Long으로 가진다

- **상태:** 적용
- **적용 범위:** 모든 JPA Entity
- **결정:** 다른 테이블을 가리키는 컬럼은 `@ManyToOne`·`@OneToMany`를 쓰지 않고 `private Long memberId;`처럼 FK 값 필드로 둔다. 연관 데이터가 필요한 조회는 MyBatis SQL에서 JOIN한다.
- **이유:** 연관관계 매핑은 지연 로딩, N+1, 양방향 순환 참조, `toString`·JSON 직렬화 무한 루프 등 알아야 할 것이 많고, Command 쪽에서 실제로 필요한 것은 "어느 회원의 것인지 검사"처럼 ID 비교가 대부분이다. 조인이 필요한 화면은 Query 쪽 SQL이 담당하므로 Entity는 테이블 컬럼을 1:1로 옮긴 단순한 형태로 유지한다.
- **예외:** 없음
- **예시:**

  ```java
  @Entity
  public class Auction {
      @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
      private Long auctionId;
      private Long productId;          // FK, Product 객체 아님
      private Long liveBroadcastId;

      @Enumerated(EnumType.STRING)
      @JdbcTypeCode(SqlTypes.NAMED_ENUM)
      private AuctionStatus status;    // PostgreSQL enum 컬럼
  }
  ```

  소유자 검사는 `product.getMemberId().equals(memberId)`처럼 ID 비교로 한다. (`Long`은 `==`가 아니라 `equals`.)

---

## 입찰 트랜잭션에는 입찰 성패를 결정하는 쓰기만 넣는다

- **상태:** 제안
- **적용 범위:** `BidCommandService.place()`, `AuctionEndTxService.endOne()` 등 경매·입찰 Command 트랜잭션과 그 안에서 호출되는 모든 코드
- **결정:** 락을 잡은 트랜잭션 안에서는 `bid` INSERT, `auction` 갱신(현재가·최고입찰·마감), 그리고 `outbox_event` INSERT 만 수행한다. 알림 저장, 행동 로그(`member_event`), 외부 API 호출(결제·알림톡·AI), 통계는 트랜잭션 밖에서 Kafka Consumer 가 처리한다.
- **이유:** "누가 먼저 입찰했는가"는 동기로 직렬화해야 하지만, 그 뒤에 일어나는 일은 입찰 성패와 무관하다. 락 구간에 부수 작업이 들어가면 락 보유 시간이 늘어 경쟁 입찰 처리량이 떨어지고, 알림 저장 실패 같은 부수 작업 오류가 입찰 자체를 롤백시킨다. 아키텍처 문서의 "실시간 입찰은 Kafka 를 거치지 않고, 후속 처리는 Kafka 로 분리한다"를 코드 규칙으로 옮긴 것이다.
- **예외:** 낙찰 주문(`order`) 생성은 낙찰 결과의 일부이므로 종료 트랜잭션 안에서 한다. 자동결제(외부 토스 API)는 트랜잭션 밖이며 `dib.auction.closed` Consumer 가 수행한다.
- **예시:**

  ```java
  @Transactional
  public BidPlacedDto place(Long auctionId, Long memberId, Long amount) {
      Auction auction = auctionRepository.findByIdForUpdate(auctionId).orElseThrow(...);
      // 검증 → bid 저장 → auction.applyBid()
      outboxEventRepository.save(OutboxEvent.of("AUCTION", auctionId, "BID_PLACED", payload));
      return BidPlacedDto.from(bid, auction);      // Notification.outbid(...) 저장 금지 → Consumer 로
  }
  ```

---

## 유실되면 안 되는 비동기 이벤트는 Outbox 를 거쳐 Kafka 로 보낸다

- **상태:** 제안
- **적용 범위:** 도메인 상태 변경 뒤에 다른 도메인·외부 시스템이 반응해야 하는 모든 이벤트. 현재 대상: `BID_PLACED`, `AUCTION_CLOSED`, `ORDER_PAID`, `ORDER_CONFIRMED`
- **결정:** 이벤트는 비즈니스 데이터와 같은 트랜잭션에서 `outbox_event` 테이블에 INSERT 한다. `OutboxPublisher`(스케줄러, `FOR UPDATE SKIP LOCKED`)가 커밋된 행을 Kafka 토픽 `dib.<aggregate>.<event>` 로 발행하고 `published_at` 을 채운다. Spring `ApplicationEventPublisher` 는 같은 Pod 안에서 즉시 반응해야 하는 것(소켓 푸시 트리거)에만 쓴다. Consumer 는 `event_id` 로 중복 처리를 막는다(같은 이벤트가 두 번 와도 결과가 같아야 함).
- **이유:** `@TransactionalEventListener(AFTER_COMMIT)` 는 Pod 가 그 순간 죽으면 이벤트가 사라지고, 커밋 전에 Kafka 로 보내면 롤백된 트랜잭션의 이벤트가 소비된다. Outbox 는 "DB 커밋 = 이벤트 발행 확정"을 보장하고 재처리가 가능하다.
- **예외:** 채팅 메시지 소켓 전달, `HIGHEST_BID_UPDATED` 같은 실시간 화면 갱신은 유실을 허용하고(재연결 시 Snapshot 으로 복구) Outbox·Kafka 를 거치지 않는다.
- **예시:**

  ```text
  Kafka 토픽                 발행 시점                       Consumer (각각 별도 @KafkaListener, groupId 다르게)
  dib.bid.placed            입찰 커밋                        notification(OUTBID 저장), memberEvent(BID 로그)
  dib.auction.closed        종료 커밋(주문 생성 포함)          payment(autoCharge), notification(AUCTION_WON), fraud(이상입찰 분석 요청), stats
  dib.order.paid            결제 확정 커밋                    notification(판매자 결제완료), stats
  dib.order.confirmed       구매 확정 커밋                    settlement(정산 지급), notification
  ```

  메시지 봉투는 소켓과 동일한 `{eventType, eventId, occurredAt, payload}` 를 쓰고, 메시지 키는 `aggregateId`(같은 경매의 이벤트가 순서대로 한 파티션에 들어가도록).

---

## 소켓 브로드캐스트는 Redis Pub/Sub 을 거쳐 모든 Pod 에 전파한다

- **상태:** 제안
- **적용 범위:** `AuctionWebSocketService`, `OrderChatWebSocketService`, `NotificationPushEventListener` 등 `SimpMessagingTemplate` 을 호출하는 모든 코드
- **결정:** 서비스 코드는 `SimpMessagingTemplate` 을 직접 호출하지 않고 `RealtimePublisher.publish(destination, envelope)` 를 호출한다. `RealtimePublisher` 는 Redis 채널 `dib:realtime` 에 발행하고, 각 Pod 의 `RedisMessageListener` 가 수신해 자기 Pod 에 붙은 세션으로 `SimpMessagingTemplate` 을 호출한다. 개인 큐(`/user/queue/**`)도 같은 경로를 탄다(각 Pod 가 `convertAndSendToUser` 를 시도하면 세션이 있는 Pod 만 실제 전달).
- **이유:** 현재 `SimpMessagingTemplate` 직접 호출은 그 Pod 에 연결된 클라이언트에게만 간다. Pod 가 2개 이상이면 다른 Pod 에 붙은 사용자는 `HIGHEST_BID_UPDATED` 를 못 받는다. Redis Pub/Sub 은 영속하지 않으므로 실시간 전달 전용이고, 놓친 메시지는 Snapshot 으로 복구한다(아키텍처 문서 5장).
- **예외:** `@SubscribeMapping` 으로 구독 직후 1회 응답하는 Snapshot 은 요청한 세션이 그 Pod 에 있으므로 직접 반환한다.
- **예시:**

  ```text
  BidPlacedEvent(AFTER_COMMIT)
   → RealtimePublisher.publish("/topic/auctions/1", envelope)
   → Redis PUBLISH dib:realtime {destination, envelope}
   → Pod A, Pod B 각각 SUBSCRIBE 수신 → SimpMessagingTemplate.convertAndSend(destination, envelope)
  ```

  Redis 가 죽으면 소켓 갱신만 멈추고 입찰·주문은 정상 동작해야 한다(발행 실패는 로그만 남기고 예외를 전파하지 않는다).

---

## 동시 입찰은 Redis 분산락 안에서 DB 행 락으로 확정한다

- **상태:** 제안 (구현 완료 — `common/lock/AuctionLock`, 팀 리뷰 후 적용)
- **적용 범위:** 같은 경매 행을 갱신하는 모든 Command (`place`, `endOne`, 시작·취소)
- **결정:** 경매 단위 Redis 락 `lock:auction:{auctionId}`(Redisson `tryLock(wait 1s, lease 3s)`) 을 먼저 잡고, 그 안에서 `findByIdForUpdate`(PESSIMISTIC_WRITE) 로 행을 다시 잠근 뒤 검증·갱신한다. 최종 정합성은 DB 락과 `UNIQUE(auction_id, amount)` 제약이 보장하고, Redis 락은 여러 Pod 의 요청이 DB 락 대기열에 몰리는 것을 줄이는 수단이다. Redis 락 획득 실패는 `409 AUCTION_BUSY` 로 응답해 클라이언트가 재시도한다.
- **이유:** DB 락만 쓰면 커넥션 풀이 락 대기로 소진될 수 있고, Redis 락만 쓰면 lease 만료·네트워크 분할 시 두 요청이 동시에 통과할 수 있다. 둘을 겹쳐 "빠른 거절 + 확실한 정합성"을 얻는다.
- **예외:** local 프로필에서 Redis 가 없을 때는 DB 락만으로 동작해야 한다(`dib.lock.redis-enabled=false`).
- **예시:**

  ```java
  RLock lock = redissonClient.getLock("lock:auction:" + auctionId);
  if (!lock.tryLock(1, 3, TimeUnit.SECONDS)) throw new BusinessException(ErrorCode.AUCTION_BUSY);
  try { return bidTxService.place(auctionId, memberId, amount); }   // 안에서 findByIdForUpdate
  finally { if (lock.isHeldByCurrentThread()) lock.unlock(); }
  ```

  락 해제는 트랜잭션 커밋 **후** 여야 하므로 락 획득 메서드와 `@Transactional` 메서드를 분리한다(같은 클래스 self-invocation 금지). 실제 코드: `BidCommandServiceImpl`(락) → `BidTxServiceImpl`(트랜잭션). Redisson 은 스타터 대신 core(`org.redisson:redisson`) 만 넣고 `RedisLockConfig` 에서 클라이언트를 직접 만든다 — 스타터는 Spring Data Redis 연결 팩토리를 Redisson 으로 갈아끼워 auth 의 Lua 스크립트 저장소에 영향을 줄 수 있어서.

---

## AI 서버(FastAPI)와는 명세 92~95 비동기 계약으로만 통신한다

- **상태:** 적용
- **적용 범위:** `components/ai`(FastAPI, `python -m uvicorn serve:app --port 8000`) 를 호출하거나 그 콜백을 받는 백엔드 코드 전부. 현재 `common/ai/AiServerClient`, `fraudDetection/command/**`
- **결정:** 백엔드는 AI 의 **비동기 내부 API(202 접수 → 콜백)** 만 호출한다. 동기 엔드포인트(`/internal/fraud/detect`, `/internal/reco/home`, `/internal/reco/similar`, `/internal/moderation/review`) 는 AI 쪽 개발·시연용이므로 서비스 코드에서 부르지 않는다. 호출은 Kafka Consumer 안에서만 한다(도메인 트랜잭션·스케줄러 스레드에서 외부 호출 금지). 요청·콜백 모두 HMAC 서명을 붙이고 검증한다.
- **이유:** AI 가 명세대로 "요청 즉시 202, 결과는 `callbackUrl` 로 POST" 구조라 백엔드가 응답을 기다리며 스레드를 묶지 않는다. 동기 엔드포인트는 snake_case·응답 구조가 다르고 AI 담당이 "계약은 비동기 쪽"이라고 명시했다. AI 장애가 경매 종료·낙찰을 막지 않아야 하므로 요청 실패는 로그만 남긴다.
- **예외:** 없음. AI 서버가 꺼져 있으면 `dib.ai.enabled=false` 로 두고 요청을 생략한다.
- **예시:**

  **AI 가 받는 것 (백엔드 → AI, `Authorization` 없음, HMAC 헤더 2개)**

  | 명세 | 요청 | 본문 (camelCase) | 응답 |
  |---|---|---|---|
  | 92 | `POST {ai}/internal/v1/ai/bid-anomalies` | `{jobId, auctionId, memberId(분석 대상 입찰자 1명), bidId?, bids:[{bidId, memberId, amount, createdAt}], callbackUrl}` | `202 {jobId, status:"ACCEPTED"}` |
  | 94 | `POST {ai}/internal/v1/ai/recommendations` | `{jobId, memberId, behaviorWindow?(미사용), candidateAuctionIds:[…](비면 전체 ACTIVE), callbackUrl}` | `202 {jobId, status:"ACCEPTED"}` |

  - 92 는 **입찰자 1명 = 요청 1건**. 경매 종료 후 입찰자 수만큼 보낸다. `bids[]` 를 보내면 AI 는 그것을 입찰 목록으로 쓴다(방금 끝난 경매의 마지막 입찰을 AI 조회기가 못 봤을 수 있어서). 경매 정보·과거 이력은 AI 조회기가 봐야 하므로 AI 가 그 경매를 모르면 `503 MODEL_UNAVAILABLE`.
  - `jobId` 가 같으면 AI 는 재분석하지 않는다(프로세스 메모리 기준). 백엔드는 `"bid-anomaly-{auctionId}-{memberId}"` 처럼 결정적으로 만든다.
  - 거절: `400 INVALID_PAYLOAD`(본문·callbackUrl 형식), `401`(서명 불일치), `503 MODEL_UNAVAILABLE`(엔진 미준비·경매 없음). 202 이후 실패(추론 예외·콜백 실패)는 AI 로그에만 남고 우리는 알 수 없다 → 콜백이 안 오면 그냥 결과 없음.
  - `callbackUrl` 호스트는 AI 의 `DIB_CALLBACK_ALLOWED_HOSTS` 에 있어야 한다(비어 있으면 검사 안 함 — 로컬).

  **AI 가 주는 것 (AI → 백엔드 콜백, HMAC 서명 있음, 4xx 면 재시도 안 함 / 5xx·연결 실패는 3회 지수 백오프)**

  | 명세 | 우리 엔드포인트 | 본문 |
  |---|---|---|
  | 93 | `POST {backend}/internal/v1/ai/callbacks/bid-anomalies` | `{jobId, auctionId, memberId, bidId, features:{bidderTendency, biddingRatio, lastBidding, auctionBids, startingPriceAverage, earlyBidding, winningRatio, auctionDuration(항상 null), ruleScore, mlScore(모델 미가동 시 null), riskScore}, predictedLabel(0/1), decisionThreshold, modelVersion(null 가능), featureVersion}` → `fraud_detection` 1행 |
  | 95 | (미구현 — 추천 담당) | `{jobId, memberId, items:[{auctionId, score, reason}]}` |

  - 판정 대상이 아닌 입찰자(이력 부족 등)는 **콜백이 오지 않는다**. "판단하지 않음"을 0점으로 저장하지 않기 위해서다. 그래서 `fraud_detection` 에 행이 없는 입찰자는 "정상"이 아니라 "미판정"으로 읽어야 한다.
  - `features` 의 null 을 저장하기 위해 V6 에서 `fraud_detection` 피처 컬럼·`ml_score`·`model_version` 의 NOT NULL 을 풀었다.

  **HMAC 규약 (양방향 동일, `common/ai/HmacSigner`)**

  ```text
  서명 대상   "{unix초}." + 요청 본문 원문(bytes)      ← JSON 을 다시 직렬화하지 말고 보낸 바이트 그대로
  서명        HMAC-SHA256(secret, 서명 대상) → 16진수
  헤더        X-DIB-Timestamp: <unix초>
              X-DIB-Signature: sha256=<hex>
  허용 오차   300초
  시크릿      백엔드→AI : dib.ai.service-hmac-secret  == AI 의 DIB_SERVICE_HMAC_SECRET
              AI→백엔드 : dib.ai.ai-hmac-secret       == AI 의 DIB_AI_HMAC_SECRET
  ```

  AI 는 시크릿이 비어 있으면 요청을 **막는다**(503). 우리도 `ai-hmac-secret` 이 비어 있으면 콜백을 401 로 거절한다.

  **AI 가 지금 못 하는 것 (AI 담당 확인 사항)**: 탐지·추천 조회기가 `InMemoryProvider`(합성 데이터) 라 실제 경매 id 로 92 를 보내면 `503 MODEL_UNAVAILABLE` 이 온다. `PostgresProvider`(읽기 전용 DB 계정) 가 붙어야 실데이터 분석이 된다. 상품 검수(`/internal/moderation/review`) 는 동기만 있고 비동기 계약이 없다 → 상품 등록 담당(민종)과 AI 담당이 정할 것.

---
