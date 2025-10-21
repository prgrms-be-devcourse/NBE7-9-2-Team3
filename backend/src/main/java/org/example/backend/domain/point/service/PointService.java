package org.example.backend.domain.point.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.backend.domain.member.entity.Member;
import org.example.backend.domain.member.repository.MemberRepository;
import org.example.backend.domain.point.dto.PointHistoryResponseDto;
import org.example.backend.domain.point.dto.PurchaseRequestDto;
import org.example.backend.domain.point.entity.Point;
import org.example.backend.domain.point.repository.PointRepository;
import org.example.backend.global.exception.BusinessException;
import org.example.backend.global.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.List;

@Transactional
@Service
@RequiredArgsConstructor
public class PointService {
    private final PointRepository pointRepository;
    private final MemberRepository memberRepository;

    // 포인트 충전
    public void chargePoint(Long memberId, Long amount) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POINT_MEMBER_NOT_FOUND));
        Long newPoints = member.getPoints() + amount;

        member.updatePoints(newPoints);
        Point point = Point.create(member, amount, newPoints);
        pointRepository.save(point);
    }

    /*
    포인트 전체 내역 조회
    - 현재 최신순으로 전체 내역 조회 기능만
    - 내역 타입 별 조회 기능 도입 예정
     */
    public List<PointHistoryResponseDto> getPointHistory(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POINT_MEMBER_NOT_FOUND));
        List<Point> pointHistory = pointRepository.findAllByMemberOrderByCreateDateDesc(member);
        if (pointHistory.isEmpty()) {
            throw new BusinessException(ErrorCode.POINT_HISTORY_NOT_FOUND);
        }

        return pointHistory.stream()
                .map(PointHistoryResponseDto::from)
                .toList();
    }


    // 결제
    public void purchaseItem(Long buyerId, PurchaseRequestDto request) {
        Member buyer = memberRepository.findById(buyerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POINT_BUYER_NOT_FOUND));
        Member seller = memberRepository.findById(request.sellerId())
                .orElseThrow(() -> new BusinessException(ErrorCode.POINT_SELLER_NOT_FOUND));

        if (buyer.getPoints() < request.amount()) {
            throw new BusinessException(ErrorCode.POINT_INSUFFICIENT);
        }

        Long buyerNewPoints = buyer.getPoints() - request.amount();
        Long sellerNewPoints = seller.getPoints() + request.amount();

        buyer.updatePoints(buyerNewPoints);
        seller.updatePoints(sellerNewPoints);

        pointRepository.save(Point.createPurchase(buyer, request.amount(), buyerNewPoints));
        pointRepository.save(Point.createSale(seller, request.amount(), sellerNewPoints));
    }
}