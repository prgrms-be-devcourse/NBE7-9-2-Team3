package org.example.backend.domain.trade.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.backend.domain.trade.dto.TradeRequestDto;
import org.example.backend.domain.trade.dto.TradeResponseDto;
import org.example.backend.domain.trade.enums.BoardType;
import org.example.backend.domain.trade.service.TradeService;
import org.example.backend.global.response.ApiResponse;
import org.example.backend.global.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/market/{boardType}")
@RequiredArgsConstructor
public class TradeController {

    private final TradeService tradeService;

    @PostMapping
    public ApiResponse<TradeResponseDto> createTrade(
        @PathVariable String boardType,
        @Valid @ModelAttribute TradeRequestDto request,
        @RequestPart(required = false) List<MultipartFile> images) {
        BoardType type = BoardType.from(boardType);
        TradeResponseDto trade = tradeService.createTrade(request, type, images);
        return ApiResponse.ok("거래 게시글 등록 성공", trade);
    }

    @GetMapping
    public ApiResponse<List<TradeResponseDto>> getAllTrades(
        @PathVariable String boardType) {
        BoardType type = BoardType.from(boardType);
        List<TradeResponseDto> trades = tradeService.getAllTrade(type);
        return ApiResponse.ok("거래 게시글 목록 조회 성공", trades);
    }

    @GetMapping("/{tradeId}")
    public ApiResponse<TradeResponseDto> getTrade(
        @PathVariable String boardType,
        @PathVariable Long tradeId) {
        BoardType type = BoardType.from(boardType);
        TradeResponseDto trade = tradeService.getTrade(type, tradeId);
        return ApiResponse.ok("거래 게시글 조회 성공", trade);
    }

    @PutMapping("/{tradeId}")
    public ApiResponse<TradeResponseDto> updateTrade(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable String boardType,
        @PathVariable Long tradeId,
        @Valid @ModelAttribute TradeRequestDto request,
        @RequestPart(required = false) List<MultipartFile> images) {
        Long memberId = userDetails.getId();
        BoardType type = BoardType.from(boardType);
        TradeResponseDto trade = tradeService.updateTrade(type, tradeId, memberId, request, images);
        return ApiResponse.ok("거래 게시글 수정 성공", trade);
    }

    @DeleteMapping("/{tradeId}")
    public ApiResponse<Void> deleteTrade(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable String boardType,
        @PathVariable Long tradeId) {
        Long memberId = userDetails.getId();
        BoardType type = BoardType.from(boardType);
        tradeService.deleteTrade(type, tradeId, memberId);
        return ApiResponse.ok("거래 게시글 삭제 성공");
    }
}
