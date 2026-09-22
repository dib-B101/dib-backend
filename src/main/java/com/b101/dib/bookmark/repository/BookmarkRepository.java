package com.b101.dib.bookmark.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.b101.dib.bookmark.domain.Bookmark;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

	Bookmark findByMemberIdAndProductId(Long myId, Long productId);

	// 찜한 상품에 일이 생겼을 때 알림을 보낼 대상. 본인(판매자)이 찜했을 수도 있어 호출부에서 걸러 낸다
	@Query("SELECT b.memberId FROM Bookmark b WHERE b.productId = :productId")
	List<Long> findMemberIdsByProductId(@Param("productId") Long productId);

	// 라이브 방송 한 건에 편성된 상품들을 한 번에. 상품마다 쿼리를 날리면 편성 수만큼 왕복한다
	@Query("SELECT b.productId, b.memberId FROM Bookmark b WHERE b.productId IN :productIds")
	List<Object[]> findProductAndMemberIdsIn(@Param("productIds") List<Long> productIds);

}
