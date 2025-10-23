package org.example.backend.domain.tradechat.dto;

import org.example.backend.domain.tradechat.entity.ChatStatus;
import org.example.backend.domain.tradechat.entity.TradeChatRoom;

import java.time.LocalDateTime;

public record TradeChatRoomDto(
        Long roomId,
        Long tradeId,
        String tradeTitle,
        Long sellerId,
        String sellerNickname,
        Long buyerId,
        String buyerNickname,
        LocalDateTime createDate,
        ChatStatus status
) {
    public static TradeChatRoomDto from(TradeChatRoom room) {
        return new TradeChatRoomDto(
                room.getId(),
                room.getTrade().getTradeId(),
                room.getTrade().getTitle(),
                room.getSellerId().getMemberId(),
                room.getSellerId().getNickname(),
                room.getBuyerId().getMemberId(),
                room.getBuyerId().getNickname(),
                room.getCreateDate(),
                room.getStatus()
        );
    }
}
