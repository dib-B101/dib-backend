package com.b101.dib.product.command.entity;

import com.b101.dib.product.query.dto.ProductCondition;
import com.b101.dib.product.query.dto.ProductStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "product")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "member_id")
//    private Member member;
    private Long memberId;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "category_id")
//    private Category category;
    private Long categoryId;

    private String title;
    private String description;

    @Enumerated(EnumType.STRING)
    private ProductCondition condition;
    private String modelName;
    private Integer releaseYear;
    private Long marketPrice;
    private String thumbnailUrl;

    @Enumerated(EnumType.STRING)
    private ProductStatus status;
    private String embedding;

    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

}
