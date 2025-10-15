package org.example.backend.domain.trade.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

import org.example.backend.domain.member.entity.Member;
import org.example.backend.domain.trade.enums.BoardType;
import org.example.backend.domain.trade.enums.TradeStatus;

@NoArgsConstructor
@Getter
@Entity
public class Trade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tradeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BoardType boardType;

    @Column(nullable = false, length = 50)
    private String title;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private Long price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TradeStatus status;

    @Column(length = 20)
    private String category;

    private LocalDateTime createDate;

    @OneToMany(mappedBy = "trade", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TradeImage> images = new ArrayList<>();

    public Trade(Member member, BoardType boardType, String title, String description,
        Long price, TradeStatus status, String category, LocalDateTime createDate) {
        this.member = member;
        this.boardType = boardType;
        this.title = title;
        this.description = description;
        this.price = price;
        this.status = status;
        this.category = category;
        this.createDate = createDate;
    }

    public void update(String title, String description, Long price, TradeStatus status,
        String category) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.status = status;
        this.category = category;
    }

}