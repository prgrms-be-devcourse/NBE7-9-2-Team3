package org.example.backend.domain.trade.repository;

import java.util.List;
import org.example.backend.domain.trade.entity.Trade;
import org.example.backend.domain.trade.enums.BoardType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface TradeRepository extends JpaRepository<Trade, Long> {

    Page<Trade> findByBoardType(BoardType boardType, Pageable pageable);

    @Query("Select t FROM Trade t WHERE t.member.memberId = :memberId AND t.boardType = :boardType")
    Page<Trade> findMyTrades(@Param("memberId") Long memberId,
        @Param("boardType") BoardType boardType,
        Pageable pageable);
}
