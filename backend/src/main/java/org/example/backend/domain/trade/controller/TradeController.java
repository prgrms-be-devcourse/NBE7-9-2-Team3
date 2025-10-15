package org.example.backend.domain.trade.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.backend.domain.trade.dto.TradeRequestDto;
import org.example.backend.domain.trade.dto.TradeResponseDto;
import org.example.backend.domain.trade.enums.BoardType;
import org.example.backend.domain.trade.service.TradeService;
import org.example.backend.global.rsdata.RsData;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/market")
@RequiredArgsConstructor
public class TradeController {

    private final TradeService tradeService;

    @PostMapping("/{boardType}")
    public RsData<TradeResponseDto> createTrade(
        @PathVariable String boardType,
        @Valid @RequestBody TradeRequestDto request) {
        BoardType type = BoardType.from(boardType);
        TradeResponseDto trade = tradeService.createTrade(request, type);
        return new RsData<>("201-1", "거래 게시글 등록 성공", trade);
    }

    @GetMapping("/{boardType}")
    public RsData<List<TradeResponseDto>> getAllTrades(
        @PathVariable String boardType) {
        BoardType type = BoardType.from(boardType);
        List<TradeResponseDto> trades = tradeService.getAllTrade(type);
        return new RsData<>("200-1", "거래 게시글 목록 조회 성공", trades);
    }

    @GetMapping("/{boardType}/{tradeId}")
    public RsData<TradeResponseDto> getTrade(
        @PathVariable String boardType,
        @PathVariable Long tradeId) {
        BoardType type = BoardType.from(boardType);
        TradeResponseDto trade = tradeService.getTrade(type, tradeId);
        return new RsData<>("200-2", "거래 게시글 조회 성공", trade);
    }

    @PutMapping("/{boardType}/{tradeId}")
    public RsData<TradeResponseDto> updateTrade(
        @PathVariable String boardType,
        @PathVariable Long tradeId,
        @RequestParam Long memberId,  // TODO : 인증 구현 후 변경 필요(보안 이슈)
        @Valid @RequestBody TradeRequestDto request) {
        BoardType type = BoardType.from(boardType);
        TradeResponseDto trade = tradeService.updateTrade(type, tradeId, memberId, request);
        return new RsData<>("200-3", "거래 게시글 수정 성공", trade);
    }

    @DeleteMapping("/{boardType}/{tradeId}")
    public RsData<Void> deleteTrade(
        @PathVariable String boardType,
        @PathVariable Long tradeId,
        @RequestParam Long memberId) {  // TODO : 인증 구현 후 변경 필요(보안 이슈)
        BoardType type = BoardType.from(boardType);
        tradeService.deleteTrade(type, tradeId, memberId);
        return new RsData<>("204-1", "거래 게시글 삭제 성공");
    }
}
