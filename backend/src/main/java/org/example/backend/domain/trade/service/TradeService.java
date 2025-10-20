package org.example.backend.domain.trade.service;


import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.backend.domain.member.entity.Member;
import org.example.backend.domain.member.repository.MemberRepository;
import org.example.backend.domain.trade.dto.TradeRequestDto;
import org.example.backend.domain.trade.dto.TradeResponseDto;
import org.example.backend.domain.trade.entity.Trade;
import org.example.backend.domain.trade.enums.BoardType;
import org.example.backend.domain.trade.repository.TradeRepository;
import org.example.backend.global.exception.BusinessException;
import org.example.backend.global.exception.ErrorCode;
import org.example.backend.global.image.ImageService;
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
    public TradeResponseDto createTrade(TradeRequestDto request,
        BoardType boardType, List<MultipartFile> images) {
        Member member = memberRepository.findById(request.memberId())
            .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        // 이미지 업로드 (S3) - DB 저장 전에 먼저 업로드
        List<String> imageUrls = new ArrayList<>();
        if (images != null && !images.isEmpty()) {
            imageUrls = imageService.uploadFiles(images, "trade");
        }

        // Trade 엔티티 생성 및 이미지 URL 연결
        Trade trade = request.toEntity(member, boardType);
        imageUrls.forEach(trade::addImage);
        Trade saved = tradeRepository.save(trade);

        return TradeResponseDto.from(saved);
    }

    public List<TradeResponseDto> getAllTrade(BoardType boardType) {
        if (boardType == null) {
            throw new BusinessException(ErrorCode.TRADE_BOARD_TYPE_INVALID);
        }

        return tradeRepository.findByBoardType(boardType).stream()
            .map(TradeResponseDto::from)
            .toList();
    }

    public TradeResponseDto getTrade(BoardType boardType, Long id) {
        Trade trade = tradeRepository
            .findById(id).orElseThrow(
                () -> new BusinessException(ErrorCode.TRADE_NOT_FOUND));

        validateBoardType(trade, boardType);

        return TradeResponseDto.from(trade);
    }

    @Transactional
    public TradeResponseDto updateTrade(BoardType boardType, Long tradeId, Long memberId,
        TradeRequestDto request, List<MultipartFile> images) {
        Trade trade = tradeRepository.findById(tradeId)
            .orElseThrow(
                () -> new BusinessException(ErrorCode.TRADE_NOT_FOUND));

        validateBoardType(trade, boardType);
        validateTradeOwner(trade, memberId);

        trade.update(
            request.title(),
            request.description(),
            request.price(),
            request.status(),
            request.category()
        );

        // 이미지 교체 로직 (새 이미지가 있는 경우만)
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
