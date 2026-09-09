package com.b101.dib.productImage.domain;

import java.time.LocalDateTime;

import com.b101.dib.auction.domain.Auction;
import com.b101.dib.auction.domain.AuctionStatus;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
@Table(name = "product_image")
public class ProductImage {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long productImageId;
	private Long productId;
	private String imageUrl;
	private Integer sequence;

}
