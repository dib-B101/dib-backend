package com.b101.dib.member.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import com.b101.dib.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);

    Optional<Member> findByEmailAndPhoneNumber(String email, String phoneNumber);

    Optional<Member> findByPhoneNumber(String phoneNumber);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    boolean existsByPhoneNumber(String phoneNumber);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            UPDATE member
               SET status = 'WITHDRAWN',
                   updated_at = :now
             WHERE status = 'ACTIVE'
               AND deleted_at IS NOT NULL
               AND deleted_at <= :now
            """, nativeQuery = true)
    int completeDueWithdrawals(
            @Param("now") LocalDateTime now
    );
}
