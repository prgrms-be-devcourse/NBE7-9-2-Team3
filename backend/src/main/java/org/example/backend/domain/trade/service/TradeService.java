package org.example.backend.domain.trade.service;


import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.backend.domain.member.entity.Member;
import org.example.backend.domain.member.repository.MemberRepository;
import org.example.backend.domain.trade.dto.PageResponseDto;
import org.example.backend.domain.trade.dto.TradeCreateRequestDto;
import org.example.backend.domain.trade.dto.TradeRequestDto;
import org.example.backend.domain.trade.dto.TradeResponseDto;
import org.example.backend.domain.trade.dto.TradeSearchRequestDto;
import org.example.backend.domain.trade.dto.TradeUpdateRequestDto;
import org.example.backend.domain.trade.entity.Trade;
import org.example.backend.domain.trade.enums.BoardType;
import org.example.backend.domain.trade.repository.TradeRepository;
import org.example.backend.global.exception.BusinessException;
import org.example.backend.global.exception.ErrorCode;
import org.example.backend.global.image.ImageService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class TradeService {

    private final TradeRepository tradeRepository;
    private final MemberRepository memberRepository;
    private final ImageService imageService;

    @Transactional
    public TradeResponseDto createTrade(TradeCreateRequestDto request) {
        Member member = memberRepository.findById(request.memberId())
            .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        Trade trade = request.toEntity(member);

        if (request.imageUrls() != null) {
            request.imageUrls().forEach(trade::addImage);
        }

        return TradeResponseDto.from(tradeRepository.save(trade));
    }

    public PageResponseDto<TradeResponseDto> getAllTrade(BoardType boardType,
        TradeSearchRequestDto searchRequest) {

        if (boardType == null) {
            throw new BusinessException(ErrorCode.TRADE_BOARD_TYPE_INVALID);
        }

        Sort sort = switch (searchRequest.sort()) {
            case "price-asc" -> Sort.by(Sort.Direction.ASC, "price");
            case "price-desc" -> Sort.by(Sort.Direction.DESC, "price");
            default -> Sort.by(Sort.Direction.DESC, "createDate");
        };

        Pageable pageable = PageRequest.of(searchRequest.page(), searchRequest.size(), sort);

        Page<Trade> tradePage;
        if (searchRequest.hasSearchTerm() || searchRequest.hasPriceFilter()
            || searchRequest.hasStatusFilter()) {
            tradePage = tradeRepository.searchTrades(
                boardType,
                searchRequest.searchTerm(),
                searchRequest.minPrice() != null ? searchRequest.minPrice().longValue() : null,
                searchRequest.maxPrice() != null ? searchRequest.maxPrice().longValue() : null,
                searchRequest.status(),
                pageable
            );
        } else {
            tradePage = tradeRepository.findByBoardType(boardType, pageable);
        }

        Page<TradeResponseDto> responsePage = tradePage.map(TradeResponseDto::from);
        return PageResponseDto.from(responsePage);
    }

    public TradeResponseDto getTrade(BoardType boardType, Long id) {
        Trade trade = tradeRepository
            .findById(id).orElseThrow(
                () -> new BusinessException(ErrorCode.TRADE_NOT_FOUND));

        validateBoardType(trade, boardType);

        return TradeResponseDto.from(trade);
    }

    @Transactional
    public TradeResponseDto updateTrade(TradeUpdateRequestDto updateRequest) {
        Trade trade = tradeRepository.findById(updateRequest.tradeId())
            .orElseThrow(
                () -> new BusinessException(ErrorCode.TRADE_NOT_FOUND));

        validateBoardType(trade, updateRequest.boardType());
        validateTradeOwner(trade, updateRequest.memberId());

        TradeRequestDto request = updateRequest.tradeData();
        trade.update(
            request.title(),
            request.description(),
            request.price(),
            request.status(),
            request.category()
        );

        // 이미지 교체 로직 (새 이미지 URL이 있는 경우만)
        List<String> newImageUrls = updateRequest.imageUrls();
        if (newImageUrls != null && !newImageUrls.isEmpty()) {
            // 1. 기존 S3 이미지 삭제
            List<String> oldImageUrls = trade.getImageUrls();
            if (!oldImageUrls.isEmpty()) {
                imageService.deleteFiles(oldImageUrls);
            }

            // 2. DB에서 기존 이미지 제거 후 새 이미지 URL 연결
            trade.clearImages();
            newImageUrls.forEach(trade::addImage);
        }

        return TradeResponseDto.from(trade);
    }

    @Transactional
    public void deleteTrade(BoardType boardType, Long tradeId, Long memberId) {
        Trade trade = tradeRepository.findById(tradeId)
            .orElseThrow(
                () -> new BusinessException(ErrorCode.TRADE_NOT_FOUND));

        validateBoardType(trade, boardType);
        validateTradeOwner(trade, memberId);

        // S3 이미지 삭제
        List<String> imageUrls = trade.getImageUrls();
        if (!imageUrls.isEmpty()) {
            imageService.deleteFiles(imageUrls);
        }

        // Trade 엔티티 삭제 (cascade로 TradeImage도 함께 삭제됨)
        tradeRepository.deleteById(tradeId);
    }

    public PageResponseDto<TradeResponseDto> getMyTrades(Long memberId, BoardType boardType,
        int page, int size) {

        Sort sort = Sort.by(Sort.Direction.DESC, "createDate");

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Trade> tradePage = tradeRepository.findMyTrades(memberId, boardType, pageable);

        Page<TradeResponseDto> responsePage = tradePage.map(TradeResponseDto::from);
        return PageResponseDto.from(responsePage);


    }

    private void validateBoardType(Trade trade, BoardType boardType) {
        if (!trade.getBoardType().equals(boardType)) {
            throw new BusinessException(ErrorCode.TRADE_BOARD_TYPE_INVALID);
        }
    }

    private void validateTradeOwner(Trade trade, Long memberId) {
        if (!trade.getMember().getMemberId().equals(memberId)) {
            throw new BusinessException(ErrorCode.TRADE_OWNER_MISMATCH);
        }
    }
}
