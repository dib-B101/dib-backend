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
