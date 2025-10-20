package org.example.backend.domain.tradechat.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.domain.tradechat.dto.TradeChatMessageDto;
import org.example.backend.domain.tradechat.dto.TradeChatRoomDto;
import org.example.backend.domain.tradechat.service.TradeChatService;
import org.example.backend.global.security.CustomUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class TradeChatController {

    private final TradeChatService tradeChatService;


    /*
    STOMP 메세지 전송
        - 클라이언트 /receive/{roomId} 경로로 수신
        - 서버는 /send/{roomId} 로 메세지를 캐스팅
     */
    @MessageMapping("/{roomId}")
    public void sendMessage(@DestinationVariable Long roomId, TradeChatMessageDto request) {
        tradeChatService.sendMessage(roomId, request);
    }

    /*
     현재 거래게시글에 대한 채팅방 생성
        - 채팅방이 존재하지 않을 때만 채팅방 새로 생성
     */
    @PostMapping("/{tradeId}/room")
    public ResponseEntity<Long> createChatRoom(@PathVariable Long tradeId, @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long memberId = userDetails.getId();
        Long roomId = tradeChatService.createChatRoom(tradeId, memberId);
        return ResponseEntity.ok(roomId);
    }

    /*
    특정 채팅방의 이전 채팅 내역 조회
        - 참여자 검증 로직 포함
     */
    @GetMapping("/rooms/messages/{roomId}")
    public ResponseEntity<List<TradeChatMessageDto>> getMessages(@PathVariable Long roomId, @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long memberId = userDetails.getId();
        List<TradeChatMessageDto> messages = tradeChatService.getMessages(roomId, memberId);
        return ResponseEntity.ok(messages);
    }

    /*
    내 채팅방 목록 조회
        - ONGOING 상태의 채팅방만 조회
     */
    @GetMapping("/rooms/me")
    public ResponseEntity<List<TradeChatRoomDto>> getMyChatRooms(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long memberId = userDetails.getId();
        List<TradeChatRoomDto> chatRooms = tradeChatService.getMyChatRooms(memberId);
        return ResponseEntity.ok(chatRooms);
    }
}
