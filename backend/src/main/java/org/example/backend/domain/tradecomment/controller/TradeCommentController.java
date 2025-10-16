package org.example.backend.domain.tradecomment.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.backend.domain.trade.enums.BoardType;
import org.example.backend.domain.tradecomment.dto.TradeCommentRequestDto;
import org.example.backend.domain.tradecomment.dto.TradeCommentResponseDto;
import org.example.backend.domain.tradecomment.service.TradeCommentService;
import org.example.backend.global.rsdata.RsData;
import org.example.backend.global.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/market")
@RequiredArgsConstructor
public class TradeCommentController {

    private final TradeCommentService tradeCommentService;

    @PostMapping("/{boardType}/{tradeId}/comments")
    public RsData<TradeCommentResponseDto> createComment(
        @PathVariable String boardType,
        @PathVariable Long tradeId,
        @Valid @RequestBody TradeCommentRequestDto request) {
        BoardType type = BoardType.from(boardType);
        TradeCommentResponseDto comment = tradeCommentService.createComment(type, request);
        return new RsData<>("201-1", "댓글 등록 성공", comment);
    }

    @GetMapping("/{boardType}/{tradeId}/comments")
    public RsData<List<TradeCommentResponseDto>> getAllComments(
        @PathVariable String boardType,
        @PathVariable Long tradeId) {
        BoardType type = BoardType.from(boardType);
        List<TradeCommentResponseDto> comments = tradeCommentService.getAllComment(type, tradeId);
        return new RsData<>("200-1", "댓글 목록 조회 성공", comments);
    }

    @PutMapping("/{boardType}/{tradeId}/comments/{commentId}")
    public RsData<TradeCommentResponseDto> updateComment(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable String boardType,
        @PathVariable Long tradeId,
        @PathVariable Long commentId,
        @Valid @RequestBody TradeCommentRequestDto request) {
        Long memberId = userDetails.getId();
        BoardType type = BoardType.from(boardType);
        TradeCommentResponseDto comment = tradeCommentService.updateComment(type, tradeId,
            commentId,
            memberId, request);
        return new RsData<>("200-2", "댓글 수정 성공", comment);
    }

    @DeleteMapping("/{boardType}/{tradeId}/comments/{commentId}")
    public RsData<Void> deleteComment(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable String boardType,
        @PathVariable Long tradeId,
        @PathVariable Long commentId) {
        Long memberId = userDetails.getId();
        BoardType type = BoardType.from(boardType);
        tradeCommentService.deleteComment(type, tradeId, commentId, memberId);
        return new RsData<>("204-1", "댓글 삭제 성공");
    }

}
