package com.b101.dib.auction.command.service;

import com.b101.dib.auction.command.dto.AuctionEndResultDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuctionEndedEvent {
    private final AuctionEndResultDto result;
}
