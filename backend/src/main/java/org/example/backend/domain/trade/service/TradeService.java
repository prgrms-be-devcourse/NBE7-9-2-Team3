package org.example.backend.domain.trade.service;


import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.backend.domain.member.entity.Member;
import org.example.backend.domain.member.repository.MemberRepository;
import org.example.backend.domain.trade.dto.TradeRequestDto;
import org.example.backend.domain.trade.dto.TradeResponseDto;
import org.example.backend.domain.trade.entity.Trade;
import org.example.backend.domain.trade.enums.BoardType;
import org.example.backend.domain.trade.repository.TradeRepository;
import org.example.backend.global.exception.ServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class TradeService {

    private final TradeRepository tradeRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public TradeResponseDto createTrade(TradeRequestDto request, BoardType boardType) {
        Member member = memberRepository.findById(request.memberId())
            .orElseThrow(() -> new ServiceException("404", "존재하지 않는 회원입니다.", HttpStatus.NOT_FOUND));
        Trade trade = request.toEntity(member, boardType);
        Trade saved = tradeRepository.save(trade);
        return TradeResponseDto.from(saved);
    }

    public List<TradeResponseDto> getAllTrade(BoardType boardType) {
        if (boardType == null) {
            throw new ServiceException("400", "게시판 타입은 필수입니다.", HttpStatus.BAD_REQUEST);
        }
        return tradeRepository.findByBoardType(boardType).stream()
            .map(TradeResponseDto::from)
            .toList();
    }

    public TradeResponseDto getTrade(BoardType boardType, Long id) {
        Trade trade = tradeRepository
            .findById(id).orElseThrow(
                () -> new ServiceException("404", "존재하지 않는 게시글입니다.", HttpStatus.NOT_FOUND));
        if (!trade.getBoardType().equals(boardType)) {
            throw new ServiceException("400", "해당 게시판의 게시글이 아닙니다.", HttpStatus.BAD_REQUEST);
        }
        return TradeResponseDto.from(trade);
    }

    @Transactional
    public TradeResponseDto updateTrade(BoardType boardType, Long tradeId, Long memberId,
        TradeRequestDto request) {
        Trade trade = tradeRepository.findById(tradeId)
            .orElseThrow(
                () -> new ServiceException("404", "존재하지 않는 게시글입니다.", HttpStatus.NOT_FOUND));
        if (!trade.getBoardType().equals(boardType)) {
            throw new ServiceException("400", "해당 게시판의 게시글이 아닙니다.", HttpStatus.BAD_REQUEST);
        }
        if (!trade.getMember().getMemberId().equals(memberId)) {
            throw new ServiceException("403", "게시글 수정 권한이 없습니다.", HttpStatus.FORBIDDEN);
        }
        trade.update(
            request.title(),
            request.description(),
            request.price(),
            request.status(),
            request.category()
        );
        return TradeResponseDto.from(trade);
    }

    @Transactional
    public void deleteTrade(BoardType boardType, Long tradeId, Long memberId) {
        Trade trade = tradeRepository.findById(tradeId)
            .orElseThrow(
                () -> new ServiceException("404", "존재하지 않는 게시글입니다.", HttpStatus.NOT_FOUND));
        if (!trade.getBoardType().equals(boardType)) {
            throw new ServiceException("400", "해당 게시판의 게시글이 아닙니다.", HttpStatus.BAD_REQUEST);
        }
        if (!trade.getMember().getMemberId().equals(memberId)) {
            throw new ServiceException("403", "게시판 삭제 권한이 없습니다.", HttpStatus.FORBIDDEN);
        }
        tradeRepository.deleteById(tradeId);
    }
}
