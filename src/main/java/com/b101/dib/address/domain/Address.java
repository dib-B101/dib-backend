package com.b101.dib.address.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "address")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id")
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(length = 50)
    private String number;

    @Column(length = 500)
    private String address;

    @Column(name = "detail_address", length = 500)
    private String detailAddress;

    @Column(name = "receiver_name", length = 100)
    private String receiverName;

    @Column(name = "receiver_phone", length = 20)
    private String receiverPhone;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "api_address_id", nullable = false, length = 500)
    private String apiAddressId;

    public void update(String number, String address, String name, String apiAddressId,
                       String detailAddress, String receiverName, String receiverPhone) {
        this.number = number;
        this.address = address;
        this.name = name;
        this.apiAddressId = apiAddressId;
        this.detailAddress = detailAddress;
        this.receiverName = receiverName;
        this.receiverPhone = receiverPhone;
    }
}
