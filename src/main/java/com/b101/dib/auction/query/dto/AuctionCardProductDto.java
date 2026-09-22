package com.b101.dib.auction.query.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

// 프론트 AuctionProductDto
@Getter
@Setter
@NoArgsConstructor
public class AuctionCardProductDto {
    private Long productId;
    private String name;
    private String title;
    private String categoryName;
    private String description;
    private String condition;
    private String modelName;
    private Integer releaseYear;
    private Long marketPrice;
    private String thumbnailUrl;
    private List<String> images = new ArrayList<>();
}
