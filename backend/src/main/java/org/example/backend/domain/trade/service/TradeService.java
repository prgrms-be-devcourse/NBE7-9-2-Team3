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
import org.springframework.web.multipart.MultipartFile;

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

        // 이미지 업로드 (S3)
        List<String> imageUrls = new ArrayList<>();
        if (request.images() != null && !request.images().isEmpty()) {
            imageUrls = imageService.uploadFiles(request.images(), "trade");
        }

        // Trade 엔티티 생성 및 이미지 연결
        Trade trade = new Trade(
            member,
            request.boardType(),
            request.title(),
            request.description(),
            request.price(),
            null,
            request.category(),
            java.time.LocalDateTime.now()
        );

        imageUrls.forEach(trade::addImage);
        Trade saved = tradeRepository.save(trade);

        return TradeResponseDto.from(saved);
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
        Page<Trade> tradePage = tradeRepository.findByBoardType(boardType, pageable);
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

        // 이미지 교체 로직 (새 이미지가 있는 경우만)
        List<MultipartFile> images = updateRequest.images();
        if (images != null && !images.isEmpty()) {
            // 1. 먼저 새 이미지 업로드 (업로드 실패 시 기존 이미지 유지)
            List<String> newImageUrls = imageService.uploadFiles(images, "trade");

            // 2. 업로드 성공 시 기존 S3 이미지 삭제
            List<String> oldImageUrls = trade.getImageUrls();
            if (!oldImageUrls.isEmpty()) {
                imageService.deleteFiles(oldImageUrls);
            }

            // 3. DB에서 기존 이미지 제거 후 새 이미지 연결
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
