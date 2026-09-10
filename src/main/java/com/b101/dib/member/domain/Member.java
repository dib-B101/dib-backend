package com.b101.dib.member.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(
        name = "member",
        uniqueConstraints = {
            @UniqueConstraint(name = "up_member_email", columnNames = "email"),
            @UniqueConstraint(name = "uq_member_nickname", columnNames = "nickname"),
            @UniqueConstraint(name = "uq_member_phone_number", columnNames = "phone_number")
        }
)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Column(nullable = false, length = 10)
    private String name;

    // DB 컬럼이 PostgreSQL 네이티브 ENUM(gender) 이라 NAMED_ENUM 으로 바인딩해야 INSERT 가 통과한다
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false)
    private Gender gender;

    @Column(nullable = false)
    private LocalDate birthDate;

    @Column(nullable = false, length = 20)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false)
    private MemberStatus status;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false)
    private MemberRole role;

    @Column(nullable = false)
    private Double score;

    @Column(name = "last_login_at", nullable = true)
    private LocalDateTime lastLoginAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at", nullable = true)
    private LocalDateTime deletedAt;

    @Column(name = "suspended_at", nullable = true)
    private LocalDateTime suspendedAt;

    @Column(nullable = true, length = 100)
    private String bankName;

    @Column(nullable = true, length = 50)
    private String accountHolder;

    @Column(nullable = true, length = 50)
    private String accountNumber;

    @Column(nullable = false)
    private Integer warningCount;

}
