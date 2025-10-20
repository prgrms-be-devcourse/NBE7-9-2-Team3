package org.example.backend.domain.tradechat.repository;

import org.example.backend.domain.member.entity.Member;
import org.example.backend.domain.trade.entity.Trade;
import org.example.backend.domain.tradechat.entity.ChatStatus;
import org.example.backend.domain.tradechat.entity.TradeChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TradeChatRoomRepository extends JpaRepository<TradeChatRoom, Long> {
    List<TradeChatRoom> findByStatusAndSellerId_MemberIdOrStatusAndBuyerId_MemberId(ChatStatus sellerStatus, Long sellerId, ChatStatus buyerStatus, Long buyerId);

    Optional<TradeChatRoom> findByTradeAndSellerIdAndBuyerId(Trade trade, Member seller, Member buyer);
}
