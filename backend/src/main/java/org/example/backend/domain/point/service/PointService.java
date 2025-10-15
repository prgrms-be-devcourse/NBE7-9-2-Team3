package org.example.backend.domain.point.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.backend.domain.member.entity.Member;
import org.example.backend.domain.member.repository.MemberRepository;
import org.example.backend.domain.point.dto.ChargePointsRequestDto;
import org.example.backend.domain.point.dto.PointHistoryResponseDto;
import org.example.backend.domain.point.dto.PurchaseRequestDto;
import org.example.backend.domain.point.entity.Point;
import org.example.backend.domain.point.repository.PointRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Transactional
@Service
@RequiredArgsConstructor
public class PointService {
    private final PointRepository pointRepository;
    private final MemberRepository memberRepository;

    // 포인트 충전
    public void chargePoint(ChargePointsRequestDto request) {
        Member member = memberRepository.findById(request.memberId()).orElse(null);
        Long newPoints = member.getPoints() + request.amount();

        member.updatePoints(newPoints);
        Point point = Point.create(member, request.amount(), newPoints);
        pointRepository.save(point);
    }

    /*
    포인트 전체 내역 조회
    - 현재 최신순으로 전체 내역 조회 기능만
    - 내역 타입 별 조회 기능 도입 예정
     */
    public List<PointHistoryResponseDto> getPointHistory(Long id) {
        Member member = memberRepository.findById(id).orElse(null);
        List<Point> pointHistory = pointRepository.findAllByMemberOrderByCreateDateDesc(member);
        if (pointHistory.isEmpty()) {
            throw new RuntimeException("포인트 내역이 존재하지 않습니다.");
        }

        return pointHistory.stream()
                .map(PointHistoryResponseDto::from)
                .toList();
    }


    //결제
    public void purchaseItem(PurchaseRequestDto request) {
        Member buyer = memberRepository.findById(request.buyerId()).orElse(null);
        Member seller = memberRepository.findById(request.sellerId()).orElse(null);

        if (buyer.getPoints() < request.amount()) {
            throw new RuntimeException("포인트가 부족합니다.")
        }

        Long buyerNewPoints = buyer.getPoints() - request.amount();
        Long sellerNewPoints = seller.getPoints() + request.amount();

        buyer.updatePoints(buyerNewPoints);
        seller.updatePoints(sellerNewPoints);

        pointRepository.save(Point.create(buyer, request.amount(), buyerNewPoints));
        pointRepository.save(Point.create(seller, request.amount(), sellerNewPoints));
    }
}