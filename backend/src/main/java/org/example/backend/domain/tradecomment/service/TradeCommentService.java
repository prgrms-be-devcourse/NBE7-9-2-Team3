package org.example.backend.domain.tradecomment.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.backend.domain.member.entity.Member;
import org.example.backend.domain.member.repository.MemberRepository;
import org.example.backend.domain.trade.entity.Trade;
import org.example.backend.domain.trade.enums.BoardType;
import org.example.backend.domain.trade.repository.TradeRepository;
import org.example.backend.domain.tradecomment.dto.TradeCommentRequestDto;
import org.example.backend.domain.tradecomment.dto.TradeCommentResponseDto;
import org.example.backend.domain.tradecomment.entity.TradeComment;
import org.example.backend.domain.tradecomment.repository.TradeCommentRepository;
import org.example.backend.global.exception.ServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class TradeCommentService {

    private final TradeCommentRepository tradeCommentRepository;
    private final MemberRepository memberRepository;
    private final TradeRepository tradeRepository;

    @Transactional
    public TradeCommentResponseDto createComment(BoardType boardType, TradeCommentRequestDto request) {
        Member member = memberRepository.findById(request.memberId())
            .orElseThrow(() -> new ServiceException("404", "존재하지 않는 회원입니다.", HttpStatus.NOT_FOUND));

        Trade trade = tradeRepository.findById(request.tradeId())
            .orElseThrow(
                () -> new ServiceException("404", "존재하지 않는 게시글입니다.", HttpStatus.NOT_FOUND));

        validateBoardType(trade, boardType);

        TradeComment comment = request.toEntity(member, trade);
        TradeComment saved = tradeCommentRepository.save(comment);
        return TradeCommentResponseDto.from(saved);
    }

    public List<TradeCommentResponseDto> getAllComment(BoardType boardType, Long tradeId) {
        Trade trade = tradeRepository.findById(tradeId)
            .orElseThrow(
                () -> new ServiceException("404", "존재하지 않는 게시글입니다.", HttpStatus.NOT_FOUND));

        validateBoardType(trade, boardType);

        return tradeCommentRepository.findByTradeTradeId(tradeId).stream()
            .map(TradeCommentResponseDto::from)
            .toList();
    }

    @Transactional
    public TradeCommentResponseDto updateComment(BoardType boardType, Long tradeId, Long commentId,
        Long memberId, TradeCommentRequestDto request) {
        TradeComment comment = tradeCommentRepository.findById(commentId)
            .orElseThrow(() -> new ServiceException("404", "존재하지 않는 댓글입니다.", HttpStatus.NOT_FOUND));

        validateTrade(comment, tradeId);
        validateBoardType(comment.getTrade(), boardType);
        validateCommentOwner(comment, memberId);

        comment.update(request.content());
        return TradeCommentResponseDto.from(comment);
    }

    @Transactional
    public void deleteComment(BoardType boardType, Long tradeId, Long commentId, Long memberId) {
        TradeComment comment = tradeCommentRepository.findById(commentId)
            .orElseThrow(() -> new ServiceException("404", "존재하지 않는 댓글입니다.", HttpStatus.NOT_FOUND));

        validateTrade(comment, tradeId);
        validateBoardType(comment.getTrade(), boardType);
        validateCommentOwner(comment, memberId);

        tradeCommentRepository.deleteById(commentId);
    }

    private void validateTrade(TradeComment comment, Long tradeId) {
        if (!comment.getTrade().getTradeId().equals(tradeId)) {
            throw new ServiceException("400", "해당 게시글의 댓글이 아닙니다.", HttpStatus.BAD_REQUEST);
        }
    }

    private void validateBoardType(Trade trade, BoardType boardType) {
        if (!trade.getBoardType().equals(boardType)) {
            throw new ServiceException("400", "해당 게시판의 게시글이 아닙니다.", HttpStatus.BAD_REQUEST);
        }
    }

    private void validateCommentOwner(TradeComment comment, Long memberId) {
        if (!comment.getMember().getMemberId().equals(memberId)) {
            throw new ServiceException("403", "댓글 작성자만 수정/삭제할 수 있습니다.", HttpStatus.FORBIDDEN);
        }
    }
}
