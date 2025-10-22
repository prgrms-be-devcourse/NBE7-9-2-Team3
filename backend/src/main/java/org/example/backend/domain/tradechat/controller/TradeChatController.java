package org.example.backend.domain.tradechat.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.backend.domain.tradechat.dto.TradeChatMessageDto;
import org.example.backend.domain.tradechat.dto.TradeChatRoomDto;
import org.example.backend.domain.tradechat.service.TradeChatService;
import org.example.backend.global.response.ApiResponse;
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
@Tag(name = "Trade Chat", description = "거래 채팅 관리 API")
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
    @Operation(summary = "채팅방 생성", description = "현재 거래게시글에 대한 채팅방을 생성합니다.")
    @PostMapping("/{tradeId}/room")
    public ResponseEntity<ApiResponse<Long>> createChatRoom(
        @Parameter(description = "거래 게시글 ID", required = true)
        @PathVariable Long tradeId, 
        @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long memberId = userDetails.getId();
        Long roomId = tradeChatService.createChatRoom(tradeId, memberId);
        return ResponseEntity.ok(ApiResponse.ok("채팅방이 생성되었습니다.", roomId));
    }

    /*
    특정 채팅방의 이전 채팅 내역 조회
        - 참여자 검증 로직 포함
     */
    @Operation(summary = "채팅 내역 조회", description = "특정 채팅방의 이전 채팅 내역을 조회합니다.")
    @GetMapping("/rooms/messages/{roomId}")
    public ResponseEntity<ApiResponse<List<TradeChatMessageDto>>> getMessages(
        @Parameter(description = "채팅방 ID", required = true)
        @PathVariable Long roomId, 
        @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long memberId = userDetails.getId();
        List<TradeChatMessageDto> messages = tradeChatService.getMessages(roomId, memberId);
        return ResponseEntity.ok(ApiResponse.ok("채팅 내역을 조회했습니다.", messages));
    }

    /*
    내 채팅방 목록 조회
        - ONGOING 상태의 채팅방만 조회
     */
    @Operation(summary = "내 채팅방 목록 조회", description = "내가 참여한 채팅방 목록을 조회합니다.")
    @GetMapping("/rooms/me")
    public ResponseEntity<ApiResponse<List<TradeChatRoomDto>>> getMyChatRooms(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long memberId = userDetails.getId();
        List<TradeChatRoomDto> chatRooms = tradeChatService.getMyChatRooms(memberId);
        return ResponseEntity.ok(ApiResponse.ok("채팅방 목록을 조회했습니다.", chatRooms));
    }
}
