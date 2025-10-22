package org.example.backend.domain.tradechat.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.backend.domain.member.entity.Member;
import org.example.backend.domain.member.repository.MemberRepository;
import org.example.backend.domain.trade.entity.Trade;
import org.example.backend.domain.trade.repository.TradeRepository;
import org.example.backend.domain.tradechat.dto.TradeChatMessageDto;
import org.example.backend.domain.tradechat.dto.TradeChatRoomDto;
import org.example.backend.domain.tradechat.entity.ChatStatus;
import org.example.backend.domain.tradechat.entity.TradeChatMessage;
import org.example.backend.domain.tradechat.entity.TradeChatRoom;
import org.example.backend.domain.tradechat.repository.TradeChatMessageRepository;
import org.example.backend.domain.tradechat.repository.TradeChatRoomRepository;
import org.example.backend.global.exception.BusinessException;
import org.example.backend.global.exception.ErrorCode;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TradeChatService {

    private final TradeChatRoomRepository chatRoomRepository;
    private final TradeChatMessageRepository chatMessageRepository;
    private final TradeRepository tradeRepository;
    private final MemberRepository memberRepository;
    private final SimpMessagingTemplate messagingTemplate;


    @Transactional
    public void sendMessage(Long roomId, TradeChatMessageDto request) {
        TradeChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TRADE_CHAT_ROOM_NOT_FOUND));
        Member sender = memberRepository.findById(request.senderId())
                .orElseThrow(() -> new BusinessException(ErrorCode.TRADE_CHAT_SENDER_NOT_FOUND));

        // 메세지 채팅 생성
        TradeChatMessage message = TradeChatMessage.create(room, sender, request.content());
        TradeChatMessage savedMessage = chatMessageRepository.save(message);

        // DB에 저장된 메시지를 DTO로 변환하여 브로드캐스트
        TradeChatMessageDto messageDto = TradeChatMessageDto.from(savedMessage);
        messagingTemplate.convertAndSend("/receive/" + roomId, messageDto);
    }

    public List<TradeChatRoomDto> getMyChatRooms(Long id) {

        // 현재 ONGOING 상태의 채팅방만 조회
        List<TradeChatRoom> rooms = chatRoomRepository.findAllWithMemberAndTrade(ChatStatus.ONGOING, id);

        return rooms.stream()
                .map(TradeChatRoomDto::from)
                .toList();
    }

    public Long createChatRoom(Long tradeId, Long memberId) {
        Trade trade = tradeRepository.findById(tradeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TRADE_CHAT_TRADE_NOT_FOUND));
        Member seller = trade.getMember();
        Member buyer = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TRADE_CHAT_BUYER_NOT_FOUND));

        // 중복 체크 후 없으면 새로 생성
        return chatRoomRepository.findByTradeAndSellerIdAndBuyerId(trade, seller, buyer)
                .map(TradeChatRoom::getId)
                .orElseGet(() -> {
                    TradeChatRoom newRoom = TradeChatRoom.builder()
                            .trade(trade)
                            .sellerId(seller)
                            .buyerId(buyer)
                            .createDate(LocalDateTime.now())
                            .status(ChatStatus.ONGOING)
                            .build();
                    chatRoomRepository.save(newRoom);
                    return newRoom.getId();
                });
    }

    public List<TradeChatMessageDto> getMessages(Long roomId, Long memberId) {
        TradeChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TRADE_CHAT_ROOM_NOT_FOUND));
        Long sellerId = room.getSellerId().getMemberId();
        Long buyerId = room.getBuyerId().getMemberId();

        // 현재 채팅방의 구매자, 판매자 아이디 모두 아닐 경우 접근 제한 (참여자 검증)
        if (!sellerId.equals(memberId) && !buyerId.equals(memberId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ACCESS);
        }

        // 최신순이 위로가도록 정렬
        List<TradeChatMessage> messages = chatMessageRepository.findByChatRoomOrderBySendDateAsc(room);
        return messages.stream()
                .map(TradeChatMessageDto::from)
                .toList();
    }
}
