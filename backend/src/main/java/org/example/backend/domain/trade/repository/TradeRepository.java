package org.example.backend.domain.trade.repository;

import java.util.List;
import org.example.backend.domain.trade.entity.Trade;
import org.example.backend.domain.trade.enums.BoardType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


public interface TradeRepository extends JpaRepository<Trade, Long> {

    List<Trade> findByBoardType(BoardType boardType);

    Page<Trade> findByBoardType(BoardType boardType, Pageable pageable);
}
