package org.example.backend.domain.trade.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Trade", description = "거래 게시글 관리 API")
public class TradeController {

    private final TradeService tradeService;

    @Operation(summary = "거래 게시글 등록", description = "새로운 거래 게시글을 등록합니다.")
    @PostMapping
    public ApiResponse<TradeResponseDto> createTrade(
        @Parameter(description = "게시판 타입 (FISH: 물고기, SECONDHAND: 중고물품)", required = true)
        @PathVariable String boardType,
        @Parameter(description = "거래 게시글 정보", required = true)
        @Valid @ModelAttribute TradeRequestDto request,
        @Parameter(description = "이미지 파일들 (선택사항)")
        @RequestPart(required = false) List<MultipartFile> images) {
        BoardType type = BoardType.from(boardType);
        TradeResponseDto trade = tradeService.createTrade(request, type, images);
        return ApiResponse.ok("거래 게시글 등록 성공", trade);
    }

    @Operation(summary = "거래 게시글 목록 조회", description = "특정 게시판의 모든 거래 게시글을 조회합니다.")
    @GetMapping
    public ApiResponse<List<TradeResponseDto>> getAllTrades(
        @Parameter(description = "게시판 타입 (FISH: 물고기, SECONDHAND: 중고물품)", required = true)
        @PathVariable String boardType) {
        BoardType type = BoardType.from(boardType);
        List<TradeResponseDto> trades = tradeService.getAllTrade(type);
        return ApiResponse.ok("거래 게시글 목록 조회 성공", trades);
    }

    @Operation(summary = "거래 게시글 조회", description = "특정 거래 게시글의 상세 정보를 조회합니다.")
    @GetMapping("/{tradeId}")
    public ApiResponse<TradeResponseDto> getTrade(
        @Parameter(description = "게시판 타입 (FISH: 물고기, SECONDHAND: 중고물품)", required = true)
        @PathVariable String boardType,
        @Parameter(description = "거래 게시글 ID", required = true)
        @PathVariable Long tradeId) {
        BoardType type = BoardType.from(boardType);
        TradeResponseDto trade = tradeService.getTrade(type, tradeId);
        return ApiResponse.ok("거래 게시글 조회 성공", trade);
    }

    @Operation(summary = "거래 게시글 수정", description = "기존 거래 게시글을 수정합니다.")
    @PutMapping("/{tradeId}")
    public ApiResponse<TradeResponseDto> updateTrade(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @Parameter(description = "게시판 타입 (FISH: 물고기, SECONDHAND: 중고물품)", required = true)
        @PathVariable String boardType,
        @Parameter(description = "거래 게시글 ID", required = true)
        @PathVariable Long tradeId,
        @Parameter(description = "거래 게시글 수정 정보", required = true)
        @Valid @ModelAttribute TradeRequestDto request,
        @Parameter(description = "이미지 파일들 (선택사항)")
        @RequestPart(required = false) List<MultipartFile> images) {
        Long memberId = userDetails.getId();
        BoardType type = BoardType.from(boardType);
        TradeResponseDto trade = tradeService.updateTrade(type, tradeId, memberId, request, images);
        return ApiResponse.ok("거래 게시글 수정 성공", trade);
    }

    @Operation(summary = "거래 게시글 삭제", description = "기존 거래 게시글을 삭제합니다.")
    @DeleteMapping("/{tradeId}")
    public ApiResponse<Void> deleteTrade(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @Parameter(description = "게시판 타입 (FISH: 물고기, SECONDHAND: 중고물품)", required = true)
        @PathVariable String boardType,
        @Parameter(description = "거래 게시글 ID", required = true)
        @PathVariable Long tradeId) {
        Long memberId = userDetails.getId();
        BoardType type = BoardType.from(boardType);
        tradeService.deleteTrade(type, tradeId, memberId);
        return ApiResponse.ok("거래 게시글 삭제 성공");
    }
}
